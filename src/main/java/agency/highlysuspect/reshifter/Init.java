package agency.highlysuspect.reshifter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;

public class Init implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
			synchronizer.waitFor(server.submit(() -> {
				Etc.LOGGER.info("Query Start");
				PacketByteBuf buf = PacketByteBufs.create();
				buf.writeInt(IdListExt.cachedHash(Block.STATE_IDS));
				Etc.LOGGER.info("Id hash: " + IdListExt.cachedHash(Block.STATE_IDS));
				sender.sendPacket(Channels.HELLO, buf);
			}));
		});
		
		ServerLoginNetworking.registerGlobalReceiver(Channels.HELLO, (server, handler, understood, buf, synchronizer, responseSender) -> {
			Etc.LOGGER.info("hello packet response received");
			synchronizer.waitFor(server.submit(() -> {
				if(!understood) {
					//Client probably doesn't have the mod installed. That's ok.
					Etc.LOGGER.info("Not understood!");
					return;
				}
				
				int clientHash = buf.readInt();
				boolean matched = clientHash == IdListExt.cachedHash(Block.STATE_IDS);
				Etc.LOGGER.info("Matched: " + matched);
				if(!matched) {
					
					handler.disconnect(new LiteralText("Blockstate ID lists do not match!"));
					
					if(true) return; //TODO
					
					PacketByteBuf response = PacketByteBufs.create();
					Etc.serializeStatemap(response, Block.STATE_IDS);
					responseSender.sendPacket(Channels.OVERRIDE_STATEMAP, response);
				}
			}));
		});
	}
}
