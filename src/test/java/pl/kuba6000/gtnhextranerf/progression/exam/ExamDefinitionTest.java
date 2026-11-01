package pl.kuba6000.gtnhextranerf.progression.exam;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

class ExamDefinitionTest {

    @Test
    void theServerChoosesWhichCertificateAConfiguredExamAwards() {
        String source = "{\"id\":\"steam\",\"enabled\":true,\"requiredCorrect\":1,\"questions\":["
            + "{\"id\":\"one\",\"prompt\":\"Question\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":\"B\"}]}";
        assertSame(
            ProgressionTier.STEAM,
            ExamDefinition.read(new StringReader(source), ProgressionTier.STEAM)
                .tier());
        assertSame(
            ProgressionTier.LV,
            ExamDefinition.read(new StringReader(source), ProgressionTier.LV)
                .tier());
    }

    @Test
    void questionBankCanRemainADisabledPreviewUntilItsAnswerKeyIsSupplied() {
        String source = "{\"id\":\"lv\",\"enabled\":false,\"requiredCorrect\":1,\"questions\":["
            + "{\"id\":\"one\",\"prompt\":\"Pending\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correct\":null}]}";

        assertFalse(
            ExamDefinition.read(new StringReader(source))
                .enabled());
        assertThrows(
            IllegalArgumentException.class,
            () -> ExamDefinition.read(new StringReader(source.replace("false", "true"))));
    }
}
