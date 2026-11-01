package pl.kuba6000.gtnhextranerf.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TelemetryCommandTest {

    @Test
    void integratedServerOwnerMayUseTheCommandWithoutCheats() {
        assertTrue(TelemetryCommand.isIntegratedOwner(false, "Steve", "Steve"));
        assertTrue(TelemetryCommand.isIntegratedOwner(false, "Steve", "steve"));
    }

    @Test
    void lanGuestsAndDedicatedServersStillNeedOperatorPermission() {
        assertFalse(TelemetryCommand.isIntegratedOwner(false, "Steve", "Alex"));
        assertFalse(TelemetryCommand.isIntegratedOwner(true, "Steve", "Steve"));
        assertFalse(TelemetryCommand.isIntegratedOwner(false, null, "Steve"));
    }
}
