package pl.kuba6000.gtnhextranerf.mixins.early;

import java.util.List;

import net.minecraft.item.crafting.CraftingManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingManager.class)
public interface MixinCraftingManagerAccessor {

    @Accessor("recipes")
    List<Object> gtnhextranerf$getRecipes();
}
