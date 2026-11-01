package pl.kuba6000.gtnhextranerf.recipes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Stages edits in registration order. Selectors must not mutate their arguments. */
public final class RecipeEditor<T> {

    private final RecipeStore<T> store;
    private final List<T> original;
    private final List<T> recipes;
    private Map<T, Integer> positions;
    private int added;
    private int modified;
    private int removed;

    RecipeEditor(RecipeStore<T> store) {
        this.store = store;
        original = new ArrayList<>(store.snapshot());
        recipes = new ArrayList<>(original);
    }

    public void removeExactly(Predicate<T> selector, int expectedCount) {
        List<T> matches = selectExactly(selector, expectedCount);
        Set<T> removedIdentities = Collections.newSetFromMap(new IdentityHashMap<>());
        removedIdentities.addAll(matches);
        recipes.removeIf(removedIdentities::contains);
        positions = null;
        removed += matches.size();
    }

    public T requireExactlyOne(Predicate<T> selector) {
        return selectExactly(selector, 1).get(0);
    }

    public void expectExactly(Predicate<T> selector, int expectedCount) {
        selectExactly(selector, expectedCount);
    }

    public void retainExactly(Predicate<T> scope, Predicate<T> retained, int expectedRetained, int expectedRemoved) {
        expectExactly(scope.and(retained), expectedRetained);
        removeExactly(scope.and(retained.negate()), expectedRemoved);
    }

    public void replaceOne(Predicate<T> selector, T replacement) {
        replaceKnownOne(requireExactlyOne(selector), replacement);
    }

    /** Replaces an already selected recipe by identity, rejecting absent or repeated references. */
    public void replaceKnownOne(T target, T replacement) {
        Map<T, Integer> index = positions();
        Integer position = index.get(target);
        if (position == null || position < 0)
            throw new RecipeValidationException(store.name() + ": expected one occurrence of the known recipe");
        requireUnregistered(replacement);
        recipes.set(position, replacement);
        index.remove(target);
        index.put(replacement, position);
        modified++;
    }

    public void addOne(T recipe, Predicate<T> signature) {
        requireUnregistered(recipe);
        if (!signature.test(recipe)) throw new RecipeValidationException(store.name() + ": invalid addition signature");
        selectExactly(signature, 0);
        recipes.add(recipe);
        positions.put(recipe, recipes.size() - 1);
        added++;
    }

    private void requireUnregistered(T recipe) {
        Objects.requireNonNull(recipe, "Recipe cannot be null");
        if (positions().containsKey(recipe)) {
            throw new RecipeValidationException(store.name() + ": recipe is already registered");
        }
    }

    private Map<T, Integer> positions() {
        if (positions == null) {
            positions = new IdentityHashMap<>();
            for (int i = 0; i < recipes.size(); i++) {
                T recipe = recipes.get(i);
                // Keep duplicate references ambiguous rather than silently selecting one occurrence.
                if (positions.putIfAbsent(recipe, i) != null) positions.put(recipe, -1);
            }
        }
        return positions;
    }

    private List<T> selectExactly(Predicate<T> selector, int expectedCount) {
        if (expectedCount < 0) throw new IllegalArgumentException("Expected count cannot be negative");
        List<T> matches = recipes.stream()
            .filter(selector)
            .collect(Collectors.toList());
        if (matches.size() != expectedCount) {
            throw new RecipeValidationException(
                store.name() + ": expected " + expectedCount + " matches, observed " + matches.size());
        }
        return matches;
    }

    void commit() {
        if (added + modified + removed > 0) store.replaceAll(new ArrayList<>(recipes));
    }

    void validateUnchanged() {
        List<T> current = store.snapshot();
        if (current.size() != original.size())
            throw new IllegalStateException(store.name() + ": registry changed during planning");
        for (int i = 0; i < original.size(); i++) {
            if (current.get(i) != original.get(i))
                throw new IllegalStateException(store.name() + ": registry changed during planning");
        }
    }

    void rollback() {
        store.replaceAll(new ArrayList<>(original));
    }

    RecipeTransaction.Result result() {
        return new RecipeTransaction.Result(added, modified, removed);
    }
}
