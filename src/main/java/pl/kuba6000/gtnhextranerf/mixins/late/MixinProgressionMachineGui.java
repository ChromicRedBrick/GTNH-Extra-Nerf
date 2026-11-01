package pl.kuba6000.gtnhextranerf.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.common.gui.modularui.singleblock.base.MTEBasicMachineBaseGui;
import gregtech.common.gui.modularui.singleblock.base.MTETieredMachineBlockBaseGui;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.progression.CertificateStatusPanel;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;

@Mixin(value = MTEBasicMachineBaseGui.class, remap = false)
public abstract class MixinProgressionMachineGui extends MTETieredMachineBlockBaseGui<MTEBasicMachine> {

    protected MixinProgressionMachineGui(MTEBasicMachine machine) {
        super(machine);
    }

    @ModifyReturnValue(method = "createErrorWidget", at = @At("RETURN"), require = 1, allow = 1)
    private Widget<?> gtnhextranerf$certificateStatus(Widget<?> ordinaryError, ModularPanel panel,
        PanelSyncManager syncManager) {
        if (MachineProgression.requiredTier(machine) == null) return ordinaryError;
        return new CertificateStatusPanel(
            ordinaryError,
            syncManager,
            () -> MachineProgression.missingTier(machine),
            tier -> GTNHExtraNerf.proxy.showCertificate(tier));
    }
}
