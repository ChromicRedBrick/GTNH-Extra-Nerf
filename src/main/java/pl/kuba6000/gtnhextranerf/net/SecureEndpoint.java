package pl.kuba6000.gtnhextranerf.net;

import java.net.URL;

/** Outbound mod traffic is HTTPS only; plain HTTP is accepted solely for a loopback test endpoint. */
public final class SecureEndpoint {

    private SecureEndpoint() {}

    public static void require(URL endpoint) {
        if (!"https".equals(endpoint.getProtocol()) && !isLoopbackHttp(endpoint))
            throw new IllegalArgumentException("Outbound connections require HTTPS");
    }

    private static boolean isLoopbackHttp(URL endpoint) {
        String host = endpoint.getHost();
        return "http".equals(endpoint.getProtocol())
            && ("localhost".equals(host) || "127.0.0.1".equals(host) || "[::1]".equals(host));
    }
}
