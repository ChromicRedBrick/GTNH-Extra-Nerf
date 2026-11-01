package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.common.tileentities.machines.multi.purification.MTEPurificationUnitOzonation;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

@Mixin(value = MTEPurificationUnitOzonation.class, remap = false)
public abstract class MixinOzonationSafety implements ProgressionMachineNerf.OzoneSafetyHook {

    @ModifyExpressionValue(
        method = { "checkProcessing", "createTooltip" },
        at = @At(
            value = "FIELD",
            target = "Lgregtech/common/tileentities/machines/multi/purification/MTEPurificationUnitOzonation;MAX_OZONE_GAS_FOR_EXPLOSION:I"),
        require = 2,
        allow = 2)
    private int gtnhextranerf$localOzoneLimit(int original) {
        return ProgressionMachineNerf.OZONE_LIMIT;
    }

    @ModifyArg(
        method = "createTooltip()Lgregtech/api/util/MultiblockTooltipBuilder;",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/MultiblockTooltipBuilder;addMarkdown(Lnet/minecraft/util/ResourceLocation;Ljava/util/Map;)Lgregtech/api/util/MultiblockTooltipBuilder;"),
        index = 0,
        require = 1)
    private ResourceLocation gtnhextranerf$ozoneVariantTooltip(ResourceLocation original) {
        return new ResourceLocation("gtnhextranerf", "purification-unit-ozonation");
    }
}
