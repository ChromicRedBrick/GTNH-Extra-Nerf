package pl.kuba6000.gtnhextranerf.progression.exam;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import net.minecraft.network.PacketBuffer;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;

class ExamSyncHandlerTest {

    @Test
    void clientKeepsPageChoicesButCannotSubmitBeforeServerAdmission() throws Exception {
        ExamSyncHandler client = new ExamSyncHandler();
        // ModularUI refuses to transmit submissions unless this public capability is enabled.
        assertTrue(client.isAllowC2S(), "Players must be able to submit their exam answers");
        ExamPaper paper = new ExamPaper(
            "server-bank",
            Arrays.asList(
                new ExamQuestion("one", "One", "A", "B", "C", "D"),
                new ExamQuestion("two", "Two", "A", "B", "C", "D")),
            2,
            true);
        PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
        try {
            ExamProtocol.writePaper(packet, paper);
            client.readOnClient(ExamSyncHandler.PAPER, packet);
            assertFalse(client.admitted());
            assertFalse(client.submit(), "Receiving a paper alone cannot authorize submission");
            client.changePage(1);
            client.choose(0, 1);
            client.changePage(1);
            client.choose(1, 8);
            client.changePage(-1);
            assertEquals(0, client.page());
            assertEquals(2, client.answeredCount());
        } finally {
            packet.release();
        }
    }
}
