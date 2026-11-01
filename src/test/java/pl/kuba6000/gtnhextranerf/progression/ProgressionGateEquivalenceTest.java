package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** The optimized gate must answer exactly like the original loop-based definition. */
class ProgressionGateEquivalenceTest {

    /** Minimum machine tier per ProgressionTier ordinal: LV, STEAM, MV, HV, ... MAX. */
    private static final int[] MINIMUM = { 1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

    private static ProgressionTier referenceForMachine(int tier, boolean steam) {
        if (steam) return ProgressionTier.STEAM;
        ProgressionTier required = null;
        for (ProgressionTier candidate : ProgressionTier.values()) {
            if (candidate != ProgressionTier.STEAM && tier >= MINIMUM[candidate.ordinal()]
                && (required == null || MINIMUM[candidate.ordinal()] > MINIMUM[required.ordinal()]))
                required = candidate;
        }
        return required;
    }

    private static boolean referenceRequires(ProgressionTier tier, ProgressionTier prerequisite) {
        return tier == ProgressionTier.STEAM ? prerequisite == ProgressionTier.STEAM
            : MINIMUM[prerequisite.ordinal()] <= MINIMUM[tier.ordinal()];
    }

    private static ProgressionTier referenceMissing(boolean[] unlocked, int machineTier, boolean steam) {
        ProgressionTier required = referenceForMachine(machineTier, steam);
        if (required == null) return null;
        if (!unlocked[ProgressionTier.STEAM.ordinal()]) return ProgressionTier.STEAM;
        for (ProgressionTier prerequisite : ProgressionTier.values())
            if (referenceRequires(required, prerequisite) && !unlocked[prerequisite.ordinal()]) return prerequisite;
        return null;
    }

    @Test
    void machineTierMapsToTheSameCertificate() {
        for (int tier = -3; tier <= 25; tier++) for (boolean steam : new boolean[] { false, true })
            assertSame(referenceForMachine(tier, steam), ProgressionTier.forMachine(tier, steam), tier + "/" + steam);
    }

    @Test
    void everyUnlockedSetReportsTheSameEarliestMissingCertificate() {
        ProgressionTier[] tiers = ProgressionTier.values();
        for (int subset = 0; subset < 1 << tiers.length; subset++) {
            ProgressionData data = new ProgressionData();
            boolean[] unlocked = new boolean[tiers.length];
            for (int i = 0; i < tiers.length; i++) {
                unlocked[i] = (subset & 1 << i) != 0;
                if (unlocked[i]) data.unlock(tiers[i]);
            }
            for (int machineTier = -1; machineTier <= 20; machineTier++)
                for (boolean steam : new boolean[] { false, true }) {
                    ProgressionTier expected = referenceMissing(unlocked, machineTier, steam);
                    String context = "subset=" + subset + " tier=" + machineTier + " steam=" + steam;
                    assertSame(expected, data.missingForMachine(machineTier, steam), context);
                    ProgressionTier required = ProgressionTier.forMachine(machineTier, steam);
                    if (required != null) {
                        assertEquals(expected != null, data.isMissing(required), context);
                        assertSame(expected, data.missing(required), context);
                    }
                }
        }
    }
}
