package pl.kuba6000.gtnhextranerf.recipes;

/** An expected recipe/registry contract mismatch, safe to report after transaction rollback. */
public final class RecipeValidationException extends IllegalStateException {

    public RecipeValidationException(String message) {
        super(message);
    }

    public RecipeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
