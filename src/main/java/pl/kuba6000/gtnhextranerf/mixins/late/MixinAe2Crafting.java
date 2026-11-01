package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.dreammaster.scripts.ScriptAppliedEnergistics2;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.mixins.support.Ae2RecipeIngredients;

@Mixin(value = ScriptAppliedEnergistics2.class, remap = false)
public abstract class MixinAe2Crafting {

    @WrapOperation(
        method = "loadRecipes",
        at = @At(
            value = "INVOKE",
            target = "Lcom/dreammaster/scripts/ScriptAppliedEnergistics2;addShapedRecipe(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)Z"),
        require = 1)
    private boolean gtnhextranerf$tpvShaped(ScriptAppliedEnergistics2 script, ItemStack output, Object[] inputs,
        Operation<Boolean> original) {
        return AuditManager.auditResult(
            AuditScope.AE2_TPV_CRAFTING,
            audit -> original.call(script, output, Ae2RecipeIngredients.crafting(output, inputs, audit)));
    }

    @WrapOperation(
        method = "loadRecipes",
        at = @At(
            value = "INVOKE",
            target = "Lcom/dreammaster/scripts/ScriptAppliedEnergistics2;addShapelessRecipe(Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)Z"),
        require = 1)
    private boolean gtnhextranerf$tpvShapeless(ScriptAppliedEnergistics2 script, ItemStack output, Object[] inputs,
        Operation<Boolean> original) {
        return AuditManager.auditResult(
            AuditScope.AE2_TPV_CRAFTING,
            audit -> original.call(script, output, Ae2RecipeIngredients.crafting(output, inputs, audit)));
    }

    @WrapOperation(
        method = "loadRecipes",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTModHandler;addCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Z"),
        require = 1)
    private boolean gtnhextranerf$tpvGtShapedWithFlags(ItemStack output, long flags, Object[] inputs,
        Operation<Boolean> original) {
        return AuditManager.auditResult(
            AuditScope.AE2_TPV_CRAFTING,
            audit -> original.call(output, flags, Ae2RecipeIngredients.crafting(output, inputs, audit)));
    }
}
