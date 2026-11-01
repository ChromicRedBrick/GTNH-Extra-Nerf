package pl.kuba6000.gtnhextranerf.recipes.crafting;

import net.minecraft.item.ItemStack;

/**
 * Immutable crafting ingredient. Implementations must return fresh mutable stacks
 * from probe() and when visiting an item, so definitions can safely share ingredients.
 */
public interface CraftingIngredient {

    static CraftingIngredient item(ItemStack stack) {
        return new ItemCraftingIngredient(stack);
    }

    static CraftingIngredient ore(String name) {
        return new OreCraftingIngredient(name);
    }

    ItemStack probe();

    <T> T accept(Visitor<T> visitor);

    /** Typed conversion boundary; only the native recipe adapter needs {@code Visitor<Object>}. */
    interface Visitor<T> {

        T item(ItemStack stack);

        T ore(String name);
    }
}
