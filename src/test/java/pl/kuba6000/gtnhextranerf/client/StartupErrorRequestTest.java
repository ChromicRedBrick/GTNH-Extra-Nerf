package pl.kuba6000.gtnhextranerf.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

class StartupErrorRequestTest {

    @Test
    void previewRequiresBothDevelopmentAndExplicitRequest() {
        assertFalse(
            StartupErrorRequest.developmentPreview(false, true)
                .isPresent());
        assertFalse(
            StartupErrorRequest.developmentPreview(true, false)
                .isPresent());
        StartupErrorRequest preview = StartupErrorRequest.developmentPreview(true, true)
            .get();
        assertTrue(preview.isPreview());
        assertTrue(
            preview.supportMessage()
                .contains(GTNHExtraNerf.SUPPORTED_GTNH_VERSION));
    }

    @Test
    void realFailuresKeepTheirOriginalCause() {
        Throwable cause = new IllegalStateException("recipe missing");
        StartupErrorRequest request = StartupErrorRequest.failure(cause);
        assertSame(cause, request.cause());
        assertFalse(request.isPreview());
    }
}
