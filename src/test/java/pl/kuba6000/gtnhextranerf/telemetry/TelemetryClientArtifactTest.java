package pl.kuba6000.gtnhextranerf.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TelemetryClientArtifactTest {

    @TempDir
    Path directory;

    private final ServerTelemetry.Snapshot snapshot = new ServerTelemetry.Snapshot(UUID.randomUUID(), 2, "lv", false);

    private JsonObject report(TelemetryClient client) {
        return new JsonParser().parse(client.encode(snapshot, () -> false))
            .getAsJsonObject();
    }

    @Test
    void missingUnreadableOrDirectoryArtifactDoesNotPreventReporting() throws Exception {
        for (Path source : new Path[] { null, directory, directory.resolve("missing.jar") }) {
            TelemetryClient client = new TelemetryClient(source, new URL("http://127.0.0.1:1"), new SecureRandom());
            JsonObject payload = report(client);
            assertTrue(
                payload.get("jarProof")
                    .isJsonNull());
            assertEquals(
                2,
                payload.get("onlinePlayers")
                    .getAsInt());
            assertFalse(
                payload.get("singleplayer")
                    .getAsBoolean());
        }
    }

    @Test
    void artifactIsReadOnceButProofUsesAFreshSaltForEachReport() throws Exception {
        Path artifact = directory.resolve("release.jar");
        try (java.io.InputStream input = getClass().getResourceAsStream("/telemetry/release.jar")) {
            Files.copy(input, artifact);
        }
        TelemetryClient client = new TelemetryClient(artifact, new URL("http://127.0.0.1:1"), new SecureRandom());
        JsonObject first = report(client).getAsJsonObject("jarProof");
        Files.delete(artifact);
        JsonObject second = report(client).getAsJsonObject("jarProof");
        assertNotEquals(first.get("salt"), second.get("salt"));
        assertNotEquals(first.get("sha256"), second.get("sha256"));
        assertEquals(
            64,
            second.get("sha256")
                .getAsString()
                .length());
    }

    @Test
    void failedArtifactReadIsNotRetriedEveryHour() throws Exception {
        Path missing = directory.resolve("missing.jar");
        TelemetryClient client = new TelemetryClient(missing, new URL("http://127.0.0.1:1"), new SecureRandom());
        assertTrue(
            report(client).get("jarProof")
                .isJsonNull());
        Files.write(missing, new byte[] { 1 });
        assertTrue(
            report(client).get("jarProof")
                .isJsonNull());
    }
}
