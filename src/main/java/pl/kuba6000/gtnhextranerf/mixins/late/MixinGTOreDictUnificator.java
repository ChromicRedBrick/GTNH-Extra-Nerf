package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.materials.NerfMaterials;

@Mixin(value = GTOreDictUnificator.class, remap = false)
public abstract class MixinGTOreDictUnificator implements NerfMaterials.ThreadSafeUnificationHook {

    @WrapMethod(method = "getNonUnifiedStacks", require = 1)
    private static List<ItemStack> serializeAlternativeLookup(Object stacks, Operation<List<ItemStack>> original) {
        // GT's empty-map guard exposes partially built data to NEI workers after the first insertion.
        synchronized (GTOreDictUnificator.class) {
            return original.call(stacks);
        }
    }
}
