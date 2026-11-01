package pl.kuba6000.gtnhextranerf.progression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.network.PacketBuffer;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import io.netty.buffer.Unpooled;

class CertificateStatusPanelTest {

    @Test
    void serverPacketsControlClientIconWithoutReadingServerStateOnClient() throws Exception {
        ExecutorService server = Executors.newSingleThreadExecutor(r -> new Thread(r, "certificate-server"));
        ExecutorService client = Executors.newSingleThreadExecutor(r -> new Thread(r, "certificate-client"));
        AtomicReference<ProgressionTier> missing = new AtomicReference<>(ProgressionTier.STEAM);
        AtomicReference<ProgressionTier> opened = new AtomicReference<>();
        try {
            PanelSyncManager serverSync = new PanelSyncManager(new ModularSyncManager(false), false);
            PanelSyncManager clientSync = new PanelSyncManager(new ModularSyncManager(true), true);
            server.submit(() -> new CertificateStatusPanel(new ParentWidget<>(), serverSync, () -> {
                assertEquals(
                    "certificate-server",
                    Thread.currentThread()
                        .getName());
                return missing.get();
            }, tier -> fail("Server must not open NEI")))
                .get();
            CertificateStatusPanel clientPanel = client
                .submit(
                    () -> new CertificateStatusPanel(
                        new ParentWidget<>(),
                        clientSync,
                        () -> { throw new AssertionError("Client read server state"); },
                        opened::set))
                .get();
            IntSyncValue source = (IntSyncValue) serverSync.findSyncHandlerNullable(CertificateStatusPanel.SYNC_ID, 0);
            IntSyncValue destination = (IntSyncValue) clientSync
                .findSyncHandlerNullable(CertificateStatusPanel.SYNC_ID, 0);
            assertFalse(destination.isAllowC2S());
            assertFalse(
                clientPanel.getChildren()
                    .get(1)
                    .isEnabled());
            for (ProgressionTier value : new ProgressionTier[] { ProgressionTier.STEAM, ProgressionTier.LV, null,
                ProgressionTier.STEAM }) {
                missing.set(value);
                byte[] payload = server.submit(() -> {
                    source.updateCacheFromSource(true);
                    PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                    try {
                        source.write(buffer);
                        byte[] bytes = new byte[buffer.readableBytes()];
                        buffer.readBytes(bytes);
                        return bytes;
                    } finally {
                        buffer.release();
                    }
                })
                    .get();
                client.submit(() -> {
                    PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(payload));
                    try {
                        destination.readOnClient(0, buffer);
                        assertEquals(
                            value != null,
                            clientPanel.getChildren()
                                .get(1)
                                .isEnabled());
                        assertEquals(
                            value == null,
                            clientPanel.getChildren()
                                .get(0)
                                .isEnabled());
                        if (value != null) {
                            ((ButtonWidget<?>) clientPanel.getChildren()
                                .get(1)).playClickSound(false)
                                    .onMousePressed(0);
                            assertSame(value, opened.get(), "Click must open the certificate received from the server");
                        }
                    } finally {
                        buffer.release();
                    }
                    return null;
                })
                    .get();
            }
        } finally {
            server.shutdownNow();
            client.shutdownNow();
        }
    }
}
