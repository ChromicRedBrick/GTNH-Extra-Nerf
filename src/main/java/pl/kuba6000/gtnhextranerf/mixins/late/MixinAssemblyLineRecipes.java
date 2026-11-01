package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IRecipeMap;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTRecipeConstants;
import gregtech.loaders.postload.recipes.AssemblyLineRecipes;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = AssemblyLineRecipes.class, remap = false)
public class MixinAssemblyLineRecipes {

    @ModifyReceiver(
        method = "run()V",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTRecipeBuilder;addTo(Lgregtech/api/interfaces/IRecipeMap;)Ljava/util/Collection;"),
        require = 1)
    private GTRecipeBuilder gtnhextranerf$modifyNetheriteAmount(GTRecipeBuilder builder, IRecipeMap map) {
        return AuditManager.auditResult(AuditScope.SET_DENSE_NETHERITE_ASSEMBLY_LINE_AMOUNT_TO_64, unit -> {
            if (map != GTRecipeConstants.AssemblyLine) {
                return builder;
            }

            ItemStack[] outputs = builder.getItemOutputs();
            if (outputs == null || outputs.length != 1
                || outputs[0] == null
                || outputs[0].stackSize != 1
                || !ItemList.FusionComputer_LuV.isStackEqual(outputs[0])) {
                return builder;
            }

            ItemStack[] inputs = builder.getItemInputsBasic();
            if (inputs == null) {
                return builder;
            }

            int netheriteInputIndex = -1;
            for (int i = 0; i < inputs.length; i++) {
                ItemStack input = inputs[i];
                if (input == null || input.stackSize != 1) {
                    continue;
                }
                ItemData data = GTOreDictUnificator.getAssociation(input);
                if (data != null && data.mPrefix == OrePrefixes.plateDense
                    && data.mMaterial != null
                    && data.mMaterial.mMaterial == Materials.Netherite) {
                    if (netheriteInputIndex != -1) {
                        return builder;
                    }
                    netheriteInputIndex = i;
                }
            }

            if (netheriteInputIndex != -1) {
                inputs[netheriteInputIndex].stackSize = 64;
                unit.recipeModified();
            }
            return builder;
        });
    }
}
