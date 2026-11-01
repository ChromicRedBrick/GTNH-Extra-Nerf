package pl.kuba6000.gtnhextranerf.recipes.crafting;

import net.minecraft.item.ItemStack;

final class ItemCraftingIngredient implements CraftingIngredient {

    private final ItemStack stack;

    ItemCraftingIngredient(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0)
            throw new IllegalArgumentException("Expected a nonempty crafting ingredient");
        this.stack = stack.copy();
    }

    @Override
    public ItemStack probe() {
        return stack.copy();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.item(stack.copy());
    }
}
