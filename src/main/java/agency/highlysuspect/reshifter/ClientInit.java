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
				int selfHash = IdListExt.cachedHash(Block.STATE_IDS);
				Etc.LOGGER.info("Id hash: " + IdListExt.cachedHash(Block.STATE_IDS));
				Etc.LOGGER.info("Server hash: " + buf.readInt());
				
				PacketByteBuf response = PacketByteBufs.create();
				response.writeInt(selfHash);
				return response;
			}, client);
		});
		
		ClientLoginNetworking.registerGlobalReceiver(Channels.OVERRIDE_STATEMAP, (client, handler, buf, listenerAdder) -> {
			return CompletableFuture.supplyAsync(() -> {
				Etc.LOGGER.info("Received statemap from the server");
				IdList<BlockState> newList = Etc.deserializeStatemap(buf);
				Etc.LOGGER.info("Statemap has " + newList.size() + " entries");
				((BlockExt) Blocks.AIR).replaceIdList(newList);
				return null;
			}, client);
		});
		
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
			Etc.LOGGER.info("Switching back to my own statemap!!!");
			((BlockExt) Blocks.AIR).unreplaceIdList();
		});
	}
}
