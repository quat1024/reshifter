package agency.highlysuspect.reshifter;

import agency.highlysuspect.reshifter.etc.Chunking;
import agency.highlysuspect.reshifter.etc.Etc;
import agency.highlysuspect.reshifter.etc.IdListExt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;

@SuppressWarnings("CodeBlock2Expr")
public class Init implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			synchronizer.waitFor(server.submit(() -> {
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeInt(IdListExt.cachedHash(Block.STATE_IDS));
				sender.sendPacket(Channels.HELLO, buf);
			}));
		});
		
		ServerLoginNetworking.registerGlobalReceiver(Channels.HELLO, (server, handler, understood, buf, synchronizer, responseSender) -> {
			synchronizer.waitFor(server.submit(() -> {
				if(!understood) {
					//Client probably doesn't have the mod installed. That's ok.
					return;
				}
				
				int clientHash = buf.readInt();
				boolean matched = clientHash == IdListExt.cachedHash(Block.STATE_IDS);
				if(!matched) {
					//handler.disconnect(new LiteralText("Blockstate ID lists do not match!"));
					try {
						byte[] data = IdListExt.toCompressedByteArray(Block.STATE_IDS, (state, out) -> new StateDescription(state).write(out));
						Chunking.Chunker chunker = new Chunking.Chunker(data);
						
						Etc.LOGGER.info("total chunked size: " + chunker.totalLength());
						
						while(!chunker.isDone()) {
							Etc.LOGGER.info("sending state_ids_chunk message");
							responseSender.sendPacket(Channels.STATE_IDS_CHUNK, chunker.writeNextChunk(PacketByteBufs.create()));
						}
					} catch (Exception e) {
						handler.disconnect(new LiteralText("Serverside error compressing state ID table, see server log"));
						Etc.LOGGER.error("Serverside error compressing state ID table", e);
					}
				}
			}));
		});
		
		ServerLoginNetworking.registerGlobalReceiver(Channels.STATE_IDS_CHUNK, (server, handler, understood, buf, synchronizer, responseSender) -> {
			//Dont do anything
		});
	}
}
