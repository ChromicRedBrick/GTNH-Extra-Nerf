package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.logic.ProcessingLogic;
import gregtech.common.tileentities.machines.multi.MTELargeChemicalReactor;

@Mixin(value = MTELargeChemicalReactor.class, remap = false)
public abstract class MixinMTELargeChemicalReactor {

    @ModifyReturnValue(
        method = "createProcessingLogic()Lgregtech/api/logic/ProcessingLogic;",
        at = @At("RETURN"),
        require = 1)
    private ProcessingLogic gtnhextranerf$configureOverclock(ProcessingLogic original) {
        return original.setOverclock(3.0, 4.0);
    }

    @ModifyArg(
        method = "createTooltip()Lgregtech/api/util/MultiblockTooltipBuilder;",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/util/MultiblockTooltipBuilder;addMarkdown(Lnet/minecraft/util/ResourceLocation;)Lgregtech/api/util/MultiblockTooltipBuilder;"),
        index = 0,
        require = 1)
    private ResourceLocation gtnhextranerf$overclockTooltip(ResourceLocation original) {
        return new ResourceLocation("gtnhextranerf", "large-chemical-reactor");
    }
}
