package pl.kuba6000.gtnhextranerf.mixins.late;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import pl.kuba6000.gtnhextranerf.progression.CertificateStatusWidget;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;
import pl.kuba6000.gtnhextranerf.progression.ProgressionMachine;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

@Mixin(value = MTEBasicMachine.class, remap = false)
public abstract class MixinProgressionMachineStatus implements ProgressionMachine {

    @Unique
    private int gtnhextranerf$missingCertificate;

    @Override
    public ProgressionTier requiredCertificate() {
        MTEBasicMachine machine = (MTEBasicMachine) (Object) this;
        return machine.getBaseMetaTileEntity() != null && machine.getBaseMetaTileEntity()
            .isClientSide() ? ProgressionTier.fromSyncId(gtnhextranerf$missingCertificate)
                : MachineProgression.missingTier(machine);
    }

    @Override
    public boolean isCertificationMissing() {
        return requiredCertificate() != null;
    }

    @Redirect(
        method = "createErrorStatusArea",
        at = @At(value = "NEW", target = "com/gtnewhorizons/modularui/common/widget/DrawableWidget"),
        require = 1,
        allow = 1)
    private DrawableWidget gtnhextranerf$clickableStatus() {
        return new CertificateStatusWidget(this);
    }

    @Inject(method = "createErrorStatusArea", at = @At("RETURN"), require = 1, allow = 1)
    private void gtnhextranerf$syncCertificate(ModularWindow.Builder builder, IDrawable picture,
        CallbackInfoReturnable<DrawableWidget> cir) {
        if (MachineProgression.requiredTier((MTEBasicMachine) (Object) this) == null) return;
        ((CertificateStatusWidget) cir.getReturnValue()).setCertificateDrawable(picture);
        cir.getReturnValue()
            .attachSyncer(
                new FakeSyncWidget.IntegerSyncer(
                    () -> ProgressionTier.toSyncId(MachineProgression.missingTier((MTEBasicMachine) (Object) this)),
                    value -> gtnhextranerf$missingCertificate = value),
                builder,
                (widget, value) -> widget.notifyTooltipChange());
    }

    @ModifyReturnValue(
        method = { "getErrorDescriptions", "getErrorDescriptionsShift" },
        at = @At("RETURN"),
        require = 2,
        allow = 2)
    private List<String> gtnhextranerf$certificateMessage(List<String> original) {
        if (!isCertificationMissing()) return original;
        List<String> result = new ArrayList<>();
        result
            .add(StatCollector.translateToLocal("gtnhextranerf.progression." + requiredCertificate().id() + ".error"));
        result.addAll(original);
        return result;
    }
}
