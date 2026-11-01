package pl.kuba6000.gtnhextranerf.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class TelemetryClientPacketTest {

    @Test
    void producesTheExpectedJsonProof() throws Exception {
        JsonObject vector;
        try (InputStreamReader input = new InputStreamReader(
            getClass().getResourceAsStream("/telemetry/report.json"),
            StandardCharsets.UTF_8)) {
            vector = new JsonParser().parse(input)
                .getAsJsonObject();
        }
        SecureRandom fixedSalt = new SecureRandom() {

            @Override
            public void nextBytes(byte[] bytes) {
                for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
            }
        };
        TelemetryClient client = new TelemetryClient(
            Paths.get(
                getClass().getResource("/telemetry/release.jar")
                    .toURI()),
            new URL("http://127.0.0.1:1"),
            fixedSalt);
        String report = client.encode(
            new ServerTelemetry.Snapshot(UUID.fromString("62420a97-276b-4afd-a226-558f247802ac"), 3, "lv", true),
            () -> false);
        // Independent Python hashlib vector.
        assertEquals(vector.get("payload"), new JsonParser().parse(report));
    }
}
