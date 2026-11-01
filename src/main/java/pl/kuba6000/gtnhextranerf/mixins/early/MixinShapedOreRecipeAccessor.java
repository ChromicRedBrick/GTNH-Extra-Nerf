package pl.kuba6000.gtnhextranerf.mixins.early;

import net.minecraftforge.oredict.ShapedOreRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedOreRecipe.class)
public interface MixinShapedOreRecipeAccessor {

    @Accessor(value = "mirrored", remap = false)
    void gtnhextranerf$setMirrored(boolean mirrored);
}
