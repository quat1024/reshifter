package agency.highlysuspect.reshifter.mixin.client;

import agency.highlysuspect.reshifter.etc.Chunking;
import agency.highlysuspect.reshifter.etc.ClientLoginNetworkHandlerExt;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandlerMixin implements ClientLoginNetworkHandlerExt {
	@Unique Chunking.Unchunker unchunker;
	
	@Override
	public void freeUnchunker() {
		unchunker = null;
	}
	
	@Override
	public Chunking.Unchunker getOrCreateUnchunker() {
		if(unchunker == null) unchunker = new Chunking.Unchunker();
		return unchunker;
	}
}
