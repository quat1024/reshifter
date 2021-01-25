package agency.highlysuspect.reshifter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IdList;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("CodeBlock2Expr")
public class ClientInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(Channels.HELLO, (client, handler, buf, listenerAdder) -> {
			return CompletableFuture.supplyAsync(() -> {
				//TODO: doesn't actually do anything with the server-sent data, lol
				// could use that to get a little jump on the rsync algorithm (client would send the first couple of hashes now)
				int serverHash = buf.readInt();
				
				PacketByteBuf response = PacketByteBufs.create();
				response.writeInt(IdListExt.cachedHash(Block.STATE_IDS));
				return response;
			}, client);
		});
		
//		ClientLoginNetworking.registerGlobalReceiver(Channels.OVERRIDE_STATEMAP, (client, handler, buf, listenerAdder) -> {
//			return CompletableFuture.supplyAsync(() -> {
//				IdList<BlockState> newList = Etc.deserializeStatemap(buf);
//				((BlockExt) Blocks.AIR).replaceIdList(newList);
//				return null;
//			}, client);
//		});
		
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
			//Mfw it's static
			((BlockExt) Blocks.AIR).unreplaceIdList();
		});
	}
}
