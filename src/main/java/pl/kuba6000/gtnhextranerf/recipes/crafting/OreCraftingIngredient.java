package pl.kuba6000.gtnhextranerf.recipes.crafting;

import net.minecraft.item.ItemStack;

import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

final class OreCraftingIngredient implements CraftingIngredient {

    private final String name;

    OreCraftingIngredient(String name) {
        if (name == null || name.trim()
            .isEmpty()) throw new IllegalArgumentException("Expected an OreDict name");
        this.name = name;
    }

    @Override
    public ItemStack probe() {
        ItemStack stack = GTOreDictUnificator.getFirstOre(name, 1);
        if (stack == null) throw new RecipeValidationException("Could not resolve crafting ingredient " + name);
        return stack.copy();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.ore(name);
    }
}
