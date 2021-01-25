package agency.highlysuspect.reshifter;

import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IdList;

public interface BlockExt {
	void replaceIdList(IdList<BlockState> newList);
	void unreplaceIdList();
}
