package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.GridFlags;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.p2p.PartP2PTunnelME;

@Mixin(value = PartP2PTunnelME.class, remap = false)
public abstract class MixinMeP2PChannelCapacity {

    @Shadow
    @Final
    private AENetworkProxy outerProxy;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void gtnhextranerf$useStandardChannelCapacity(CallbackInfo callback) {
        // Standard AE nodes carry eight channels; keep the restriction on nested ME P2P traffic.
        outerProxy.getFlags()
            .remove(GridFlags.DENSE_CAPACITY);
    }
}
