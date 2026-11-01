package pl.kuba6000.gtnhextranerf.integration;

import static gregtech.api.recipe.RecipeMaps.fusionRecipes;
import static gregtech.api.util.GTRecipeConstants.FUSION_THRESHOLD;

import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.Materials;
import gregtech.api.util.GTRecipe;
import gregtech.nei.formatter.FusionSpecialValueFormatter;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeMatch;

/** Verifies the documented fusion balance through the registered GregTech recipes. */
final class FusionRecipeIntegrationTests {

    private FusionRecipeIntegrationTests() {}

    static void verify() {
        verifyRecipe(
            Materials.Europium.getMolten(4),
            32,
            24_576,
            150_000_000L,
            Materials.Neodymium.getMolten(64),
            Materials.Hydrogen.getGas(192));
        verifyRecipe(
            Materials.Europium.getMolten(16),
            32,
            24_576,
            150_000_000L,
            Materials.Neodymium.getMolten(16),
            Materials.Promethium.getMolten(8));
        verifyRecipe(
            Materials.Americium.getMolten(4),
            96,
            49_152,
            200_000_000L,
            Materials.Lutetium.getMolten(64),
            Materials.Chrome.getMolten(64));
        verifyRecipe(
            Materials.Americium.getMolten(16),
            96,
            38_764,
            200_000_000L,
            Materials.Lutetium.getMolten(16),
            Materials.Promethium.getMolten(8));
        // The separate plasma route changes only ignition energy, requiring MK4.
        verifyRecipe(
            Materials.Americium.getPlasma(144),
            64,
            98_304,
            1_000_000_000L,
            Materials.Plutonium241.getMolten(144),
            Materials.Hydrogen.getGas(2_000));
    }

    private static void verifyRecipe(FluidStack output, int duration, int eut, long ignition, FluidStack... inputs) {
        GTRecipe found = null;
        for (GTRecipe recipe : fusionRecipes.getAllRecipes()) {
            if (recipe.mFluidOutputs.length != 1 || !recipe.mFluidOutputs[0].isFluidEqual(output)
                || recipe.mFluidInputs.length != inputs.length) continue;
            boolean sameIngredients = true;
            for (FluidStack input : inputs) {
                boolean present = false;
                for (FluidStack actual : recipe.mFluidInputs) present |= actual.isFluidEqual(input);
                sameIngredients &= present;
            }
            if (!sameIngredients) continue;
            check(
                found == null,
                "duplicate fusion route for " + output.getFluid()
                    .getName());
            found = recipe;
        }
        check(
            found != null,
            "missing fusion route for " + output.getFluid()
                .getName());
        check(
            RecipeMatch.builder()
                .itemInputs()
                .itemOutputs()
                .fluidInputs(inputs)
                .fluidOutputs(output)
                .duration(duration)
                .eut(eut)
                .build()
                .test(found),
            "fusion balance differs from the documented contract for " + output.getFluid()
                .getName()
                + " with "
                + inputs[1].getFluid()
                    .getName());
        check(
            found.getMetadataOrDefault(FUSION_THRESHOLD, 0L) == ignition,
            "fusion ignition threshold changed for " + output.getFluid()
                .getName());
        if (output.isFluidEqual(Materials.Americium.getPlasma(1))) {
            check(
                FusionSpecialValueFormatter.getFusionTier(found.getMetadataOrDefault(FUSION_THRESHOLD, 0L), found.mEUt)
                    == 4,
                "Americium plasma must require fusion MK4");
        }
        // Exercise the machine's lookup, including indexes rebuilt after changing fluid amounts.
        check(
            fusionRecipes.findRecipeQuery()
                .items()
                .fluids(inputs)
                .voltage(eut)
                .find() == found,
            "fusion machine cannot find the modified route for " + output.getFluid()
                .getName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
