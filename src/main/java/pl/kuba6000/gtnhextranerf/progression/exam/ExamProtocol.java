package pl.kuba6000.gtnhextranerf.progression.exam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.PacketBuffer;

/** Small bounded payloads. Pencil strokes and the server answer key are not part of this protocol. */
public final class ExamProtocol {

    private ExamProtocol() {}

    public static void writePaper(PacketBuffer buffer, ExamPaper paper) throws IOException {
        writeText(buffer, paper.id(), 64);
        buffer.writeBoolean(paper.enabled());
        buffer.writeByte(paper.requiredCorrect());
        buffer.writeByte(
            paper.questions()
                .size());
        writeText(buffer, paper.introduction(), 4096);
        writeText(buffer, paper.passedMessage(), 4096);
        writeText(buffer, paper.failedMessage(), 4096);
        for (ExamQuestion question : paper.questions()) {
            writeText(buffer, question.id(), 64);
            writeText(buffer, question.prompt(), 1024);
            for (String option : question.options()) writeText(buffer, option, 512);
        }
    }

    public static ExamPaper readPaper(PacketBuffer buffer) throws IOException {
        String id = buffer.readStringFromBuffer(64);
        boolean enabled = buffer.readBoolean();
        int required = buffer.readUnsignedByte(), count = buffer.readUnsignedByte();
        if (count < 1 || count > ExamDefinition.MAX_QUESTIONS) throw new IOException("Invalid exam question count");
        String introduction = buffer.readStringFromBuffer(4096), passedMessage = buffer.readStringFromBuffer(4096),
            failedMessage = buffer.readStringFromBuffer(4096);
        List<ExamQuestion> questions = new ArrayList<>(count);
        try {
            for (int i = 0; i < count; i++) {
                String questionId = buffer.readStringFromBuffer(64), prompt = buffer.readStringFromBuffer(1024);
                String[] options = new String[4];
                for (int option = 0; option < 4; option++) options[option] = buffer.readStringFromBuffer(512);
                questions.add(new ExamQuestion(questionId, prompt, options));
            }
            return new ExamPaper(id, questions, required, enabled, introduction, passedMessage, failedMessage);
        } catch (IllegalArgumentException failure) {
            throw new IOException("Invalid exam paper", failure);
        }
    }

    private static void writeText(PacketBuffer buffer, String text, int maxLength) throws IOException {
        if (text.length() > maxLength) throw new IOException("Exam text exceeds " + maxLength + " characters");
        buffer.writeStringToBuffer(text);
    }

    public static void writeChoices(PacketBuffer buffer, int[] choices) throws IOException {
        if (choices.length < 1 || choices.length > ExamDefinition.MAX_QUESTIONS)
            throw new IOException("Invalid exam answer count");
        buffer.writeByte(choices.length);
        for (int choice : choices) {
            if (choice < 0 || choice > 15) throw new IOException("Invalid exam choice");
            buffer.writeByte(choice);
        }
    }

    public static int[] readChoices(PacketBuffer buffer) throws IOException {
        if (!buffer.isReadable()) throw new IOException("Empty exam submission");
        int count = buffer.readUnsignedByte();
        if (count < 1 || count > ExamDefinition.MAX_QUESTIONS || buffer.readableBytes() != count)
            throw new IOException("Invalid exam submission size");
        int[] choices = new int[count];
        for (int i = 0; i < count; i++) {
            choices[i] = buffer.readUnsignedByte();
            if (choices[i] > 15) throw new IOException("Invalid exam choice");
        }
        return choices;
    }
}
