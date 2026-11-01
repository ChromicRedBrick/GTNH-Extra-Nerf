package pl.kuba6000.gtnhextranerf.progression.exam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Public question text. Correct answers belong only to the server definition. */
public final class ExamQuestion {

    private final String id;
    private final String prompt;
    private final List<String> options;

    public ExamQuestion(String id, String prompt, String... options) {
        this.id = Objects.requireNonNull(id);
        this.prompt = Objects.requireNonNull(prompt);
        if (id.isEmpty() || id.length() > 64 || prompt.isEmpty() || prompt.length() > 1024 || options.length != 4)
            throw new IllegalArgumentException("An exam question needs an ID, prompt and four options");
        String[] copy = options.clone();
        for (String option : copy) if (option == null || option.isEmpty() || option.length() > 512)
            throw new IllegalArgumentException("Empty exam option");
        this.options = Collections.unmodifiableList(Arrays.asList(copy));
    }

    public String id() {
        return id;
    }

    public String prompt() {
        return prompt;
    }

    public List<String> options() {
        return options;
    }
}
