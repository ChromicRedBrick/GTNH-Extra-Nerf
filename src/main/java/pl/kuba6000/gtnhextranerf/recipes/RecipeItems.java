package pl.kuba6000.gtnhextranerf.recipes;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.Mods;
import gregtech.api.util.GTModHandler;

/** Required item lookups for recipe declarations, with the recipe system's validation failure contract. */
public final class RecipeItems {

    private RecipeItems() {}

    public static ItemStack requireModItem(Mods mod, String name, int amount, int metadata) {
        ItemStack item = GTModHandler.getModItem(mod.ID, name, amount, metadata);
        if (item == null)
            throw new RecipeValidationException("Missing recipe item: " + mod.ID + ":" + name + ":" + metadata);
        return item;
    }
}
