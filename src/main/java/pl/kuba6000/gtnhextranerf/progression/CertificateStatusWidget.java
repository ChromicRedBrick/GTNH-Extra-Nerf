package pl.kuba6000.gtnhextranerf.progression;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Reuses GT's existing power-failure icon, position, tooltip and syncers. */
public final class CertificateStatusWidget extends DrawableWidget implements Interactable {

    private static final UITexture CERTIFICATE_WARNING = UITexture
        .fullImage(GTNHExtraNerf.MODID, "items/progression_certificate_warning");
    private static final UITexture STEAM_WARNING = UITexture
        .fullImage(GTNHExtraNerf.MODID, "items/steam_certificate_warning");

    private final ProgressionMachine machine;

    public CertificateStatusWidget(ProgressionMachine machine) {
        this.machine = machine;
    }

    public void setCertificateDrawable(IDrawable ordinaryError) {
        setDrawable(
            () -> !machine.isCertificationMissing() ? ordinaryError
                : machine.requiredCertificate() == ProgressionTier.STEAM ? STEAM_WARNING : CERTIFICATE_WARNING);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ClickResult onClick(int buttonId, boolean doubleClick) {
        if (buttonId != 0 || !machine.isCertificationMissing() || machine.requiredCertificate() == null)
            return ClickResult.IGNORE;
        GTNHExtraNerf.proxy.showCertificate(machine.requiredCertificate());
        return ClickResult.ACCEPT;
    }
}
