package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProgressionDataTest {

    @ParameterizedTest
    @CsvSource({ "6,luv", "7,zpm", "8,uv", "9,uhv", "10,uev", "11,uiv", "12,umv", "13,uxv", "14,max" })
    void laterVoltageHasItsOwnPersistentPaymentOnlyCertificate(int machineTier, String certificateId) {
        ProgressionTier tier = ProgressionTier.forMachine(machineTier, false);
        assertNotNull(tier);
        assertEquals(certificateId, tier.id());
        assertFalse(tier.hasExam());
        assertSame(tier, ProgressionTier.fromSyncId(ProgressionTier.toSyncId(tier)));

        ProgressionData world = new ProgressionData();
        assertTrue(world.unlock(tier));
        assertFalse(world.unlock(tier));
        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertTrue(reloaded.isUnlocked(tier));
        assertSame(ProgressionTier.STEAM, reloaded.missingForMachine(machineTier, false));
    }

    @Test
    void aBlockedTelemetryWorldStaysBlockedAfterSavingAndLoading() {
        ProgressionData world = new ProgressionData();
        assertFalse(world.isTelemetryBlocked());
        assertTrue(world.blockTelemetry());
        assertFalse(world.blockTelemetry());
        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertTrue(reloaded.isTelemetryBlocked());
        // A world saved before this check existed is not blocked.
        ProgressionData legacy = new ProgressionData();
        legacy.readFromNBT(new NBTTagCompound());
        assertFalse(legacy.isTelemetryBlocked());
    }

    @Test
    void ivRequiresEveryEarlierGateAndRetainsItsCertificationAfterRevocation() {
        ProgressionData world = new ProgressionData();
        world.unlock(ProgressionTier.STEAM);
        world.unlock(ProgressionTier.LV);
        world.unlock(ProgressionTier.IV);
        assertSame(ProgressionTier.MV, world.missingForMachine(5, false));
        world.unlock(ProgressionTier.MV);
        assertSame(ProgressionTier.HV, world.missingForMachine(5, false));
        world.unlock(ProgressionTier.HV);
        assertSame(ProgressionTier.EV, world.missingForMachine(5, false));
        world.unlock(ProgressionTier.EV);
        assertNull(world.missingForMachine(5, false));
        assertSame(ProgressionTier.LUV, world.missingForMachine(8, false));
        world.revoke(ProgressionTier.EV);
        assertTrue(world.isUnlocked(ProgressionTier.IV));
        assertTrue(world.isCurrentCertificate(ProgressionTier.IV, 0));
        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertSame(ProgressionTier.EV, reloaded.missingForMachine(5, false));
        reloaded.unlock(ProgressionTier.EV);
        assertNull(reloaded.missingForMachine(5, false));
    }

    @Test
    void hvMachinesRequireMvEvenWhenHvWasAlreadyEarned() {
        ProgressionData world = new ProgressionData();
        world.unlock(ProgressionTier.STEAM);
        world.unlock(ProgressionTier.LV);
        world.unlock(ProgressionTier.HV);
        assertSame(ProgressionTier.MV, world.missingForMachine(3, false));
        world.unlock(ProgressionTier.MV);
        assertNull(world.missingForMachine(3, false));
        world.revoke(ProgressionTier.MV);
        assertTrue(world.isUnlocked(ProgressionTier.HV));
        assertTrue(world.isCurrentCertificate(ProgressionTier.HV, 0));
        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertSame(ProgressionTier.MV, reloaded.missingForMachine(3, false));
        reloaded.unlock(ProgressionTier.MV);
        assertNull(reloaded.missingForMachine(3, false));
    }

    @Test
    void revokingEarlierCertificatesPreservesLaterPaymentsAcrossReloads() {
        ProgressionData world = new ProgressionData();
        world.unlock(ProgressionTier.STEAM);
        world.unlock(ProgressionTier.LV);
        assertSame(ProgressionTier.MV, world.missingForMachine(2, false));
        world.unlock(ProgressionTier.MV);
        world.unlock(ProgressionTier.HV);
        world.unlock(ProgressionTier.EV);
        world.unlock(ProgressionTier.IV);
        assertNull(world.missingForMachine(5, false));
        world.revoke(ProgressionTier.LV);
        assertTrue(world.isUnlocked(ProgressionTier.MV));
        assertTrue(world.isCurrentCertificate(ProgressionTier.MV, 0));
        assertSame(ProgressionTier.LV, world.missingForMachine(5, false));
        world.revoke(ProgressionTier.STEAM);
        assertSame(ProgressionTier.STEAM, world.missingForMachine(5, false));
        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertTrue(reloaded.isUnlocked(ProgressionTier.MV));
        reloaded.unlock(ProgressionTier.STEAM);
        assertSame(ProgressionTier.LV, reloaded.missingForMachine(2, false));
        reloaded.unlock(ProgressionTier.LV);
        assertNull(reloaded.missingForMachine(2, false));
    }

    @Test
    void steamIsAnIndependentPersistentPrerequisiteForElectricalMachines() {
        ProgressionData world = new ProgressionData();
        assertSame(ProgressionTier.STEAM, world.missingForMachine(1, false));
        world.unlock(ProgressionTier.LV);
        assertSame(ProgressionTier.STEAM, world.missingForMachine(2, false));
        world.unlock(ProgressionTier.STEAM);
        assertNull(world.missingForMachine(1, false));
        assertNull(world.missingForMachine(0, true));
        world.revoke(ProgressionTier.STEAM);
        assertTrue(world.isUnlocked(ProgressionTier.LV));
        assertSame(ProgressionTier.STEAM, world.missingForMachine(2, false));
        assertFalse(world.isCurrentCertificate(ProgressionTier.STEAM, 0));
        assertTrue(world.isCurrentCertificate(ProgressionTier.LV, 0));

        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertSame(ProgressionTier.STEAM, reloaded.missingForMachine(2, false));
        reloaded.unlock(ProgressionTier.STEAM);
        reloaded.revoke(ProgressionTier.LV);
        assertSame(ProgressionTier.LV, reloaded.missingForMachine(2, false));
        assertNull(reloaded.missingForMachine(0, true));
        assertNull(reloaded.missingForMachine(0, false));
    }

    @Test
    void unlockIsPersistedIdempotentAndScopedToOneSave() {
        ProgressionData firstWorld = new ProgressionData();
        assertFalse(firstWorld.isUnlocked(ProgressionTier.LV));
        assertTrue(firstWorld.unlock(ProgressionTier.LV));
        assertTrue(firstWorld.isDirty());
        assertFalse(firstWorld.unlock(ProgressionTier.LV));
        NBTTagCompound saved = new NBTTagCompound();
        firstWorld.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertTrue(reloaded.isUnlocked(ProgressionTier.LV));
        assertFalse(new ProgressionData().isUnlocked(ProgressionTier.LV));
        reloaded.readFromNBT(new NBTTagCompound());
        assertFalse(reloaded.isUnlocked(ProgressionTier.LV));
    }

    @Test
    void revokedCertificationIsSavedAndCanBeEarnedAgain() {
        ProgressionData world = new ProgressionData();
        world.unlock(ProgressionTier.LV);
        world.setDirty(false);

        assertTrue(world.revoke(ProgressionTier.LV));
        assertTrue(world.isDirty());
        assertFalse(world.isUnlocked(ProgressionTier.LV));
        world.setDirty(false);
        assertFalse(world.revoke(ProgressionTier.LV));
        assertFalse(world.isDirty());

        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertFalse(reloaded.isUnlocked(ProgressionTier.LV));
        assertTrue(reloaded.unlock(ProgressionTier.LV));
        assertTrue(reloaded.isUnlocked(ProgressionTier.LV));
    }

    @Test
    void revocationInvalidatesPreviouslyIssuedCertificatesAcrossReloads() {
        ProgressionData world = new ProgressionData();
        long issuedBeforeAccident = world.certificateEpoch(ProgressionTier.LV);
        world.unlock(ProgressionTier.LV);
        world.revoke(ProgressionTier.LV);
        assertFalse(world.isCurrentCertificate(ProgressionTier.LV, issuedBeforeAccident));
        long freshEpoch = world.certificateEpoch(ProgressionTier.LV);
        assertTrue(world.isCurrentCertificate(ProgressionTier.LV, freshEpoch));

        NBTTagCompound saved = new NBTTagCompound();
        world.writeToNBT(saved);
        ProgressionData reloaded = new ProgressionData();
        reloaded.readFromNBT(saved);
        assertFalse(reloaded.isCurrentCertificate(ProgressionTier.LV, issuedBeforeAccident));
        assertTrue(reloaded.isCurrentCertificate(ProgressionTier.LV, freshEpoch));
        reloaded.revoke(ProgressionTier.LV);
        assertEquals(freshEpoch, reloaded.certificateEpoch(ProgressionTier.LV));
    }
}
