package agency.highlysuspect.reshifter;

import agency.highlysuspect.reshifter.etc.Chunking;
import agency.highlysuspect.reshifter.etc.Etc;
import agency.highlysuspect.reshifter.etc.IdListExt;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.block.Block;
import net.minecraft.text.LiteralText;

@SuppressWarnings("CodeBlock2Expr")
public class Init implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			synchronizer.waitFor(server.submit(() -> {
				sender.sendPacket(Channels.HELLO, PacketByteBufs.create());
			}));
		});
		
		ServerLoginNetworking.registerGlobalReceiver(Channels.HELLO, (server, handler, understood, buf, synchronizer, responseSender) -> {
			synchronizer.waitFor(server.submit(() -> {
				if(!understood) {
					//Client probably doesn't have the mod installed. That's ok.
					return;
				}
				
				int clientHash = buf.readInt();
				int serverHash = IdListExt.cachedHash(Block.STATE_IDS);
				boolean matched = clientHash == serverHash;
				if(!matched) {
					Etc.LOGGER.info("{} is logging in with a mismatched state IDs table (my hash {}, their hash {}). Sending them my copy", handler.getConnectionInfo(), clientHash, serverHash);
					
					try {
						int packetCount = 0;
						
						byte[] data = IdListExt.toCompressedByteArray(Block.STATE_IDS, (state, out) -> new StateDescription(state).write(out));
						Chunking.Chunker chunker = new Chunking.Chunker(data);
						
						while(!chunker.isDone()) {
							responseSender.sendPacket(Channels.STATE_IDS_CHUNK, chunker.writeNextChunk(PacketByteBufs.create()));
							packetCount++;
						}
						
						Etc.LOGGER.info("Successfully compressed and sent state IDs table; it fit into {} packets", packetCount);
					} catch (Exception e) {
						handler.disconnect(new LiteralText("Serverside error compressing state ID table, see server log"));
						Etc.LOGGER.error("Serverside error compressing state ID table", e);
					}
				}
			}));
		});
		
		ServerLoginNetworking.registerGlobalReceiver(Channels.STATE_IDS_CHUNK, (server, handler, understood, buf, synchronizer, responseSender) -> {
			//Don't touch this block of code or you get hit with a bunch of "unexpected query response" kicks after the ID table is sent
			//Not sure why
		});
	}
}
