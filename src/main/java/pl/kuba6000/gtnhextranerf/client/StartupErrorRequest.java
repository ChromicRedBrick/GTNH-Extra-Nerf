package pl.kuba6000.gtnhextranerf.client;

import java.util.Objects;
import java.util.Optional;

import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Screen content and intent; independent of Minecraft GUI classes. */
public final class StartupErrorRequest {

    public static final String DEV_PREVIEW_PROPERTY = "gtnhextranerf.forceAuditErrorScreen";
    private final Throwable cause;
    private final boolean preview;

    private StartupErrorRequest(Throwable cause, boolean preview) {
        this.cause = Objects.requireNonNull(cause, "cause");
        this.preview = preview;
    }

    public static StartupErrorRequest failure(Throwable cause) {
        return new StartupErrorRequest(cause, false);
    }

    public static Optional<StartupErrorRequest> developmentPreview(boolean development, boolean requested) {
        return development && requested
            ? Optional.of(
                new StartupErrorRequest(new IllegalStateException("Development error screen preview requested"), true))
            : Optional.empty();
    }

    public String supportMessage() {
        return "Extra Nerf is supported only on GT New Horizons (GTNH) " + GTNHExtraNerf.SUPPORTED_GTNH_VERSION + ".";
    }

    public Throwable cause() {
        return cause;
    }

    public boolean isPreview() {
        return preview;
    }
}
