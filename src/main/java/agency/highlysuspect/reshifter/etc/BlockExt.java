package agency.highlysuspect.reshifter.etc;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.IdList;

public interface BlockExt {
	void replaceIdList(IdList<BlockState> newList);
	void unreplaceIdList();
	
	static void replaceIdList0(IdList<BlockState> newList) {
		((BlockExt) Blocks.AIR).replaceIdList(newList);
	}
	
	static void unreplaceIdList0() {
		((BlockExt) Blocks.AIR).unreplaceIdList();
	}
}
