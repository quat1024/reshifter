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
					handler.disconnect(new LiteralText("Blockstate ID lists do not match!"));
					
					//TODO
					
//					PacketByteBuf response = PacketByteBufs.create();
//					Etc.serializeStatemap(response, Block.STATE_IDS);
//					responseSender.sendPacket(Channels.OVERRIDE_STATEMAP, response);
				}
			}));
		});
	}
}
