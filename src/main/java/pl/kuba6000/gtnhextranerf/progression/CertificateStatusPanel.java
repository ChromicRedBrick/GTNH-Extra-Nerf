package pl.kuba6000.gtnhextranerf.progression;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.util.StatCollector;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import gregtech.api.metatileentity.BaseTileEntity;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** MUI2 error slot. The client renders a received value and never queries world certification. */
public final class CertificateStatusPanel extends ParentWidget<CertificateStatusPanel> {

    public static final String SYNC_ID = "gtnhextranerf.certificate_missing";
    private static final UITexture WARNING = UITexture
        .fullImage(GTNHExtraNerf.MODID, "items/progression_certificate_warning");
    private static final UITexture STEAM_WARNING = UITexture
        .fullImage(GTNHExtraNerf.MODID, "items/steam_certificate_warning");

    public CertificateStatusPanel(Widget<?> ordinaryError, PanelSyncManager syncManager,
        Supplier<ProgressionTier> serverMissing, Consumer<ProgressionTier> openPage) {
        IntSyncValue missing = new IntSyncValue(
            () -> syncManager.isClient() ? 0 : ProgressionTier.toSyncId(serverMissing.get())).allowC2S(false);
        syncManager.syncValue(SYNC_ID, missing);
        ParentWidget<?> ordinaryHost = new ParentWidget<>().size(18)
            .child(ordinaryError);
        ButtonWidget<?> certificate = new ButtonWidget<>().size(18)
            .background(IDrawable.EMPTY)
            .disableThemeBackground(true)
            .disableHoverThemeBackground(true)
            .disableHoverBackground()
            .overlay(
                new DynamicDrawable(
                    () -> ProgressionTier.fromSyncId(missing.getIntValue()) == ProgressionTier.STEAM ? STEAM_WARNING
                        : WARNING))
            .tooltipShowUpTimer(BaseTileEntity.TOOLTIP_DELAY)
            .tooltipBuilder(tooltip -> {
                ProgressionTier tier = ProgressionTier.fromSyncId(missing.getIntValue());
                if (tier != null) tooltip
                    .addLine(StatCollector.translateToLocal("gtnhextranerf.progression." + tier.id() + ".error"));
            })
            .onMousePressed(button -> {
                ProgressionTier tier = ProgressionTier.fromSyncId(missing.getIntValue());
                if (button != 0 || !syncManager.isClient() || tier == null) return false;
                openPage.accept(tier);
                return true;
            });
        Runnable updateVisibility = () -> {
            boolean locked = ProgressionTier.fromSyncId(missing.getIntValue()) != null;
            ordinaryHost.setEnabled(!locked);
            certificate.setEnabled(locked);
            certificate.markTooltipDirty();
        };
        missing.changeListener(updateVisibility);
        updateVisibility.run();
        size(18);
        child(ordinaryHost);
        child(certificate);
    }
}
