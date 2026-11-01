package pl.kuba6000.gtnhextranerf.progression.exam;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Immutable server-owned question bank and answer key. */
public final class ExamDefinition {

    public static final int MAX_QUESTIONS = 32;

    private final String id;
    private final ProgressionTier tier;
    private final List<ExamQuestion> questions;
    private final int[] correctOptions;
    private final int requiredCorrect;
    private final boolean enabled;
    private final ExamPaper paper;

    public ExamDefinition(String id, ProgressionTier tier, List<ExamQuestion> questions, int[] correctOptions,
        int requiredCorrect, boolean enabled) {
        this(id, tier, questions, correctOptions, requiredCorrect, enabled, "", "", "");
    }

    public ExamDefinition(String id, ProgressionTier tier, List<ExamQuestion> questions, int[] correctOptions,
        int requiredCorrect, boolean enabled, String introduction, String passedMessage, String failedMessage) {
        this.id = Objects.requireNonNull(id);
        this.tier = Objects.requireNonNull(tier);
        if (id.isEmpty() || id.length() > 64
            || questions.isEmpty()
            || questions.size() > MAX_QUESTIONS
            || correctOptions.length != questions.size()
            || requiredCorrect < 1
            || requiredCorrect > questions.size()) throw new IllegalArgumentException("Invalid exam definition: " + id);
        Set<String> ids = new HashSet<>();
        for (ExamQuestion question : questions) if (!ids.add(question.id()))
            throw new IllegalArgumentException("Duplicate exam question: " + question.id());
        for (int option : correctOptions) if (option < (enabled ? 0 : -1) || option > 3)
            throw new IllegalArgumentException("Correct exam options must be A-D");
        this.questions = Collections.unmodifiableList(new ArrayList<>(questions));
        this.correctOptions = correctOptions.clone();
        this.requiredCorrect = requiredCorrect;
        this.enabled = enabled;
        this.paper = new ExamPaper(
            id,
            this.questions,
            requiredCorrect,
            enabled,
            introduction,
            passedMessage,
            failedMessage);
    }

    public String id() {
        return id;
    }

    public ProgressionTier tier() {
        return tier;
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

    public ExamPaper paper() {
        return paper;
    }

    public static ExamDefinition read(Reader reader) {
        return read(reader, ProgressionTier.LV);
    }

    /** The registered server factory determines the reward; JSON cannot redirect it. */
    public static ExamDefinition read(Reader reader, ProgressionTier tier) {
        JsonObject json = new JsonParser().parse(reader)
            .getAsJsonObject();
        JsonArray entries = json.getAsJsonArray("questions");
        List<ExamQuestion> questions = new ArrayList<>();
        int[] key = new int[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            JsonObject entry = entries.get(i)
                .getAsJsonObject();
            JsonArray answers = entry.getAsJsonArray("options");
            String[] options = new String[answers.size()];
            for (int option = 0; option < options.length; option++) options[option] = answers.get(option)
                .getAsString();
            questions.add(
                new ExamQuestion(
                    entry.get("id")
                        .getAsString(),
                    entry.get("prompt")
                        .getAsString(),
                    options));
            JsonElement correct = entry.get("correct");
            if (correct == null || correct.isJsonNull()) key[i] = -1;
            else {
                String answer = correct.getAsString();
                if (answer.length() != 1 || "ABCD".indexOf(answer) < 0) throw new IllegalArgumentException(
                    "Correct exam option must be A-D: " + questions.get(i)
                        .id());
                key[i] = "ABCD".indexOf(answer);
            }
        }
        return new ExamDefinition(
            json.get("id")
                .getAsString(),
            tier,
            questions,
            key,
            json.get("requiredCorrect")
                .getAsInt(),
            json.get("enabled")
                .getAsBoolean(),
            text(json, "introduction"),
            text(json, "passedMessage"),
            text(json, "failedMessage"));
    }

    private static String text(JsonObject json, String name) {
        JsonElement value = json.get(name);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    int score(int[] choices) {
        int correct = 0;
        for (int i = 0; i < choices.length; i++) if (choices[i] == (1 << correctOptions[i])) correct++;
        return correct;
    }
}
