package pl.kuba6000.gtnhextranerf.mixins.early;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.client.FMLClientHandler;
import pl.kuba6000.gtnhextranerf.client.ClientStartupErrors;

/** loadComplete is dispatched inside initializeMods, after FML's last state transition. */
@Mixin(value = FMLClientHandler.class, remap = false)
public abstract class MixinFMLClientHandler {

    @Inject(
        method = "finishMinecraftLoading",
        at = @At(value = "INVOKE", target = "Lcpw/mods/fml/common/Loader;initializeMods()V", shift = At.Shift.AFTER),
        require = 1)
    private void gtnhextranerf$showPendingStartupError(CallbackInfo ci) {
        // Throw within FMLClientHandler's CustomModLoadingErrorDisplayException catch boundary.
        ClientStartupErrors.raisePending();
    }
}
