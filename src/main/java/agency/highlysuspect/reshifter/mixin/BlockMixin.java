package agency.highlysuspect.reshifter.mixin;

import agency.highlysuspect.reshifter.BlockExt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.*;

@Mixin(Block.class)
public class BlockMixin implements BlockExt {
	@Shadow @Final @Mutable public static IdList<BlockState> STATE_IDS;
	@Unique private static IdList<BlockState> BACKUP_STATES;
	
	@Override
	public void replaceIdList(IdList<BlockState> newList) {
		BACKUP_STATES = STATE_IDS;
		STATE_IDS = newList;
	}
	
	@Override
	public void unreplaceIdList() {
		if(BACKUP_STATES != null)	STATE_IDS = BACKUP_STATES;
	}
}
