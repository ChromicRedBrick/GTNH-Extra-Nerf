package pl.kuba6000.gtnhextranerf.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/** Cardinality regressions previously covered by RecipeMapPatchTest, now exercised through the public editor. */
class RecipeEditorTest {

    @Test
    void knownReplacementsPreserveIdentityAndFollowStagedEdits() {
        String first = new String("equal");
        String second = new String("equal");
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("obsolete", first, second);
        RecipeTransaction.Result result = RecipeTransaction.execute(transaction -> {
            RecipeEditor<String> editor = transaction.edit(store);
            editor.replaceKnownOne(first, "first-change");
            editor.removeExactly("obsolete"::equals, 1);
            editor.addOne("added", "added"::equals);
            editor.replaceKnownOne("added", "finished");
            editor.replaceKnownOne("first-change", "final-first");
            assertSame(
                first,
                store.snapshot()
                    .get(1));
        });
        assertEquals(Arrays.asList("final-first", second, "finished"), store.snapshot());
        assertSame(
            second,
            store.snapshot()
                .get(1));
        assertEquals(3, result.modified());
        assertEquals(1, result.added());
        assertEquals(1, result.removed());
    }

    @Test
    void invalidKnownTargetsAndRegisteredReplacementsPublishNoChanges() {
        for (String[] initial : new String[][] { { "original", "other" }, { "original", "original", "other" } }) {
            for (int scenario = 0; scenario < 3; scenario++) {
                int invalidCase = scenario;
                RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore(initial);
                assertThrows(RecipeValidationException.class, () -> RecipeTransaction.execute(transaction -> {
                    RecipeEditor<String> editor = transaction.edit(store);
                    editor.addOne("staged", "staged"::equals);
                    if (invalidCase == 0) editor.replaceKnownOne(new String("original"), "replacement");
                    else if (invalidCase == 1) editor.replaceKnownOne("other", "original");
                    else {
                        editor.replaceKnownOne("original", "replacement");
                        editor.replaceKnownOne("original", "stale");
                    }
                }));
                assertEquals(Arrays.asList(initial), store.snapshot());
            }
        }
    }

    @Test
    void selectsTheOnlyMatch() {
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("skip", "match");
        RecipeTransaction.execute(
            transaction -> assertEquals(
                "match",
                transaction.edit(store)
                    .requireExactlyOne("match"::equals)));
    }

    @Test
    void missingAndAmbiguousTargetsAreRejectedWithoutMutation() {
        for (String[] initial : new String[][] { { "skip" }, { "match", "match" } }) {
            RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore(initial);
            assertThrows(
                IllegalStateException.class,
                () -> RecipeTransaction.execute(
                    transaction -> transaction.edit(store)
                        .replaceOne("match"::equals, "replacement")));
            assertEquals(Arrays.asList(initial), store.snapshot());
        }
    }

    @Test
    void retentionKeepsAllowedAndUnrelatedRecipes() {
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore(
            "steel-coke",
            "steel-coal",
            "glass-coal");
        RecipeTransaction.Result result = RecipeTransaction.execute(
            transaction -> transaction.edit(store)
                .retainExactly(value -> value.startsWith("steel-"), value -> value.endsWith("coke"), 1, 1));
        assertEquals(Arrays.asList("steel-coke", "glass-coal"), store.snapshot());
        assertEquals(1, result.removed());
        assertEquals(0, result.modified());
        assertEquals(0, result.added());
    }

    @Test
    void unexpectedRetentionCountDoesNotRemoveRecipes() {
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("coke", "coal");
        assertThrows(
            IllegalStateException.class,
            () -> RecipeTransaction.execute(
                transaction -> transaction.edit(store)
                    .retainExactly(value -> true, "coke"::equals, 2, 1)));
        assertEquals(Arrays.asList("coke", "coal"), store.snapshot());
    }

    @Test
    void absenceAssertionDoesNotCountAsRemoval() {
        RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("unrelated");
        RecipeTransaction.Result result = RecipeTransaction.execute(
            transaction -> transaction.edit(store)
                .expectExactly("missing"::equals, 0));
        assertEquals(Arrays.asList("unrelated"), store.snapshot());
        assertEquals(0, result.removed());
    }

    @Test
    void duplicateOrInvalidAdditionDoesNotPublishEarlierEdits() {
        for (String addition : Arrays.asList("existing", "invalid")) {
            RecipeTransactionTest.MemoryStore store = new RecipeTransactionTest.MemoryStore("old", "existing");
            assertThrows(IllegalStateException.class, () -> RecipeTransaction.execute(transaction -> {
                transaction.edit(store)
                    .removeExactly("old"::equals, 1);
                transaction.edit(store)
                    .addOne(addition, "existing"::equals);
            }));
            assertEquals(Arrays.asList("old", "existing"), store.snapshot());
        }
    }
}
