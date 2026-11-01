package pl.kuba6000.gtnhextranerf.mixins.support;

import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.recipe.RecipeMaps.cableRecipes;
import static gregtech.api.util.GTRecipeBuilder.HALF_INGOTS;
import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.QUARTER_INGOTS;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IRecipeMap;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import pl.kuba6000.gtnhextranerf.audit.AuditHandle;

public final class ProcessingWireRecipeTransformer {

    private static final List<Materials> TARGET_MATERIALS = Arrays.asList(
        Materials.RedstoneAlloy,
        Materials.ElectricalSteel,
        Materials.EnergeticAlloy,
        Materials.Nichrome,
        Materials.Steel,
        Materials.BlackSteel,
        Materials.Titanium,
        Materials.Aluminium,
        Materials.TPV,
        Materials.VibrantAlloy);

    private static final List<OrePrefixes> WIRE_PREFIXES = Arrays.asList(
        OrePrefixes.wireGt01,
        OrePrefixes.wireGt02,
        OrePrefixes.wireGt04,
        OrePrefixes.wireGt08,
        OrePrefixes.wireGt12,
        OrePrefixes.wireGt16);

    private ProcessingWireRecipeTransformer() {}

    public static GTRecipeBuilder transform(GTRecipeBuilder builder, IRecipeMap map, OrePrefixes prefix,
        Materials material, AuditHandle audit) {
        if ((map != assemblerRecipes && map != cableRecipes) || !WIRE_PREFIXES.contains(prefix)
            || !TARGET_MATERIALS.contains(material)) {
            return builder;
        }

        ItemStack[] inputs = builder.getItemInputsBasic();
        ItemStack[] outputs = builder.getItemOutputs();
        FluidStack[] fluids = builder.getFluidInputs();
        if (!hasUsableItemShape(inputs, outputs) || isWireOutput(outputs[0])
            || !hasSingleFluid(fluids)
            || containsPpsFoil(inputs)) {
            return builder;
        }

        long amount = directRecipePpsAmount(inputs, fluids[0]);
        int insertionIndex = inputs.length - 1;
        if (amount <= 0) {
            amount = dielectricRecipePpsAmount(inputs, outputs, fluids[0]);
            insertionIndex = inputs.length;
        }
        if (amount > 0) {
            insertPpsFoil(builder, inputs, insertionIndex, amount);
            audit.recipeModified();
        }
        return builder;
    }

    private static long dielectricRecipePpsAmount(ItemStack[] inputs, ItemStack[] outputs, FluidStack fluid) {
        if (isFourCableDielectricRecipe(inputs, outputs, fluid)) return inputs[1].stackSize * 4L;
        if (isSingleCableDielectricRecipe(inputs, outputs, fluid)) return inputs[1].stackSize;
        return 0;
    }

    private static boolean hasUsableItemShape(ItemStack[] inputs, ItemStack[] outputs) {
        return inputs != null && inputs.length >= 2
            && inputs[0] != null
            && outputs != null
            && outputs.length == 1
            && outputs[0] != null;
    }

    private static boolean hasSingleFluid(FluidStack[] fluids) {
        return fluids != null && fluids.length == 1 && fluids[0] != null;
    }

    private static boolean isWireOutput(ItemStack output) {
        ItemData data = GTOreDictUnificator.getAssociation(output);
        return data != null && WIRE_PREFIXES.contains(data.mPrefix);
    }

    private static long directRecipePpsAmount(ItemStack[] inputs, FluidStack fluid) {
        if (!isCircuit(inputs[inputs.length - 1], 24)) {
            return 0;
        }

        long amount = amountFromFluid(fluid, Materials.Rubber.getMolten(1), INGOTS);
        if (amount > 0) return amount;

        amount = amountFromFluid(fluid, Materials.StyreneButadieneRubber.getMolten(1), 3L * QUARTER_INGOTS);
        if (amount > 0) return amount;

        return amountFromFluid(fluid, Materials.RubberSilicone.getMolten(1), HALF_INGOTS);
    }

    private static boolean isFourCableDielectricRecipe(ItemStack[] inputs, ItemStack[] outputs, FluidStack fluid) {
        return inputs.length == 2 && inputs[0].stackSize == 4
            && outputs[0].stackSize == 4
            && isDielectric(inputs[1], OrePrefixes.dust)
            && isSyntheticRubber(fluid)
            && fluid.amount == inputs[1].stackSize * INGOTS;
    }

    private static boolean isSingleCableDielectricRecipe(ItemStack[] inputs, ItemStack[] outputs, FluidStack fluid) {
        return inputs.length == 2 && inputs[0].stackSize == 1
            && outputs[0].stackSize == 1
            && isDielectric(inputs[1], OrePrefixes.dustSmall)
            && isSyntheticRubber(fluid)
            && fluid.amount == inputs[1].stackSize * QUARTER_INGOTS;
    }

    private static boolean isDielectric(ItemStack stack, OrePrefixes prefix) {
        ItemData data = GTOreDictUnificator.getAssociation(stack);
        if (data == null || data.mPrefix != prefix || data.mMaterial == null) {
            return false;
        }
        Materials material = data.mMaterial.mMaterial;
        return material == Materials.PolyvinylChloride || material == Materials.Polydimethylsiloxane;
    }

    private static boolean isSyntheticRubber(FluidStack fluid) {
        return fluidMatches(fluid, Materials.StyreneButadieneRubber.getMolten(1))
            || fluidMatches(fluid, Materials.RubberSilicone.getMolten(1));
    }

    private static boolean containsPpsFoil(ItemStack[] inputs) {
        for (ItemStack input : inputs) {
            ItemData data = GTOreDictUnificator.getAssociation(input);
            if (data != null && data.mPrefix == OrePrefixes.foil
                && data.mMaterial != null
                && data.mMaterial.mMaterial == Materials.PolyphenyleneSulfide) {
                return true;
            }
        }
        return false;
    }

    private static long amountFromFluid(FluidStack actual, FluidStack expected, long amountPerFoil) {
        if (!fluidMatches(actual, expected) || actual.amount <= 0 || actual.amount % amountPerFoil != 0) {
            return 0;
        }
        return actual.amount / amountPerFoil;
    }

    private static boolean fluidMatches(FluidStack actual, FluidStack expected) {
        return actual != null && expected != null && actual.isFluidEqual(expected);
    }

    private static boolean isCircuit(ItemStack stack, int configuration) {
        ItemStack circuit = GTUtility.getIntegratedCircuit(configuration);
        return stack != null && circuit != null && circuit.isItemEqual(stack);
    }

    private static void insertPpsFoil(GTRecipeBuilder builder, ItemStack[] inputs, int index, long amount) {
        ItemStack[] newInputs = new ItemStack[inputs.length + 1];
        System.arraycopy(inputs, 0, newInputs, 0, index);
        newInputs[index] = ppsFoil(amount);
        System.arraycopy(inputs, index, newInputs, index + 1, inputs.length - index);
        builder.itemInputs(newInputs);
    }

    private static ItemStack ppsFoil(long amount) {
        return GTOreDictUnificator.get(OrePrefixes.foil, Materials.PolyphenyleneSulfide, amount);
    }
}
