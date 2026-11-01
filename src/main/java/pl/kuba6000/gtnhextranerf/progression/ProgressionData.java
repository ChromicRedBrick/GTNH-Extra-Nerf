package pl.kuba6000.gtnhextranerf.progression;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

/** One record in the overworld's MapStorage, shared by every player and dimension. */
public final class ProgressionData extends WorldSavedData {

    public static final String NAME = "gtnhextranerf_progression";
    /** One bit per tier ordinal; the per-tick machine gate reduces to a mask comparison. */
    private int unlockedMask;
    private final Map<ProgressionTier, Long> certificateEpochs = new EnumMap<>(ProgressionTier.class);
    /**
     * Set for good when a certificate above Steam is earned in a very young world; see {@link TelemetryEligibility}.
     */
    private boolean telemetryBlocked;

    public ProgressionData() {
        this(NAME);
    }

    public ProgressionData(String name) {
        super(name);
    }

    public boolean isUnlocked(ProgressionTier tier) {
        return (unlockedMask & tier.bit()) != 0;
    }

    /** Earliest missing prerequisite, independent of the order in which certificates were earned. */
    public ProgressionTier missingForMachine(int machineTier, boolean steam) {
        ProgressionTier required = ProgressionTier.forMachine(machineTier, steam);
        return required == null ? null : missing(required);
    }

    /** Cheap per-tick check; use {@link #missing} only when the missing certificate must be named. */
    public boolean isMissing(ProgressionTier required) {
        int needed = required.requiredMask();
        return (unlockedMask & needed) != needed;
    }

    /** STEAM comes first, then the remaining prerequisites in certificate order. */
    public ProgressionTier missing(ProgressionTier required) {
        int missing = required.requiredMask() & ~unlockedMask;
        if (missing == 0) return null;
        if ((missing & ProgressionTier.STEAM.bit()) != 0) return ProgressionTier.STEAM;
        return ProgressionTier.atIndex(Integer.numberOfTrailingZeros(missing));
    }

    public boolean unlock(ProgressionTier tier) {
        int bit = Objects.requireNonNull(tier)
            .bit();
        if ((unlockedMask & bit) != 0) return false;
        unlockedMask |= bit;
        markDirty();
        return true;
    }

    public boolean revoke(ProgressionTier tier) {
        int bit = Objects.requireNonNull(tier)
            .bit();
        if ((unlockedMask & bit) == 0) return false;
        unlockedMask &= ~bit;
        certificateEpochs.put(tier, certificateEpoch(tier) + 1);
        markDirty();
        return true;
    }

    public boolean isTelemetryBlocked() {
        return telemetryBlocked;
    }

    public boolean blockTelemetry() {
        if (telemetryBlocked) return false;
        telemetryBlocked = true;
        markDirty();
        return true;
    }

    public long certificateEpoch(ProgressionTier tier) {
        return certificateEpochs.getOrDefault(Objects.requireNonNull(tier), 0L);
    }

    public boolean isCurrentCertificate(ProgressionTier tier, long issuedEpoch) {
        return issuedEpoch == certificateEpoch(tier);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        unlockedMask = 0;
        telemetryBlocked = tag.getBoolean("telemetryBlocked");
        certificateEpochs.clear();
        NBTTagCompound tiers = tag.getCompoundTag("certifiedTiers");
        for (ProgressionTier tier : ProgressionTier.values())
            if (tiers.getBoolean(tier.id())) unlockedMask |= tier.bit();
        NBTTagCompound epochs = tag.getCompoundTag("certificateEpochs");
        for (ProgressionTier tier : ProgressionTier.values())
            certificateEpochs.put(tier, Math.max(0, epochs.getLong(tier.id())));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagCompound tiers = new NBTTagCompound();
        for (ProgressionTier tier : ProgressionTier.values()) if (isUnlocked(tier)) tiers.setBoolean(tier.id(), true);
        tag.setTag("certifiedTiers", tiers);
        NBTTagCompound epochs = new NBTTagCompound();
        for (ProgressionTier tier : ProgressionTier.values()) epochs.setLong(tier.id(), certificateEpoch(tier));
        tag.setTag("certificateEpochs", epochs);
        if (telemetryBlocked) tag.setBoolean("telemetryBlocked", true);
    }
}
