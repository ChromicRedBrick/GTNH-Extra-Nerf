package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TelemetryEligibilityTest {

    private static final long FIVE_HOURS = 5L * 60 * 60 * 20;

    @Test
    void anyCertificateAboveSteamInAYoungWorldIsTooEarly() {
        for (ProgressionTier tier : ProgressionTier.values()) {
            if (tier == ProgressionTier.STEAM) continue;
            assertTrue(TelemetryEligibility.tooEarly(tier, 0), tier.id());
            assertTrue(TelemetryEligibility.tooEarly(tier, FIVE_HOURS - 1), tier.id());
        }
    }

    @Test
    void steamIsNeverTooEarly() {
        assertFalse(TelemetryEligibility.tooEarly(ProgressionTier.STEAM, 0));
    }

    @Test
    void aWorldThatHasRunFiveHoursMayEarnAnyCertificate() {
        for (ProgressionTier tier : ProgressionTier.values())
            assertFalse(TelemetryEligibility.tooEarly(tier, FIVE_HOURS), tier.id());
        assertEquals(FIVE_HOURS, TelemetryEligibility.MINIMUM_WORLD_TICKS);
    }
}
