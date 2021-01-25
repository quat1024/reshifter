package agency.highlysuspect.reshifter.mixin;

import agency.highlysuspect.reshifter.IdListExt;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(IdList.class)
public abstract class IdListMixin<T> implements IdListExt<T> {
	@Shadow @Final @Mutable private List<@Nullable T> list;
	@Shadow public abstract int size();
	
	@Unique private boolean hashDirty = true;
	@Unique private int hashCache;
	
	@Inject(method = "add", at = @At("HEAD"))
	private void onAdd(CallbackInfo ci) {
		hashDirty = true;
	}
	
	@Inject(method = "set", at = @At("HEAD"))
	private void onSet(CallbackInfo ci) {
		hashDirty = true;
	}
	
	@Override
	public int cachedHash() {
		if(hashDirty) {
			hashCache = computeHashRange(0, size());
			hashDirty = false;
		}
		return hashCache;
	}
	
	@Override
	public int computeHashRange(int start, int end) {
		int hash = 0;
		for(int i = start; i < end; i++) {
			//Forgive me for this toString call.
			//Nothing in BlockState or any superclasses implements hashCode.
			//However, there is a custom toString, and it's stable and good enough to hash.
			//This is also more likely to be compatible with optimization mods that radically change the BlockState guts.
			//TODO: Verify that!
			// I might have to write my own blockstate hasher that ensures all the properties stay in the same predictable order.
			// It's ok for this to be relatively expensive; the hashcode is cached in the happy path where there are no errors.
			//
			//Oh also the Objects.toString is because the list is *technically* nullable, if there's gaps in it.
			//Probably doesn't come up in practice, doesn't hurt to check.
			hash ^= Objects.toString(list.get(i)).hashCode();
			hash ^= MathHelper.idealHash(i);
			hash *= 31;
		}
		return hash;
	}
}
