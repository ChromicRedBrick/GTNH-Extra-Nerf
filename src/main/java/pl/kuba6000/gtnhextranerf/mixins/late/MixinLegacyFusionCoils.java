package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.GregTechAPI;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer2;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer3;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.processing.advanced.MTEAdvFusionMk4;
import gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.processing.advanced.MTEAdvFusionMk5;

@Mixin(
    value = { MTEFusionComputer2.class, MTEFusionComputer3.class, MTEAdvFusionMk4.class, MTEAdvFusionMk5.class },
    remap = false)
public abstract class MixinLegacyFusionCoils {

    @ModifyReturnValue(method = "getFusionCoil", at = @At("RETURN"), require = 1, allow = 1)
    private Block gtnhextranerf$isolationCoil(Block original) {
        return GregTechAPI.sBlockCasingsBEC;
    }

    @ModifyReturnValue(method = "getFusionCoilMeta", at = @At("RETURN"), require = 1, allow = 1)
    private int gtnhextranerf$isolationCoilMetadata(int original) {
        return 1;
    }

    @ModifyReturnValue(method = "getCasing", at = @At("RETURN"), require = 1, allow = 1)
    private Block gtnhextranerf$preserveOrdinaryCasing(Block original) {
        // MK4/MK5 use their coil getter to resolve the ordinary casing block too.
        if ((Object) this instanceof MTEAdvFusionMk4) return ModBlocks.blockCasings3Misc;
        if ((Object) this instanceof MTEAdvFusionMk5) return ModBlocks.blockCasings6Misc;
        return original;
    }

}
