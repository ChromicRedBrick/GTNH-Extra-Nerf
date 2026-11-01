package pl.kuba6000.gtnhextranerf.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

class TelemetryClientHttpTest {

    @Test
    void sendsTheRequiredHeadersAndNeverFollowsAnEndpointRedirect() throws Exception {
        AtomicInteger redirects = new AtomicInteger();
        AtomicReference<String> observed = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/iamalive", exchange -> {
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int count;
            while ((count = exchange.getRequestBody()
                .read(buffer)) >= 0) if (count > 0) received.write(buffer, 0, count);
            observed.set(
                exchange.getRequestMethod() + "|"
                    + exchange.getRequestHeaders()
                        .getFirst("User-Agent")
                    + "|"
                    + exchange.getRequestHeaders()
                        .getFirst("X-ExtraNerf-Version")
                    + "|"
                    + exchange.getRequestHeaders()
                        .getFirst("Content-Type")
                    + "|"
                    + new String(received.toByteArray(), StandardCharsets.UTF_8));
            exchange.getResponseHeaders()
                .add("Location", "/redirected");
            exchange.sendResponseHeaders(303, -1);
            exchange.close();
        });
        server.createContext("/redirected", exchange -> {
            redirects.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            TelemetryClient transport = new TelemetryClient(
                null,
                new URL(
                    "http://127.0.0.1:" + server.getAddress()
                        .getPort() + "/iamalive"),
                new SecureRandom());
            assertEquals(
                303,
                transport.send(new ServerTelemetry.Snapshot(UUID.randomUUID(), 1, "stone", true), () -> false).status);
            assertTrue(
                observed.get()
                    .startsWith("POST|GTNH-Extra-Nerf|null|application/json|"));
            JsonObject payload = new JsonParser().parse(
                observed.get()
                    .split("\\|", 5)[4])
                .getAsJsonObject();
            assertEquals(
                5,
                payload.entrySet()
                    .size());
            assertTrue(
                payload.get("singleplayer")
                    .getAsBoolean());
            assertTrue(
                payload.get("jarProof")
                    .isJsonNull());
            assertEquals(0, redirects.get());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void requiresHttpsExceptForLocalLoopbackEndpoints() throws Exception {
        assertDoesNotThrow(
            () -> new TelemetryClient(null, new URL("https://extranerf.kuba6000.pl/iamalive"), new SecureRandom()));
        assertDoesNotThrow(() -> new TelemetryClient(null, new URL("http://localhost:8080/"), new SecureRandom()));
        assertDoesNotThrow(() -> new TelemetryClient(null, new URL("http://127.0.0.1:8080/"), new SecureRandom()));
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryClient(null, new URL("http://extranerf.kuba6000.pl/iamalive"), new SecureRandom()));
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryClient(null, new URL("http://127.0.0.1.example.com/"), new SecureRandom()));
    }
}
