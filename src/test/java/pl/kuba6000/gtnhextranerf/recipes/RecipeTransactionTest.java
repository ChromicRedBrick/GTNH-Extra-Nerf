package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class RecipeTransactionTest {

    @Test
    void failedRollbackIsReportedAsUnrecoverable() {
        MemoryStore store = new MemoryStore("original");
        store.failPermanently = true;
        assertThrows(
            RecipeRollbackException.class,
            () -> RecipeTransaction.execute(
                transaction -> transaction.edit(store)
                    .replaceOne("original"::equals, "replacement")));
    }

    @Test
    void concurrentRegistryChangeIsNotOverwrittenByAnOutdatedPlan() {
        MemoryStore store = new MemoryStore("old");
        assertThrows(IllegalStateException.class, () -> RecipeTransaction.execute(transaction -> {
            transaction.edit(store)
                .replaceOne("old"::equals, "planned");
            store.replaceAll(Arrays.asList("external"));
        }));
        assertEquals(Arrays.asList("external"), store.snapshot());
    }

    @Test
    void failedWriteRestoresEarlierRegistriesAndThePartiallyWrittenRegistry() {
        MemoryStore first = new MemoryStore("first");
        MemoryStore second = new MemoryStore("second");
        second.rejectNextReplacement = true;
        assertThrows(IllegalStateException.class, () -> RecipeTransaction.execute(transaction -> {
            transaction.edit(first)
                .replaceOne("first"::equals, "changed-first");
            transaction.edit(second)
                .replaceOne("second"::equals, "changed-second");
        }));
        assertEquals(Arrays.asList("first"), first.snapshot());
        assertEquals(Arrays.asList("second"), second.snapshot());
    }

    @Test
    void appliesAddRemoveAndReplacementInOrder() {
        MemoryStore store = new MemoryStore("old", "obsolete", "unrelated");
        RecipeTransaction.execute(transaction -> {
            RecipeEditor<String> recipes = transaction.edit(store);
            recipes.replaceOne("old"::equals, "new");
            recipes.removeExactly("obsolete"::equals, 1);
            recipes.addOne("added", "added"::equals);
            recipes.replaceOne("added"::equals, "finished");
            assertEquals(Arrays.asList("old", "obsolete", "unrelated"), store.snapshot());
        });
        assertEquals(Arrays.asList("new", "unrelated", "finished"), store.snapshot());
    }

    @Test
    void missingTargetLeavesAllRegistriesUnchanged() {
        MemoryStore first = new MemoryStore("old", "unrelated");
        MemoryStore second = new MemoryStore("other");

        assertThrows(IllegalStateException.class, () -> RecipeTransaction.execute(transaction -> {
            transaction.edit(first)
                .removeExactly("old"::equals, 1);
            transaction.edit(second)
                .removeExactly("missing"::equals, 1);
        }));

        assertEquals(Arrays.asList("old", "unrelated"), first.snapshot());
        assertEquals(Arrays.asList("other"), second.snapshot());
    }

    static final class MemoryStore implements RecipeStore<String> {

        private final List<String> recipes = new ArrayList<>();
        private boolean rejectNextReplacement;
        private boolean failPermanently;

        MemoryStore(String... recipes) {
            this.recipes.addAll(Arrays.asList(recipes));
        }

        @Override
        public String name() {
            return "test recipes";
        }

        @Override
        public List<String> snapshot() {
            return new ArrayList<>(recipes);
        }

        @Override
        public void replaceAll(List<String> replacement) {
            recipes.clear();
            recipes.addAll(replacement);
            if (rejectNextReplacement || failPermanently) {
                rejectNextReplacement = false;
                throw new IllegalStateException("Registry rejected the replacement");
            }
        }
    }
}
