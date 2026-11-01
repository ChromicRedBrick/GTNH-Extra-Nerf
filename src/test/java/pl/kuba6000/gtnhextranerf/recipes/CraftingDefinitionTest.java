package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingDefinition;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingIngredient;
import pl.kuba6000.gtnhextranerf.recipes.crafting.CraftingRecipeBuilder;

class CraftingDefinitionTest {

    @Test
    void universalShapelessSupportsIndependentIngredientProbesAndRejectsPatterns() {
        ItemStack ingredient = new ItemStack(new Item());
        CraftingDefinition definition = CraftingRecipeBuilder.universalShapeless()
            .inputs(ingredient, ingredient)
            .output(new ItemStack(new Item()))
            .build();
        ItemStack[] probe = definition.probe();
        assertSame(ingredient.getItem(), probe[0].getItem());
        assertSame(ingredient.getItem(), probe[1].getItem());
        assertNull(probe[2]);
        probe[0].stackSize = 9;
        assertEquals(1, definition.probe()[0].stackSize);
        assertThrows(
            IllegalArgumentException.class,
            () -> definition.toBuilder()
                .pattern("AA"));
        assertThrows(
            IllegalArgumentException.class,
            () -> definition.toBuilder()
                .removable(false));
    }

    @Test
    void typedIngredientsCanBeReusedWithoutSharingMutableStacks() {
        ItemStack stack = new ItemStack(new Item());
        CraftingIngredient ingredient = CraftingIngredient.item(stack);
        CraftingDefinition shaped = CraftingRecipeBuilder.shaped()
            .pattern("A")
            .ingredient('A', ingredient)
            .output(new ItemStack(new Item()))
            .build();
        CraftingDefinition shapeless = CraftingRecipeBuilder.shapeless()
            .ingredients(ingredient, ingredient)
            .output(new ItemStack(new Item()))
            .build();
        stack.stackSize = 42;
        ingredient.probe().stackSize = 99;
        assertEquals(1, shaped.probe()[0].stackSize);
        ItemStack[] probe = shapeless.probe();
        probe[0].stackSize = 7;
        assertEquals(1, probe[1].stackSize);
        assertEquals(1, shapeless.probe()[0].stackSize);
    }

    @Test
    void derivesIndependentProbesFromThePatternAndChangedBindings() {
        ItemStack ingredient = new ItemStack(new Item());
        ItemStack replacement = new ItemStack(new Item());
        CraftingDefinition original = CraftingRecipeBuilder.shaped()
            .pattern(" A ", "AAA", " A ")
            .ingredient('A', ingredient)
            .output(new ItemStack(new Item()))
            .build();
        CraftingDefinition changed = original.toBuilder()
            .ingredient('A', replacement)
            .build();
        ingredient.stackSize = 42;
        ItemStack[] probe = original.probe();
        assertNull(probe[0]);
        assertEquals(1, probe[1].stackSize);
        assertSame(ingredient.getItem(), probe[3].getItem());
        assertSame(replacement.getItem(), changed.probe()[3].getItem());
        probe[1].stackSize = 99;
        assertEquals(1, original.probe()[1].stackSize);
        assertThrows(
            IllegalArgumentException.class,
            () -> original.toBuilder()
                .pattern("B")
                .build());
    }
}
