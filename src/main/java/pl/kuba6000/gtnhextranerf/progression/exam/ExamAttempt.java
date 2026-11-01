package pl.kuba6000.gtnhextranerf.progression.exam;

import java.util.Objects;
import java.util.UUID;

/** One server-thread attempt. Repeated delivery of a completed submission cannot repeat its reward. */
public final class ExamAttempt {

    public enum Status {
        READY,
        UNAVAILABLE,
        INCOMPLETE,
        INVALID,
        FAILED,
        PASSED
    }

    public static final class Result {

        private final Status status;
        private final int correct;
        private final int total;

        public Result(Status status, int correct, int total) {
            this.status = Objects.requireNonNull(status);
            this.correct = correct;
            this.total = total;
        }

        public Status status() {
            return status;
        }

        public int correct() {
            return correct;
        }

        public int total() {
            return total;
        }
    }

    private final UUID owner;
    private final ExamDefinition definition;
    private final Runnable awardCertificate;
    private Result completed;

    public ExamAttempt(UUID owner, ExamDefinition definition, Runnable awardCertificate) {
        this.owner = Objects.requireNonNull(owner);
        this.definition = Objects.requireNonNull(definition);
        this.awardCertificate = Objects.requireNonNull(awardCertificate);
    }

    public Result submit(UUID player, int[] choices) {
        int total = definition.questions()
            .size();
        if (!owner.equals(player)) return new Result(Status.INVALID, 0, total);
        if (completed != null) return completed;
        if (!definition.enabled()) return new Result(Status.UNAVAILABLE, 0, total);
        if (choices == null || choices.length != total) return new Result(Status.INVALID, 0, total);
        for (int choice : choices) if (choice < 0 || choice > 15) return new Result(Status.INVALID, 0, total);
        for (int choice : choices) if (Integer.bitCount(choice) != 1) return new Result(Status.INCOMPLETE, 0, total);
        int score = definition.score(choices);
        completed = new Result(score >= definition.requiredCorrect() ? Status.PASSED : Status.FAILED, score, total);
        if (completed.status() == Status.PASSED) awardCertificate.run();
        return completed;
    }
}
