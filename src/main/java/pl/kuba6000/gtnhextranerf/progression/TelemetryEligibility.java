package pl.kuba6000.gtnhextranerf.progression;

/**
 * A world that earns a certificate above Steam before it is a few hours old is a test, creative or cheat world, not
 * real progress, so telemetry is turned off for that world for good.
 */
public final class TelemetryEligibility {

    /** Server ticks the world must have run before any certificate above Steam: 5 hours. */
    public static final long MINIMUM_WORLD_TICKS = 5L * 60 * 60 * 20;

    private TelemetryEligibility() {}

    /** @param worldTicks the overworld's total world time, which {@code /time set} does not change */
    public static boolean tooEarly(ProgressionTier tier, long worldTicks) {
        return tier != ProgressionTier.STEAM && worldTicks < MINIMUM_WORLD_TICKS;
    }
}
