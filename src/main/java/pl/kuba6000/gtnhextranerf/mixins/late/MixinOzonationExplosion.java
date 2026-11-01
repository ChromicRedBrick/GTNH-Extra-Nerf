package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;

@Mixin(value = MTEMultiBlockBase.class, remap = false)
public abstract class MixinOzonationExplosion implements ProgressionMachineNerf.OzoneExplosionHook {

    @Inject(method = "explodeMultiblock", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void gtnhextranerf$skipRepeatedOzoneExplosion(CallbackInfo callback) {
        if ((Object) this instanceof ProgressionMachineNerf.OzoneSafetyHook
            && ((MTEMultiBlockBase) (Object) this).getBaseMetaTileEntity() == null) callback.cancel();
    }

    @ModifyExpressionValue(
        method = "explodeMultiblock",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/Iterables;concat([Ljava/lang/Iterable;)Ljava/lang/Iterable;"),
        require = 1,
        allow = 1)
    private Iterable<MetaTileEntity> gtnhextranerf$stableExplosionHatches(Iterable<MetaTileEntity> original) {
        // World removal callbacks mutate the live hatch lists while the native explosion iterates them.
        return (Object) this instanceof ProgressionMachineNerf.OzoneSafetyHook ? Lists.newArrayList(original)
            : original;
    }

    @WrapOperation(
        method = "explodeMultiblock",
        at = @At(value = "INVOKE", target = "Lgregtech/api/interfaces/tileentity/IGregTechTileEntity;doExplosion(J)V"),
        require = 2,
        allow = 2)
    private void gtnhextranerf$skipAlreadyDestroyedTile(IGregTechTileEntity tile, long power,
        Operation<Void> original) {
        // A hatch's terrain explosion can already remove the controller before the native final call.
        if (tile != null || !((Object) this instanceof ProgressionMachineNerf.OzoneSafetyHook))
            original.call(tile, power);
    }
}
