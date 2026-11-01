package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import bartworks.system.material.werkstoff_loaders.recipe.CraftingMaterialLoader;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.mixins.support.RingRecipeTransformer;

@Mixin(value = CraftingMaterialLoader.class, remap = false)
public class MixinCraftingMaterialLoader {

    @WrapOperation(
        method = "run",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTModHandler;addCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Z"))
    private boolean modifyRingRecipe(ItemStack aResult, long aBitMask, Object[] aRecipe, Operation<Boolean> operation) {
        return AuditManager.auditResult(AuditScope.RING_FROM_LONG_STICK_WERKSTOFF_HAND_CRAFTING, unit -> {
            if (RingRecipeTransformer.replaceStick(aResult, aRecipe)) {
                unit.recipeModified();

                boolean registered = operation.call(aResult, aBitMask, aRecipe);
                if (registered) {
                    unit.recipeAdded();
                }
                return registered;
            }
            return operation.call(aResult, aBitMask, aRecipe);
        });
    }
}
