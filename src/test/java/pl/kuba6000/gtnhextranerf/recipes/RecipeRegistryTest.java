package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import pl.kuba6000.gtnhextranerf.audit.AuditReport;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.audit.AuditSession;
import pl.kuba6000.gtnhextranerf.audit.AuditStatus;

class RecipeRegistryTest {

    @Test
    void developmentContinuesAfterValidationFailureButRetainsFailedAuditAndNoPartialWrites() {
        AuditScope failed = AuditScope.MODIFY_TITANIUM_BLAST_FURNACE_RECIPE;
        AuditScope next = AuditScope.MODIFY_ILMENITE_BLAST_FURNACE_RECIPE;
        AuditSession audit = new AuditSession(Arrays.asList(failed, next));
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("old");
        RecipeRegistry registry = new RecipeRegistry(audit, RecipeFailurePolicy.CONTINUE_ON_VALIDATION_FAILURE);
        registry.register(failed, recipes -> {
            recipes.edit(store)
                .replaceOne("old"::equals, "partial");
            recipes.edit(store)
                .removeExactly("missing"::equals, 1);
        });
        registry.register(
            next,
            recipes -> recipes.edit(store)
                .replaceOne("old"::equals, "done"));
        registry.run();
        AuditReport report = audit.finish();
        assertEquals(Arrays.asList("done"), store.snapshot());
        assertFalse(report.accepted());
        assertEquals(
            AuditStatus.FAILED,
            report.result(failed.id())
                .status());
        assertEquals(
            0,
            report.result(failed.id())
                .counts()
                .modified());
        assertEquals(
            AuditStatus.SUCCESS,
            report.result(next.id())
                .status());
        assertEquals(
            1,
            report.counts()
                .modified());
    }

    @Test
    void developmentDoesNotSuppressProgrammerErrors() {
        AuditScope scope = AuditScope.MODIFY_TITANIUM_BLAST_FURNACE_RECIPE;
        AuditSession audit = new AuditSession(Arrays.asList(scope));
        RecipeRegistry registry = new RecipeRegistry(audit, RecipeFailurePolicy.CONTINUE_ON_VALIDATION_FAILURE);
        IllegalStateException failure = new IllegalStateException("unexpected state");
        registry.register(scope, recipes -> { throw failure; });
        assertSame(failure, assertThrows(IllegalStateException.class, registry::run));
        assertEquals(
            AuditStatus.FAILED,
            audit.finish()
                .result(scope.id())
                .status());
    }

    @Test
    void registeredModifiersSeeEarlierResultsAndRecordCommittedChanges() {
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("old");
        AuditSession audit = new AuditSession(
            Arrays.asList(
                AuditScope.MODIFY_TITANIUM_BLAST_FURNACE_RECIPE,
                AuditScope.MODIFY_ILMENITE_BLAST_FURNACE_RECIPE));
        RecipeRegistry registry = new RecipeRegistry(audit, RecipeFailurePolicy.FAIL_FAST);
        registry.register(
            AuditScope.MODIFY_TITANIUM_BLAST_FURNACE_RECIPE,
            recipes -> recipes.edit(store)
                .replaceOne("old"::equals, "intermediate"));
        registry.register(
            AuditScope.MODIFY_ILMENITE_BLAST_FURNACE_RECIPE,
            recipes -> recipes.edit(store)
                .replaceOne("intermediate"::equals, "finished"));

        assertEquals(Arrays.asList("old"), store.snapshot());
        registry.run();

        assertEquals(Arrays.asList("finished"), store.snapshot());
        AuditReport report = audit.finish();
        assertTrue(report.accepted());
        assertEquals(
            2,
            report.counts()
                .modified());
    }
}
