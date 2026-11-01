package pl.kuba6000.gtnhextranerf.recipes;

/** Development may continue only after a known validation mismatch, never an arbitrary exception. */
public enum RecipeFailurePolicy {
    FAIL_FAST,
    CONTINUE_ON_VALIDATION_FAILURE
}
