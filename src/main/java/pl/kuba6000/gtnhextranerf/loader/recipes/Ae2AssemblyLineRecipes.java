package pl.kuba6000.gtnhextranerf.loader.recipes;

import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.AssemblyLine;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;
import static pl.kuba6000.gtnhextranerf.recipes.RecipeItems.requireModItem;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.Materials;
import gregtech.api.enums.Mods;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.recipe.Scanning;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

/** Native registration also creates the Scanner and Assembly Line NEI entries. */
public final class Ae2AssemblyLineRecipes {

    private Ae2AssemblyLineRecipes() {}

    public static void register() {
        if (!Mods.AppliedEnergistics2.isModLoaded()) return;
        AuditManager.run(AuditScope.AE2_ADD_CONTROLLER_ASSEMBLY_LINE, audit -> {
            ItemStack controller = requireModItem(Mods.AppliedEnergistics2, "tile.BlockController", 1, 0);
            ItemStack research = requireModItem(Mods.AppliedEnergistics2, "item.ItemBasicStorageCell.64k", 1, 0);
            if (GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes.stream()
                .anyMatch(
                    recipe -> GTUtility.areStacksEqual(recipe.mOutput, controller, true)
                        || GTUtility.areStacksEqual(recipe.mResearchItem, research, true)))
                throw new RecipeValidationException("Controller output or 64k cell research is already registered");

            if (GTRecipeBuilder.builder()
                .metadata(RESEARCH_ITEM, research)
                .metadata(SCANNING, new Scanning(MINUTES, TierEU.RECIPE_HV))
                .itemInputs(
                    GTOreDictUnificator.get(OrePrefixes.plate, Materials.TPV, 4),
                    new Object[] { OrePrefixes.circuit.get(Materials.HV), 2 },
                    requireModItem(Mods.AppliedEnergistics2, "item.ItemMultiMaterial", 2, 24),
                    requireModItem(Mods.AppliedEnergistics2, "tile.BlockFluix", 1, 0))
                .fluidInputs(Materials.SolderingAlloy.getMolten(576))
                .itemOutputs(controller)
                .duration(30 * SECONDS)
                .eut(TierEU.RECIPE_LuV)
                .addTo(AssemblyLine)
                .isEmpty()) throw new RecipeValidationException("Controller Assembly Line registration failed");
            audit.recipeAdded();
        });
    }

}
