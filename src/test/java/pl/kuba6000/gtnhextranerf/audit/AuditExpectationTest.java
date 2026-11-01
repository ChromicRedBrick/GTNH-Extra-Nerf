package pl.kuba6000.gtnhextranerf.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuditExpectationTest {

    @Test
    void familyContractsPreserveTheirDistinctCountRules() {
        AuditExpectation minimum = AuditExpectation.atLeastModified(1);
        assertTrue(minimum.accepts(new AuditCounts(2, 4, 3)));
        assertFalse(minimum.accepts(new AuditCounts(0, 4, 3)));
        AuditExpectation paired = AuditExpectation.addedPerModification();
        assertTrue(paired.accepts(new AuditCounts(3, 3, 0)));
        assertFalse(paired.accepts(new AuditCounts(0, 0, 0)));
        assertFalse(paired.accepts(new AuditCounts(3, 2, 0)));
        assertFalse(paired.accepts(new AuditCounts(3, 3, 1)));
    }

    @Test
    void exactCountsRejectUnexpectedKindsOfChanges() {
        AuditExpectation expectation = AuditExpectation.exact()
            .modified(2)
            .added(1)
            .build();
        assertTrue(expectation.accepts(new AuditCounts(2, 1, 0)));
        assertFalse(expectation.accepts(new AuditCounts(2, 1, 1)));
        assertFalse(expectation.accepts(new AuditCounts(1, 1, 0)));
    }
}
