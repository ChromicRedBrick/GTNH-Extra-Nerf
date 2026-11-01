package pl.kuba6000.gtnhextranerf.progression.exam;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AnswerSheetTest {

    @Test
    void pencilMarksSelectOnlyTouchedBoxesAndErasingRemovesTheSelection() {
        AnswerSheet sheet = new AnswerSheet(100, 100, new int[][] { { 10, 10, 10, 10 }, { 10, 40, 10, 10 } });

        sheet.line(12, 12, 17, 17, false);
        assertEquals(1, sheet.selectedMask());
        sheet.line(70, 12, 85, 17, false);
        assertEquals(1, sheet.selectedMask());
        sheet.line(12, 42, 17, 47, false);
        assertEquals(3, sheet.selectedMask());
        sheet.line(12, 12, 17, 17, true);
        assertEquals(2, sheet.selectedMask());
        sheet.line(12, 42, 17, 47, true);
        assertEquals(0, sheet.selectedMask());
    }
}
