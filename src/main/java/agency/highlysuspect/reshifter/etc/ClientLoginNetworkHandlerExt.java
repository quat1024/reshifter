package agency.highlysuspect.reshifter.etc;

import net.minecraft.client.network.ClientLoginNetworkHandler;

public interface ClientLoginNetworkHandlerExt {
	void freeUnchunker();
	Chunking.Unchunker getOrCreateUnchunker();
	
	static void freeUnchunker(ClientLoginNetworkHandler handler) {
		((ClientLoginNetworkHandlerExt) handler).freeUnchunker();
	}
	
	static Chunking.Unchunker getOrCreateUnchunker(ClientLoginNetworkHandler handler) {
		return ((ClientLoginNetworkHandlerExt) handler).getOrCreateUnchunker();
	}
}
