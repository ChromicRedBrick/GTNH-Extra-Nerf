package pl.kuba6000.gtnhextranerf.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

class AuditSessionTest {

    @Test
    void inactiveDefinitionsDoNotResolveUnavailableExpectations() {
        AuditDefinition optional = new AuditDefinition() {

            public String id() {
                return "optional";
            }

            public boolean isActive() {
                return false;
            }

            public AuditExpectation expectation() {
                throw new IllegalStateException("optional dependency absent");
            }
        };
        AuditReport report = new AuditSession(Arrays.asList(optional)).finish();
        assertTrue(report.accepted());
        assertEquals(
            AuditStatus.SKIPPED,
            report.result("optional")
                .status());
        assertFalse(
            report.result("optional")
                .active());
    }

    @Test
    void auditProtocolErrorsDoNotReplaceTheOriginalCallbackFailure() {
        AuditDefinition scope = definition(
            "scope",
            AuditExpectation.exact()
                .build());
        AuditSession session = new AuditSession(Arrays.asList(scope));
        RuntimeException original = new IllegalArgumentException("original failure");
        assertSame(original, assertThrows(RuntimeException.class, () -> session.run(scope, handle -> {
            handle.close();
            throw original;
        })));
        assertFalse(
            session.finish()
                .accepted());
    }

    @Test
    void invalidClosureAndFinishLeaveTheSessionUsableAndOldHandlesStayClosed() {
        AuditDefinition scope = definition(
            "scope",
            AuditExpectation.exact()
                .modified(1)
                .build());
        AuditSession session = new AuditSession(Arrays.asList(scope));
        AuditHandle outer = session.open(scope);
        AuditHandle inner = session.open(scope);
        assertThrows(IllegalStateException.class, outer::close);
        assertThrows(IllegalStateException.class, session::finish);
        java.util.concurrent.CompletableFuture
            .runAsync(() -> assertThrows(IllegalStateException.class, inner::recipeModified))
            .join();
        inner.success();
        inner.close();
        outer.success();
        outer.close();
        session.run(scope, next -> {
            assertThrows(IllegalStateException.class, inner::recipeModified);
            assertThrows(IllegalStateException.class, inner::close);
            next.recipeModified();
        });
        assertTrue(
            session.finish()
                .accepted());
    }

    @Test
    void failuresAndSkippedExecutionsCannotPassByMatchingCounts() {
        AuditDefinition failed = definition(
            "failed",
            AuditExpectation.exact()
                .modified(1)
                .build());
        AuditDefinition skipped = definition(
            "skipped",
            AuditExpectation.exact()
                .build());
        AuditDefinition missing = definition(
            "missing",
            AuditExpectation.exact()
                .build());
        AuditSession session = new AuditSession(Arrays.asList(failed, skipped, missing));
        RuntimeException cause = new IllegalArgumentException("broken input");
        assertSame(cause, assertThrows(RuntimeException.class, () -> session.run(failed, handle -> {
            handle.recipeModified();
            throw cause;
        })));
        session.run(skipped, handle -> handle.skip("optional input unavailable"));
        AuditReport report = session.finish();
        assertFalse(report.accepted());
        assertEquals(
            AuditStatus.FAILED,
            report.result("failed")
                .status());
        assertEquals(
            1,
            report.result("failed")
                .failedRuns());
        assertEquals(
            0,
            report.result("failed")
                .successfulRuns());
        assertFalse(
            report.result("failed")
                .diagnostics()
                .isEmpty());
        assertEquals(
            AuditStatus.SKIPPED,
            report.result("skipped")
                .status());
        assertEquals(
            AuditStatus.NOT_RUN,
            report.result("missing")
                .status());
        assertThrows(
            UnsupportedOperationException.class,
            () -> report.results()
                .clear());
    }

    static AuditDefinition definition(String id, AuditExpectation expectation) {
        return new AuditDefinition() {

            public String id() {
                return id;
            }

            public AuditExpectation expectation() {
                return expectation;
            }
        };
    }

    @Test
    void nestedScopesHaveIndependentCountsAndExclusiveTime() {
        AuditDefinition outer = definition(
            "outer",
            AuditExpectation.exact()
                .modified(1)
                .build());
        AuditDefinition inner = definition(
            "inner",
            AuditExpectation.exact()
                .added(1)
                .build());
        AtomicLong time = new AtomicLong();
        AuditSession session = new AuditSession(Arrays.asList(outer, inner), time::get);
        AuditHandle parent = session.open(outer);
        parent.recipeModified();
        time.set(10);
        session.run(inner, child -> {
            child.recipeAdded();
            time.set(30);
        });
        time.set(50);
        parent.success();
        parent.close();
        AuditReport report = session.finish();
        assertTrue(report.accepted());
        assertEquals(
            30,
            report.result("outer")
                .exclusiveNanos());
        assertEquals(
            20,
            report.result("inner")
                .exclusiveNanos());
        assertEquals(
            1,
            report.result("outer")
                .counts()
                .modified());
        assertEquals(
            0,
            report.result("outer")
                .counts()
                .added());
        assertThrows(IllegalStateException.class, parent::recipeModified);
        assertThrows(IllegalStateException.class, () -> session.open(outer));
        assertSame(report, session.finish());
    }
}
