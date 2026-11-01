package pl.kuba6000.gtnhextranerf.progression.exam;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.network.PacketBuffer;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

class ExamProtocolTest {

    @Test
    void clientReceivesQuestionTextWithoutReceivingTheServersAnswerKey() throws Exception {
        ExamQuestion question = new ExamQuestion("first", "Question", "Alpha", "Bravo", "Charlie", "Delta");
        ExamDefinition first = new ExamDefinition(
            "exam",
            ProgressionTier.LV,
            Arrays.asList(question),
            new int[] { 0 },
            1,
            true);
        ExamDefinition second = new ExamDefinition(
            "exam",
            ProgressionTier.LV,
            Arrays.asList(question),
            new int[] { 3 },
            1,
            true);
        PacketBuffer a = new PacketBuffer(Unpooled.buffer());
        PacketBuffer b = new PacketBuffer(Unpooled.buffer());
        try {
            ExamProtocol.writePaper(a, first.paper());
            ExamProtocol.writePaper(b, second.paper());
            assertEquals(a, b, "Changing only the answer key must not change the client payload");
            ExamPaper paper = ExamProtocol.readPaper(a);
            assertEquals(
                "Question",
                paper.questions()
                    .get(0)
                    .prompt());
            assertEquals(
                Arrays.asList("Alpha", "Bravo", "Charlie", "Delta"),
                paper.questions()
                    .get(0)
                    .options());
        } finally {
            a.release();
            b.release();
        }
    }

    @Test
    void malformedOrOversizedSubmissionsAreRejectedBeforeGrading() {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            buffer.writeByte(ExamDefinition.MAX_QUESTIONS + 1);
            assertThrows(IOException.class, () -> ExamProtocol.readChoices(buffer));
            buffer.clear();
            buffer.writeByte(2)
                .writeByte(1);
            assertThrows(IOException.class, () -> ExamProtocol.readChoices(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void serverIntroductionAndResultMessagesReachTheClientUnchanged() throws Exception {
        ExamPaper source = new ExamPaper(
            "exam",
            Arrays.asList(new ExamQuestion("first", "Question", "A", "B", "C", "D")),
            1,
            true,
            "Introduction\nSecond paragraph",
            "Passed message",
            "Failed message");
        PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
        try {
            ExamProtocol.writePaper(packet, source);
            ExamPaper received = ExamProtocol.readPaper(packet);
            assertEquals("Introduction\nSecond paragraph", received.introduction());
            assertEquals("Passed message", received.passedMessage());
            assertEquals("Failed message", received.failedMessage());
        } finally {
            packet.release();
        }
    }
}
