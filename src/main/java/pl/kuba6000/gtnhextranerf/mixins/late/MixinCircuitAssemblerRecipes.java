package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.Collection;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.dreammaster.gthandler.recipes.CircuitAssemblerRecipes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.IRecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

@Mixin(value = CircuitAssemblerRecipes.class, remap = false)
public class MixinCircuitAssemblerRecipes {

    @WrapOperation(
        method = "recipesEditedByBW",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/GTRecipeBuilder;addTo(Lgregtech/api/interfaces/IRecipeMap;)Ljava/util/Collection;"))
    private Collection<GTRecipe> scaleNanoQuantumRecipes(GTRecipeBuilder builder, IRecipeMap map,
        Operation<Collection<GTRecipe>> operation) {
        return AuditManager.auditResult(AuditScope.SCALE_NANO_QUANTUM_CIRCUIT_ASSEMBLER_INPUTS, unit -> {
            if (map != RecipeMaps.circuitAssemblerRecipes) {
                return operation.call(builder, map);
            }

            ItemStack[] outputs = builder.getItemOutputs();
            if (outputs == null || outputs.length == 0 || outputs[0] == null) {
                return operation.call(builder, map);
            }

            ItemStack output = outputs[0];
            boolean isNano = ItemList.Circuit_Nanoprocessor.get(1)
                .isItemEqual(output);
            boolean isQuantum = ItemList.Circuit_Quantumprocessor.get(1)
                .isItemEqual(output);

            if (!isNano && !isQuantum) {
                return operation.call(builder, map);
            }

            ItemStack[] inputs = builder.getItemInputsBasic();
            for (ItemStack input : inputs) {
                if (input != null) {
                    input.stackSize = (int) Math.min(64L, input.stackSize * 6L);
                }
            }
            unit.recipeModified();

            Collection<GTRecipe> recipes = operation.call(builder, map);
            if (recipes != null && !recipes.isEmpty()) {
                unit.recipeAdded();
            }
            return recipes;
        });
    }
}
