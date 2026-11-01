package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.loaders.oreprocessing.ProcessingShaping;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = ProcessingShaping.class, remap = false)
public class MixinProcessingShaping {

    @WrapOperation(
        method = "registerOre",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTModHandler;addCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Z"),
        require = 1)
    private boolean modifyRingRecipe(ItemStack aResult, long aBitMask, Object[] aRecipe, Operation<Boolean> original) {
        return AuditManager.auditResult(AuditScope.RING_FROM_LONG_STICK_HAND_CRAFTING, unit -> {
            ItemData outputData = GTOreDictUnificator.getAssociation(aResult);
            if (outputData != null && outputData.mPrefix == OrePrefixes.ring) {
                for (int i = 0; i < aRecipe.length; i++) {
                    if (aRecipe[i] instanceof ItemData data) {
                        if (data.mPrefix == OrePrefixes.stick) {
                            aRecipe[i] = OrePrefixes.stickLong.get(data.mMaterial.mMaterial);
                            unit.recipeModified();
                            break;
                        }
                    }
                }
            }
            return original.call(aResult, aBitMask, aRecipe);
        });
    }
}
