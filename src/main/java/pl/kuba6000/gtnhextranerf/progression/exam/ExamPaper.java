package pl.kuba6000.gtnhextranerf.progression.exam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Server-to-client projection. Contains display text and availability, never the answer key. */
public final class ExamPaper {

    private final String id;
    private final List<ExamQuestion> questions;
    private final int requiredCorrect;
    private final boolean enabled;
    private final String introduction;
    private final String passedMessage;
    private final String failedMessage;

    public ExamPaper(String id, List<ExamQuestion> questions, int requiredCorrect, boolean enabled) {
        this(id, questions, requiredCorrect, enabled, "", "", "");
    }

    public ExamPaper(String id, List<ExamQuestion> questions, int requiredCorrect, boolean enabled, String introduction,
        String passedMessage, String failedMessage) {
        this.id = Objects.requireNonNull(id);
        if (id.isEmpty() || questions.isEmpty()
            || questions.size() > ExamDefinition.MAX_QUESTIONS
            || requiredCorrect < 1
            || requiredCorrect > questions.size()) throw new IllegalArgumentException("Invalid exam paper");
        this.questions = Collections.unmodifiableList(new ArrayList<>(questions));
        this.requiredCorrect = requiredCorrect;
        this.enabled = enabled;
        this.introduction = message(introduction);
        this.passedMessage = message(passedMessage);
        this.failedMessage = message(failedMessage);
    }

    private static String message(String text) {
        Objects.requireNonNull(text);
        if (text.length() > 4096) throw new IllegalArgumentException("Exam message is too long");
        return text;
    }

    public String id() {
        return id;
    }

    public List<ExamQuestion> questions() {
        return questions;
    }

    public int requiredCorrect() {
        return requiredCorrect;
    }

    public boolean enabled() {
        return enabled;
    }

    public String introduction() {
        return introduction;
    }

    public String passedMessage() {
        return passedMessage;
    }

    public String failedMessage() {
        return failedMessage;
    }
}
