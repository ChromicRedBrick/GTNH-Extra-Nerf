package pl.kuba6000.gtnhextranerf.progression;

/** Stable certificate identities; machine requirements can include earlier stages. */
public enum ProgressionTier {

    LV(1, "lv", 1),
    STEAM(0, "steam", 2),
    MV(2, "mv", 3),
    HV(3, "hv", 4),
    EV(4, "ev", 5),
    IV(5, "iv", 6),
    LUV(6, "luv", 7),
    ZPM(7, "zpm", 8),
    UV(8, "uv", 9),
    UHV(9, "uhv", 10),
    UEV(10, "uev", 11),
    UIV(11, "uiv", 12),
    UMV(12, "umv", 13),
    UXV(13, "uxv", 14),
    MAX(14, "max", 15);

    private static final ProgressionTier[] VALUES = values();
    /** Highest-requirement non-steam certificate for each machine tier, so the per-tick gate needs no loop. */
    private static final ProgressionTier[] BY_MACHINE_TIER = new ProgressionTier[MAX.minimumMachineTier + 1];
    /** Bit set of every certificate a tier requires, indexed by ordinal. */
    private static final int[] REQUIRED_MASK = new int[VALUES.length];

    static {
        for (int machineTier = 0; machineTier < BY_MACHINE_TIER.length; machineTier++) {
            ProgressionTier required = null;
            for (ProgressionTier candidate : VALUES) {
                if (candidate != STEAM && machineTier >= candidate.minimumMachineTier
                    && (required == null || candidate.minimumMachineTier > required.minimumMachineTier))
                    required = candidate;
            }
            BY_MACHINE_TIER[machineTier] = required;
        }
        for (ProgressionTier tier : VALUES) for (ProgressionTier prerequisite : VALUES)
            if (tier.requires(prerequisite)) REQUIRED_MASK[tier.ordinal()] |= prerequisite.bit();
    }

    private final int minimumMachineTier;
    private final String id;
    private final int syncId;

    ProgressionTier(int machineTier, String id, int syncId) {
        this.minimumMachineTier = machineTier;
        this.id = id;
        this.syncId = syncId;
    }

    public String id() {
        return id;
    }

    public String certificateTexture() {
        return this == STEAM ? "steam_certificate" : "progression_certificate";
    }

    public boolean hasExam() {
        return this == STEAM || this == LV;
    }

    public boolean requires(ProgressionTier prerequisite) {
        return this == STEAM ? prerequisite == STEAM : prerequisite.minimumMachineTier <= minimumMachineTier;
    }

    int bit() {
        return 1 << ordinal();
    }

    /** Bits of every certificate this tier requires, including itself. */
    int requiredMask() {
        return REQUIRED_MASK[ordinal()];
    }

    static ProgressionTier atIndex(int index) {
        return VALUES[index];
    }

    public static int toSyncId(ProgressionTier tier) {
        return tier == null ? 0 : tier.syncId;
    }

    public static ProgressionTier fromSyncId(int id) {
        for (ProgressionTier tier : values()) if (tier.syncId == id) return tier;
        return null;
    }

    public static ProgressionTier forMachine(int tier, boolean steam) {
        if (steam) return STEAM;
        if (tier < 0) return null;
        return BY_MACHINE_TIER[Math.min(tier, BY_MACHINE_TIER.length - 1)];
    }
}
