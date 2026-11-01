package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;

import gregtech.api.util.GTRecipe;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeMatch;

class RecipeMatchTest {

    @Test
    void matchesGuaranteedOutputsWhenGregTechOmitsTheChanceArray() {
        GTRecipe recipe = recipe();
        recipe.mOutputs = new ItemStack[] { new ItemStack(new Item()), new ItemStack(new Item()) };
        RecipeMatch guaranteed = RecipeMatch.builder()
            .outputChances(10000, 10000)
            .build();
        recipe.mOutputChances = null;
        assertTrue(guaranteed.test(recipe));
        recipe.mOutputChances = new int[] { 10000, 10000 };
        assertTrue(guaranteed.test(recipe));
        recipe.mOutputChances = new int[] { 7500 };
        assertFalse(guaranteed.test(recipe));
        RecipeMatch mixed = RecipeMatch.builder()
            .outputChances(7500, 10000)
            .build();
        assertTrue(mixed.test(recipe));
        recipe.mOutputs = new ItemStack[] { new ItemStack(new Item()) };
        assertFalse(mixed.test(recipe));
    }

    @Test
    void builtMatchesAreIndependentOfTheirBuilderAndCallerArrays() {
        ItemStack ingredient = new ItemStack(new Item(), 2, 3);
        ItemStack[] inputs = { ingredient };
        int[] chances = { 7500 };
        GTRecipe recipe = recipe();
        recipe.mInputs = new ItemStack[] { ingredient.copy() };
        recipe.mOutputs = new ItemStack[] { ingredient.copy() };
        recipe.mOutputChances = chances.clone();
        recipe.mDuration = 20;
        RecipeMatch.Builder builder = RecipeMatch.builder()
            .itemInputs(inputs)
            .duration(20)
            .outputChances(chances);
        RecipeMatch first = builder.build();

        ingredient.stackSize = 9;
        inputs[0] = new ItemStack(new Item());
        chances[0] = 100;
        RecipeMatch second = builder.duration(40)
            .build();

        assertTrue(first.test(recipe));
        assertFalse(second.test(recipe));
        recipe.mDuration = 40;
        assertTrue(second.test(recipe));
        assertFalse(first.test(recipe));
    }

    @Test
    void itemMatchingPreservesMultiplicityMetadataAndNbt() {
        ItemStack ingredient = new ItemStack(new Item(), 1, 3);
        ingredient.setTagCompound(new NBTTagCompound());
        ingredient.getTagCompound()
            .setString("variant", "expected");
        RecipeMatch match = RecipeMatch.builder()
            .itemInputs(ingredient, ingredient)
            .build();
        GTRecipe recipe = recipe();
        recipe.mInputs = new ItemStack[] { ingredient.copy(), ingredient.copy() };
        assertTrue(match.test(recipe));
        recipe.mInputs[1].setItemDamage(4);
        assertFalse(match.test(recipe));
        recipe.mInputs[1] = ingredient.copy();
        recipe.mInputs[1].getTagCompound()
            .setString("variant", "different");
        assertFalse(match.test(recipe));
        recipe.mInputs = new ItemStack[] { ingredient.copy() };
        assertFalse(match.test(recipe));
    }

    @Test
    void explicitFluidAlternativesReplaceAnEarlierCountConstraint() {
        RecipeMatch match = RecipeMatch.builder()
            .fluidInputCount(1)
            .fluidInputsAnyOf(new FluidStack[0])
            .build();
        GTRecipe recipe = recipe();
        assertTrue(match.test(recipe));
        recipe.mFluidInputs = new FluidStack[1];
        assertFalse(match.test(recipe));
    }

    @Test
    void rejectsIncompleteOrInvalidMatchDefinitions() {
        assertThrows(
            IllegalStateException.class,
            () -> RecipeMatch.builder()
                .build());
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeMatch.builder()
                .oreDictInput("", 1));
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeMatch.builder()
                .oreDictInput("dustAnyCarbon", 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeMatch.builder()
                .fluidInputsAnyOf());
        assertThrows(IllegalArgumentException.class, () -> RecipeMatch.anyOf());
    }

    @Test
    void fluidCountAndAlternativeSignaturesKeepTheirOwnConstraints() {
        GTRecipe recipe = recipe();
        recipe.mDuration = 20;
        recipe.mEUt = 8;
        recipe.mFluidInputs = new FluidStack[1];
        RecipeMatch first = RecipeMatch.builder()
            .duration(20)
            .eut(8)
            .fluidInputCount(1)
            .build();
        RecipeMatch second = RecipeMatch.builder()
            .duration(40)
            .eut(32)
            .fluidInputs()
            .build();
        RecipeMatch either = RecipeMatch.anyOf(first, second);

        assertTrue(either.test(recipe));
        recipe.mEUt = 32;
        assertFalse(either.test(recipe));
        recipe.mDuration = 40;
        recipe.mFluidInputs = new FluidStack[0];
        assertTrue(either.test(recipe));
        recipe.mFluidInputs = new FluidStack[1];
        assertFalse(either.test(recipe));
    }

    @Test
    void matchesExactIngredientsAmountsOutputsAndChances() {
        ItemStack first = new ItemStack(new Item(), 2, 3);
        ItemStack second = new ItemStack(new Item(), 1, 0);
        GTRecipe recipe = recipe();
        recipe.mInputs = new ItemStack[] { second.copy(), first.copy() };
        recipe.mOutputs = new ItemStack[] { first.copy() };
        recipe.mOutputChances = new int[] { 7500 };
        recipe.mSpecialValue = 900;

        RecipeMatch match = RecipeMatch.builder()
            .itemInputs(first, second)
            .itemOutputs(first)
            .fluidInputs()
            .fluidOutputs()
            .outputChances(7500)
            .specialValue(900)
            .build();

        assertTrue(match.test(recipe));
        recipe.mInputs[1].stackSize++;
        assertFalse(match.test(recipe));
        recipe.mInputs[1] = first.copy();
        recipe.mFluidOutputs = new FluidStack[1];
        assertFalse(match.test(recipe));
        recipe.mFluidOutputs = new FluidStack[0];
        recipe.mOutputChances[0]++;
        assertFalse(match.test(recipe));
        recipe.mOutputChances[0] = 7500;
        recipe.mSpecialValue++;
        assertFalse(match.test(recipe));
    }

    @Test
    void matchesDeclaredFieldsAndLeavesOmittedFieldsUnrestricted() {
        GTRecipe recipe = recipe();
        recipe.mDuration = 40;
        recipe.mEUt = 32;
        recipe.mSpecialValue = 900;

        RecipeMatch match = RecipeMatch.builder()
            .duration(40)
            .eut(32)
            .build();

        assertTrue(match.test(recipe));
        recipe.mSpecialValue = 1200;
        assertTrue(match.test(recipe));
        recipe.mDuration++;
        assertFalse(match.test(recipe));
        recipe.mDuration = 40;
        recipe.mEUt++;
        assertFalse(match.test(recipe));
    }

    private static GTRecipe recipe() {
        // GTRecipe constructors require a live Forge/GT proxy. Use its public data fields as an external fixture.
        GTRecipe recipe = new ObjenesisStd().newInstance(GTRecipe.class);
        recipe.mInputs = new ItemStack[0];
        recipe.mOutputs = new ItemStack[0];
        recipe.mFluidInputs = new FluidStack[0];
        recipe.mFluidOutputs = new FluidStack[0];
        recipe.mOutputChances = new int[0];
        return recipe;
    }
}
