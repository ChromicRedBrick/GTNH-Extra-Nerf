package pl.kuba6000.gtnhextranerf.recipes.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import pl.kuba6000.gtnhextranerf.mixins.early.MixinRecipeAccessor;

/** Typed handle; Minecraft's IRecipe stays behind the descriptor-safe accessor boundary. */
public final class CraftingRecipe {

    private final Object handle;
    private final ItemStack output;

    CraftingRecipe(Object handle) {
        this(handle, ((MixinRecipeAccessor) handle).gtnhextranerf$getRecipeOutput());
    }

    private CraftingRecipe(Object handle, ItemStack output) {
        this.handle = handle;
        this.output = output == null ? null : output.copy();
    }

    public ItemStack output() {
        return output == null ? null : output.copy();
    }

    public CraftingRecipe withOutputAmount(int amount) {
        if (amount <= 0 || output == null) throw new IllegalArgumentException("A positive recipe output is required");
        ItemStack replacement = output.copy();
        replacement.stackSize = amount;
        return new CraftingRecipe(handle, replacement);
    }

    Object handle() {
        return handle;
    }

    boolean matches(InventoryCrafting inventory) {
        return ((MixinRecipeAccessor) handle).gtnhextranerf$matches(inventory, null);
    }

    boolean hasCurrentOutput() {
        return ItemStack.areItemStacksEqual(output, ((MixinRecipeAccessor) handle).gtnhextranerf$getRecipeOutput());
    }

    void publishOutput() {
        if (output == null) return;
        ItemStack actual = ((MixinRecipeAccessor) handle).gtnhextranerf$getRecipeOutput();
        if (actual == null) throw new IllegalStateException("Crafting recipe output disappeared");
        actual.stackSize = output.stackSize;
        if (((MixinRecipeAccessor) handle).gtnhextranerf$getRecipeOutput().stackSize != output.stackSize) {
            throw new IllegalStateException("Crafting recipe did not accept its output amount");
        }
    }
}
