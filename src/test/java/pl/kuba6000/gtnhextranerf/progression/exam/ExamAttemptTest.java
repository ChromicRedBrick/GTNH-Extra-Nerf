package pl.kuba6000.gtnhextranerf.progression.exam;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

class ExamAttemptTest {

    @Test
    void passingTheServerExamAwardsOneCertificateEvenWhenSubmissionIsRepeated() {
        ExamDefinition exam = new ExamDefinition(
            "test-lv",
            ProgressionTier.LV,
            Arrays.asList(
                new ExamQuestion("first", "First question", "A", "B", "C", "D"),
                new ExamQuestion("second", "Second question", "A", "B", "C", "D")),
            new int[] { 0, 3 },
            2,
            true);
        UUID owner = UUID.randomUUID();
        AtomicInteger certificates = new AtomicInteger();
        ExamAttempt attempt = new ExamAttempt(owner, exam, certificates::incrementAndGet);

        ExamAttempt.Result result = attempt.submit(owner, new int[] { 1, 8 });

        assertEquals(ExamAttempt.Status.PASSED, result.status());
        assertEquals(2, result.correct());
        assertEquals(1, certificates.get());
        assertEquals(
            ExamAttempt.Status.PASSED,
            attempt.submit(owner, new int[] { 1, 8 })
                .status());
        assertEquals(1, certificates.get());
    }

    @Test
    void foreignIncompleteAndFailedAnswersDoNotIssueCertificates() {
        UUID owner = UUID.randomUUID();
        AtomicInteger certificates = new AtomicInteger();
        ExamDefinition exam = new ExamDefinition(
            "test",
            ProgressionTier.LV,
            Arrays.asList(new ExamQuestion("first", "Question", "A", "B", "C", "D")),
            new int[] { 2 },
            1,
            true);
        ExamAttempt attempt = new ExamAttempt(owner, exam, certificates::incrementAndGet);

        assertEquals(
            ExamAttempt.Status.INVALID,
            attempt.submit(UUID.randomUUID(), new int[] { 4 })
                .status());
        assertEquals(
            ExamAttempt.Status.INVALID,
            attempt.submit(owner, new int[] { 16 })
                .status());
        assertEquals(
            ExamAttempt.Status.INCOMPLETE,
            attempt.submit(owner, new int[] { 0 })
                .status());
        assertEquals(
            ExamAttempt.Status.INCOMPLETE,
            attempt.submit(owner, new int[] { 5 })
                .status());
        assertEquals(0, certificates.get());
        assertEquals(
            ExamAttempt.Status.FAILED,
            attempt.submit(owner, new int[] { 1 })
                .status());
        assertEquals(
            ExamAttempt.Status.FAILED,
            attempt.submit(owner, new int[] { 4 })
                .status());
        assertEquals(0, certificates.get());
    }

    @Test
    void unfinishedQuestionBankCannotAwardCertificates() {
        UUID owner = UUID.randomUUID();
        AtomicInteger certificates = new AtomicInteger();
        ExamDefinition preview = new ExamDefinition(
            "preview",
            ProgressionTier.LV,
            Arrays.asList(new ExamQuestion("first", "Pending", "A", "B", "C", "D")),
            new int[] { 0 },
            1,
            false);

        assertEquals(
            ExamAttempt.Status.UNAVAILABLE,
            new ExamAttempt(owner, preview, certificates::incrementAndGet).submit(owner, new int[] { 1 })
                .status());
        assertEquals(0, certificates.get());
    }

    @Test
    void passThresholdAllowsSomeWrongAnswersButRejectsInsufficientScore() {
        ExamDefinition exam = new ExamDefinition(
            "threshold",
            ProgressionTier.LV,
            Arrays.asList(
                new ExamQuestion("one", "One", "A", "B", "C", "D"),
                new ExamQuestion("two", "Two", "A", "B", "C", "D"),
                new ExamQuestion("three", "Three", "A", "B", "C", "D")),
            new int[] { 0, 1, 2 },
            2,
            true);
        UUID owner = UUID.randomUUID();
        AtomicInteger certificates = new AtomicInteger();

        ExamAttempt.Result passed = new ExamAttempt(owner, exam, certificates::incrementAndGet)
            .submit(owner, new int[] { 1, 2, 8 });
        assertEquals(ExamAttempt.Status.PASSED, passed.status());
        assertEquals(2, passed.correct());
        ExamAttempt.Result failed = new ExamAttempt(owner, exam, certificates::incrementAndGet)
            .submit(owner, new int[] { 1, 8, 8 });
        assertEquals(ExamAttempt.Status.FAILED, failed.status());
        assertEquals(1, failed.correct());
        assertEquals(1, certificates.get());
    }
}
