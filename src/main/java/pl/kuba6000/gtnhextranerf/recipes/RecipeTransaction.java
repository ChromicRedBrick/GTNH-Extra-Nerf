package pl.kuba6000.gtnhextranerf.recipes;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** One logical modifier: finish planning all registries before publishing any changes. */
public final class RecipeTransaction {

    private final Map<RecipeStore<?>, RecipeEditor<?>> editors = new IdentityHashMap<>();
    private final List<RecipeEditor<?>> ordered = new ArrayList<>();

    private RecipeTransaction() {}

    public static Result execute(Consumer<RecipeTransaction> modifier) {
        RecipeTransaction transaction = new RecipeTransaction();
        modifier.accept(transaction);
        for (RecipeEditor<?> editor : transaction.ordered) editor.validateUnchanged();
        List<RecipeEditor<?>> attempted = new ArrayList<>();
        try {
            for (RecipeEditor<?> editor : transaction.ordered) {
                attempted.add(editor);
                editor.commit();
            }
        } catch (RuntimeException failure) {
            boolean rollbackFailed = false;
            for (int i = attempted.size() - 1; i >= 0; i--) {
                try {
                    attempted.get(i)
                        .rollback();
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                    rollbackFailed = true;
                }
            }
            if (rollbackFailed) throw new RecipeRollbackException(failure);
            throw failure;
        }
        int added = 0;
        int modified = 0;
        int removed = 0;
        for (RecipeEditor<?> editor : transaction.ordered) {
            Result result = editor.result();
            added += result.added();
            modified += result.modified();
            removed += result.removed();
        }
        return new Result(added, modified, removed);
    }

    // Store identity fixes T: an editor is created only from the exact store used as its key.
    @SuppressWarnings("unchecked")
    public <T> RecipeEditor<T> edit(RecipeStore<T> store) {
        RecipeEditor<?> editor = editors.get(store);
        if (editor == null) {
            editor = new RecipeEditor<>(store);
            editors.put(store, editor);
            ordered.add(editor);
        }
        return (RecipeEditor<T>) editor;
    }

    public static final class Result {

        private final int added;
        private final int modified;
        private final int removed;

        Result(int added, int modified, int removed) {
            this.added = added;
            this.modified = modified;
            this.removed = removed;
        }

        public int added() {
            return added;
        }

        public int modified() {
            return modified;
        }

        public int removed() {
            return removed;
        }
    }
}
