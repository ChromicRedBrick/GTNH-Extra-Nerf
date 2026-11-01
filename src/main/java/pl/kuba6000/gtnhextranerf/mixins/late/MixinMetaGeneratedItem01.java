package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTModHandler;
import gregtech.common.items.MetaGeneratedItem01;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = MetaGeneratedItem01.class, remap = false)
public class MixinMetaGeneratedItem01 {

    @WrapOperation(
        method = "craftingShapedRecipes",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTModHandler;addCraftingRecipe(Lnet/minecraft/item/ItemStack;J[Ljava/lang/Object;)Z"),
        require = 1)
    private boolean modifyLvMotorRecipe(ItemStack aResult, long aBitMask, Object[] aRecipe,
        Operation<Boolean> operation) {
        if (!isLvMotor(aResult)) {
            return operation.call(aResult, aBitMask, aRecipe);
        }
        if (isMotorRecipe(aRecipe, Materials.IronMagnetic, Materials.AnyIron)) {
            return AuditManager.auditResult(AuditScope.REMOVE_IRON_LV_MOTOR_HAND_CRAFTING, unit -> {
                unit.recipeRemoved();
                return true;
            });
        }
        if (isMotorRecipe(aRecipe, Materials.SteelMagnetic, Materials.Steel)) {
            return AuditManager.auditResult(AuditScope.MARK_STEEL_LV_MOTOR_HAND_CRAFTING_DISMANTLEABLE, unit -> {
                long dismantleableBits = aBitMask | GTModHandler.RecipeBits.DISMANTLEABLE;
                unit.recipeModified();

                boolean registered = operation.call(aResult, dismantleableBits, aRecipe);
                if (registered) {
                    unit.recipeAdded();
                }
                return registered;
            });
        }
        return operation.call(aResult, aBitMask, aRecipe);
    }

    @Unique
    private static boolean isLvMotor(ItemStack result) {
        return result != null && result.stackSize == 1
            && ItemList.Electric_Motor_LV.get(1)
                .isItemEqual(result);
    }

    @Unique
    private static boolean isMotorRecipe(Object[] recipe, Materials magneticMaterial, Materials rodMaterial) {
        return recipe.length == 11 && "CWR".equals(recipe[0])
            && "WIW".equals(recipe[1])
            && "RWC".equals(recipe[2])
            && Character.valueOf('I')
                .equals(recipe[3])
            && isPrefixMaterial(recipe[4], OrePrefixes.stick, magneticMaterial)
            && Character.valueOf('R')
                .equals(recipe[5])
            && isPrefixMaterial(recipe[6], OrePrefixes.stick, rodMaterial)
            && Character.valueOf('W')
                .equals(recipe[7])
            && isPrefixMaterial(recipe[8], OrePrefixes.wireGt01, Materials.AnyCopper)
            && Character.valueOf('C')
                .equals(recipe[9])
            && isPrefixMaterial(recipe[10], OrePrefixes.cableGt01, Materials.Tin);
    }

    @Unique
    private static boolean isPrefixMaterial(Object ingredient, OrePrefixes prefix, Materials material) {
        if (!(ingredient instanceof ItemData)) return false;
        ItemData data = (ItemData) ingredient;
        return data.mPrefix == prefix && data.mMaterial != null && data.mMaterial.mMaterial == material;
    }
}
