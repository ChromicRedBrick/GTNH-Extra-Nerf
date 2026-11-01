package pl.kuba6000.gtnhextranerf.mixins.early;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("public-target")
@Mixin(targets = "net.minecraft.item.crafting.IRecipe")
public interface MixinRecipeAccessor {

    @Invoker("getRecipeOutput")
    ItemStack gtnhextranerf$getRecipeOutput();

    @Invoker("matches")
    boolean gtnhextranerf$matches(InventoryCrafting crafting, World world);
}
