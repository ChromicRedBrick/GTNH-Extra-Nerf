package pl.kuba6000.gtnhextranerf.mixins.support;

public final class GeneratorEfficiency {

    private static final int BASE_EFFICIENCY = 10;
    private static final int EARLY_TIER_GAIN = 10;
    private static final int LATE_TIER_GAIN = 5;
    private static final int EARLY_TIER_COUNT = 3;

    private GeneratorEfficiency() {}

    public static int forTier(int machineTier, int firstTier) {
        int progressionTier = machineTier - firstTier + 1;
        return BASE_EFFICIENCY + EARLY_TIER_GAIN * Math.min(progressionTier, EARLY_TIER_COUNT)
            + LATE_TIER_GAIN * Math.max(0, progressionTier - EARLY_TIER_COUNT);
    }
}
