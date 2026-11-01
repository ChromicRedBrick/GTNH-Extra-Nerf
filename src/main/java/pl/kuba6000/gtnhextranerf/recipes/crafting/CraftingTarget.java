package pl.kuba6000.gtnhextranerf.recipes.crafting;

import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import gregtech.api.util.GTUtility;

/** An actual 3x3 crafting probe, its exact output and optionally an upstream recipe type. */
final class CraftingTarget implements Predicate<CraftingRecipe> {

    private final ItemStack[] probe;
    private final ItemStack output;
    private final CraftingRecipeType type;

    private CraftingTarget(ItemStack[] probe, ItemStack output, CraftingRecipeType type) {
        if (probe == null || probe.length != 9 || output == null) {
            throw new IllegalArgumentException("A crafting target needs a 3x3 probe and an output");
        }
        this.probe = new ItemStack[9];
        for (int i = 0; i < 9; i++) this.probe[i] = probe[i] == null ? null : probe[i].copy();
        this.output = output.copy();
        this.type = type;
    }

    static CraftingTarget matching(ItemStack[] probe, ItemStack output) {
        return new CraftingTarget(probe, output, null);
    }

    CraftingTarget ofType(CraftingRecipeType type) {
        return new CraftingTarget(probe, output, type);
    }

    @Override
    public boolean test(CraftingRecipe recipe) {
        ItemStack actual = recipe.output();
        if (actual == null || !GTUtility.areStacksEqual(actual, output, true)
            || actual.getItemDamage() != output.getItemDamage()
            || actual.stackSize != output.stackSize
            || (type != null && !type.matches(recipe))) return false;
        InventoryCrafting inventory = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer player) {
                return false;
            }
        }, 3, 3);
        for (int i = 0; i < probe.length; i++)
            inventory.setInventorySlotContents(i, probe[i] == null ? null : probe[i].copy());
        return recipe.matches(inventory);
    }
}
