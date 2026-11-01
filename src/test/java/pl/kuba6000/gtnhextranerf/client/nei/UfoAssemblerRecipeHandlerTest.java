package pl.kuba6000.gtnhextranerf.client.nei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.IUsageHandler;
import pl.kuba6000.gtnhextranerf.components.ComponentRegistry;
import pl.kuba6000.gtnhextranerf.components.ComponentTier;
import pl.kuba6000.gtnhextranerf.components.ComponentType;
import pl.kuba6000.gtnhextranerf.fabrication.UfoAssemblyRegistry;

class UfoAssemblerRecipeHandlerTest {

    @Test
    void sourceUsagesHideBlueprintsWhileFictionalIngredientsAndTheUfoOpenThem() {
        Map<ComponentType, ItemStack> sources = new EnumMap<>(ComponentType.class);
        for (ComponentType type : ComponentType.values()) sources.put(type, new ItemStack(new Item(), 1, 23));
        ComponentRegistry components = new ComponentRegistry((tier, type) -> sources.get(type));
        UfoAssemblyRegistry fabrication = new UfoAssemblyRegistry();
        UfoAssemblerRecipeHandler handler = new UfoAssemblerRecipeHandler(components, fabrication);

        for (ComponentType type : ComponentType.values()) {
            ItemStack source = sources.get(type)
                .copy();
            source.stackSize = 17;
            IUsageHandler fromSource = handler.getUsageHandler("item", source);
            IUsageHandler fromPart = handler.getUsageHandler("item", fabrication.ingredient(type));
            assertEquals(0, fromSource.numRecipes());
            assertEquals(1, fromPart.numRecipes());
            assertSame(
                components.stack(ComponentTier.UQV, type, 1)
                    .getItem(),
                fromPart.getResultStack(0).item.getItem());
            assertEquals(
                0,
                handler.getRecipeHandler("item", fabrication.ingredient(type))
                    .numRecipes());
        }
        assertEquals(
            ComponentType.values().length,
            handler.getRecipeHandler("item", fabrication.assembly())
                .numRecipes());
        assertEquals(
            ComponentType.values().length,
            handler.getUsageAndCatalystHandler("item", fabrication.assembly())
                .numRecipes());
    }

    @Test
    void eachQuestionableComponentShowsItsOwnSourcePartAndOutput() {
        Map<ComponentType, ItemStack> sources = new EnumMap<>(ComponentType.class);
        for (ComponentType type : ComponentType.values()) sources.put(type, new ItemStack(new Item(), 1, 23));
        ComponentRegistry components = new ComponentRegistry((tier, type) -> sources.get(type));
        UfoAssemblyRegistry fabrication = new UfoAssemblyRegistry();
        UfoAssemblerRecipeHandler handler = new UfoAssemblerRecipeHandler(components, fabrication);

        for (ComponentType type : ComponentType.values()) {
            ItemStack output = components.stack(ComponentTier.UQV, type, 1);
            ICraftingHandler recipes = handler.getRecipeHandler("item", output);
            IUsageHandler usages = handler.getUsageHandler("item", output);
            assertEquals(1, recipes.numRecipes());
            assertEquals(1, usages.numRecipes());
            assertSame(output.getItem(), recipes.getResultStack(0).item.getItem());
            assertSame(output.getItem(), usages.getResultStack(0).item.getItem());
            assertEquals(
                2,
                recipes.getIngredientStacks(0)
                    .size());
            assertSame(
                sources.get(type)
                    .getItem(),
                recipes.getIngredientStacks(0)
                    .get(0).item.getItem());
            assertEquals(
                23,
                recipes.getIngredientStacks(0)
                    .get(0).item.getItemDamage());
            assertSame(
                fabrication.ingredient(type)
                    .getItem(),
                recipes.getIngredientStacks(0)
                    .get(1).item.getItem());
            assertEquals(
                1,
                recipes.getOtherStacks(0)
                    .size());
            assertSame(
                fabrication.assembly()
                    .getItem(),
                recipes.getOtherStacks(0)
                    .get(0).item.getItem());
        }
        ItemStack unrelated = new ItemStack(new Item());
        assertEquals(
            0,
            handler.getRecipeHandler("item", unrelated)
                .numRecipes());
        assertEquals(
            0,
            handler.getUsageHandler("item", unrelated)
                .numRecipes());
        assertEquals(
            0,
            handler.getAllRecipeHandler()
                .numRecipes());
        assertTrue(handler.transferRects.isEmpty());
    }
}
