package pl.kuba6000.gtnhextranerf.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Defers loadComplete failures until FML's custom-error catch boundary. Client thread only. */
@SideOnly(Side.CLIENT)
public final class ClientStartupErrors {

    private static StartupErrorRequest pending;

    private ClientStartupErrors() {}

    public static void request(StartupErrorRequest request) {
        if (pending == null || pending.isPreview()) pending = request;
    }

    public static void raisePending() {
        if (pending == null) return;
        StartupErrorRequest request = pending;
        pending = null;
        throw new StartupErrorDisplay(request);
    }
}
