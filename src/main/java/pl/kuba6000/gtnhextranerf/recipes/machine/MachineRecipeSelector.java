package pl.kuba6000.gtnhextranerf.recipes.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import gregtech.api.util.GTRecipe;

/** Ingredient comparison used internally by RecipeMatch. */
final class MachineRecipeSelector {

    private MachineRecipeSelector() {}

    public static Predicate<GTRecipe> itemInputsExactly(ItemStack... expected) {
        ItemStack[] signature = copyItems(expected);
        return recipe -> itemsExactly(recipe.mInputs, signature);
    }

    public static Predicate<GTRecipe> itemInputsExactlyWithOreDict(ItemStack[] expectedItems,
        Map<String, Integer> expectedOres) {
        ItemStack[] itemSignature = copyItems(expectedItems);
        Map<Integer, Integer> oreAmounts = new HashMap<>();
        expectedOres.forEach((name, amount) -> oreAmounts.put(OreDictionary.getOreID(name), amount));
        return recipe -> itemInputsExactlyWithOreDict(recipe, itemSignature, oreAmounts);
    }

    public static Predicate<GTRecipe> itemOutputsExactly(ItemStack... expected) {
        ItemStack[] signature = copyItems(expected);
        return recipe -> itemsExactly(recipe.mOutputs, signature);
    }

    public static Predicate<GTRecipe> fluidInputsExactly(FluidStack... expected) {
        FluidStack[] signature = copyFluids(expected);
        return recipe -> fluidsExactly(recipe.mFluidInputs, signature);
    }

    public static Predicate<GTRecipe> fluidOutputsExactly(FluidStack... expected) {
        FluidStack[] signature = copyFluids(expected);
        return recipe -> fluidsExactly(recipe.mFluidOutputs, signature);
    }

    public static boolean itemsExactly(ItemStack[] actual, ItemStack[] expected) {
        return unorderedMultisetEquals(actual, expected, ItemStack::areItemStacksEqual);
    }

    private static boolean itemInputsExactlyWithOreDict(GTRecipe recipe, ItemStack[] expectedItems,
        Map<Integer, Integer> oreAmounts) {
        if (!(recipe instanceof GTRecipe.GTRecipe_WithAlt)) return false;
        GTRecipe.GTRecipe_WithAlt withAlt = (GTRecipe.GTRecipe_WithAlt) recipe;
        if (!hasExpectedOreDictStructure(withAlt, expectedItems.length + oreAmounts.size())) return false;

        List<ItemStack> ordinaryInputs = new ArrayList<>(expectedItems.length);
        Set<Integer> foundOres = new HashSet<>();
        for (int i = 0; i < withAlt.mInputs.length; i++) {
            ItemStack input = withAlt.mInputs[i];
            int slotOreId = i < withAlt.mOreDictIds.length ? withAlt.mOreDictIds[i] : -1;
            Integer amount = oreAmounts.get(slotOreId);
            if (amount != null) {
                if (!foundOres.add(slotOreId) || !isExactOreDictSlot(withAlt, i, input, amount)) return false;
            } else {
                if (slotOreId != -1) return false;
                ordinaryInputs.add(input);
            }
        }
        return foundOres.size() == oreAmounts.size()
            && itemsExactly(ordinaryInputs.toArray(new ItemStack[0]), expectedItems);
    }

    private static boolean hasExpectedOreDictStructure(GTRecipe.GTRecipe_WithAlt recipe, int expectedInputCount) {
        return recipe.mInputs != null && recipe.mOreDictIds != null
            && recipe.mOreDictAlt != null
            && recipe.mInputs.length == expectedInputCount
            && recipe.mOreDictIds.length <= recipe.mInputs.length
            && recipe.mOreDictAlt.length <= recipe.mInputs.length;
    }

    private static boolean isExactOreDictSlot(GTRecipe.GTRecipe_WithAlt recipe, int index, ItemStack input,
        int expectedAmount) {
        return input != null && input.stackSize == expectedAmount
            && index < recipe.mOreDictAlt.length
            && recipe.mOreDictAlt[index] != null
            && recipe.mOreDictAlt[index].length > 0;
    }

    public static boolean fluidsExactly(FluidStack[] actual, FluidStack[] expected) {
        return unorderedMultisetEquals(actual, expected, FluidStack::isFluidStackIdentical);
    }

    static ItemStack[] copyItems(ItemStack[] stacks) {
        ItemStack[] result = new ItemStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) result[i] = stacks[i] == null ? null : stacks[i].copy();
        return result;
    }

    private static FluidStack[] copyFluids(FluidStack[] stacks) {
        FluidStack[] result = new FluidStack[stacks.length];
        for (int i = 0; i < stacks.length; i++) result[i] = stacks[i] == null ? null : stacks[i].copy();
        return result;
    }

    private static <T> boolean unorderedMultisetEquals(T[] actual, T[] expected, BiPredicate<T, T> equals) {
        if (actual == null || expected == null || actual.length != expected.length) {
            return false;
        }
        boolean[] matched = new boolean[expected.length];
        for (T actualValue : actual) {
            boolean found = false;
            for (int i = 0; i < expected.length; i++) {
                if (!matched[i] && actualValue != null
                    && expected[i] != null
                    && equals.test(actualValue, expected[i])) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

}
