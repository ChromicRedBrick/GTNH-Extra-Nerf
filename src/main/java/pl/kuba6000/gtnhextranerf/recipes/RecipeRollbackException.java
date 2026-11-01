package pl.kuba6000.gtnhextranerf.recipes;

/** Never tolerated in development: a registry could not be restored to a known state. */
public final class RecipeRollbackException extends RuntimeException {

    public RecipeRollbackException(RuntimeException cause) {
        super("Recipe transaction failed and its registries could not be restored", cause);
    }
}
