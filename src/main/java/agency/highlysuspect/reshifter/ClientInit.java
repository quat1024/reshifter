package agency.highlysuspect.reshifter;

import agency.highlysuspect.reshifter.etc.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
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
		
		ClientLoginNetworking.registerGlobalReceiver(Channels.STATE_IDS_CHUNK, (client, handler, buf, listenerAdder) -> {
			return CompletableFuture.supplyAsync(() -> {
				Etc.LOGGER.info("received state ids chunk packet");
				
				Chunking.Unchunker unchunker = ClientLoginNetworkHandlerExt.getOrCreateUnchunker(handler);
				unchunker.handleByteBuf(buf);
				
				if(unchunker.isFinished()) {
					Etc.LOGGER.info("unchunker says it's finished");
					try {
						byte[] result = unchunker.getBytes();
						IdList<BlockState> newList = IdListExt.fromCompressedByteArray(result, data -> StateDescription.read(data).approximateBlockState());
						BlockExt.replaceIdList0(newList);
					} catch (Exception e) {
						Etc.LOGGER.error("Clientside error decompressing state id table", e);
						handler.onDisconnected(new LiteralText("Clientside error decompressing state id table, see log"));
					} finally {
						ClientLoginNetworkHandlerExt.freeUnchunker(handler);
					}
				}
				
				return PacketByteBufs.create();
			}, client);
		});
		
		ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> {
			BlockExt.unreplaceIdList0();
			ClientLoginNetworkHandlerExt.freeUnchunker(handler);
		});
	}
}
