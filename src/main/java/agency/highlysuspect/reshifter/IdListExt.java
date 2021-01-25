package agency.highlysuspect.reshifter;

import net.minecraft.util.collection.IdList;

public interface IdListExt<T> {
	IdList<T> copy();
	
	int cachedHash();
	int computeHashRange(int start, int end); //start inclusive, end exclusive
	
	@SuppressWarnings("unchecked")
	static <T> int cachedHash(IdList<T> list) {
		return ((IdListExt<T>) list).cachedHash();
	}
}
