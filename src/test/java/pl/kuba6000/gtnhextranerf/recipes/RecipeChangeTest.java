package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;
import org.objenesis.ObjenesisStd;

import gregtech.api.util.GTRecipe;
import pl.kuba6000.gtnhextranerf.recipes.machine.RecipeChange;

class RecipeChangeTest {

    @Test
    void energyChangeIsReusableAndPreservesDurationAndIngredients() {
        RecipeChange change = RecipeChange.builder()
            .eut(1920L)
            .build();
        ItemStack input = new ItemStack(new Item(), 10);
        GTRecipe recipe = new ObjenesisStd().newInstance(GTRecipe.class);
        recipe.mInputs = new ItemStack[] { input };
        recipe.mDuration = 960;
        recipe.mEUt = 480;
        change.accept(recipe);
        assertEquals(1920, recipe.mEUt);
        assertEquals(960, recipe.mDuration);
        org.junit.jupiter.api.Assertions.assertSame(input, recipe.mInputs[0]);
        change.accept(recipe);
        assertEquals(1920, recipe.mEUt);
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .eut(0));
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .eut(-1));
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .eut((long) Integer.MAX_VALUE + 1));
    }

    @Test
    void rejectsEmptyChangesAndInvalidStackAmountsAtDeclaration() {
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .build());
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .itemOutputs(new ItemStack(new Item(), 0)));
        assertThrows(
            IllegalArgumentException.class,
            () -> RecipeChange.builder()
                .itemInputs(new ItemStack(new Item(), -1)));
    }

    @Test
    void repeatedOutputsRequireAnExplicitCountBeforeAnyOfThemChange() {
        ItemStack target = new ItemStack(new Item(), 64);
        GTRecipe recipe = new ObjenesisStd().newInstance(GTRecipe.class);
        recipe.mOutputs = new ItemStack[] { target.copy(), target.copy(), target.copy() };
        RecipeChange change = RecipeChange.builder()
            .itemOutputAmounts(target, 3, 32)
            .build();
        change.accept(recipe);
        for (ItemStack output : recipe.mOutputs) assertEquals(32, output.stackSize);
        recipe.mOutputs = new ItemStack[] { target.copy(), target.copy() };
        assertThrows(IllegalStateException.class, () -> change.accept(recipe));
        assertEquals(64, recipe.mOutputs[0].stackSize);
    }

    @Test
    void inputAmountsKeepAlternativesConsistentAndChangesCanBeReused() {
        ItemStack target = new ItemStack(new Item(), 2);
        ItemStack alternative = new ItemStack(new Item(), 4);
        RecipeChange change = RecipeChange.builder()
            .itemInputAmount(target, 6)
            .build();
        GTRecipe.GTRecipe_WithAlt recipe = new ObjenesisStd().newInstance(GTRecipe.GTRecipe_WithAlt.class);
        recipe.mInputs = new ItemStack[] { target.copy() };
        recipe.mOreDictAlt = new ItemStack[][] { { alternative.copy() } };
        change.accept(recipe);
        assertEquals(6, recipe.mInputs[0].stackSize);
        assertEquals(12, recipe.mOreDictAlt[0][0].stackSize);
        recipe.mInputs = new ItemStack[] { target.copy() };
        recipe.mOreDictAlt = null;
        target.stackSize = 99;
        change.accept(recipe);
        assertEquals(6, recipe.mInputs[0].stackSize);
        recipe.mInputs = new ItemStack[0];
        assertThrows(IllegalStateException.class, () -> change.accept(recipe));
    }

    @Test
    void editsTheNamedOutputRegardlessOfItsPositionAndPreservesOtherChances() {
        ItemStack target = new ItemStack(new Item(), 2);
        ItemStack other = new ItemStack(new Item(), 3);
        RecipeChange change = RecipeChange.builder()
            .itemOutputAmount(target, 5)
            .outputChance(target, 2500)
            .build();
        GTRecipe recipe = new ObjenesisStd().newInstance(GTRecipe.class);
        recipe.mOutputs = new ItemStack[] { other.copy(), target.copy() };
        recipe.mOutputChances = new int[] { 7500, 5000 };

        change.accept(recipe);

        assertEquals(3, recipe.mOutputs[0].stackSize);
        assertEquals(7500, recipe.mOutputChances[0]);
        assertEquals(5, recipe.mOutputs[1].stackSize);
        assertEquals(2500, recipe.mOutputChances[1]);
        recipe.mOutputs = new ItemStack[] { target.copy(), target.copy() };
        assertThrows(IllegalStateException.class, () -> change.accept(recipe));
    }
}
