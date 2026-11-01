package pl.kuba6000.gtnhextranerf.recipes;

/** Registers related modifiers in their intended execution order, without resolving game ingredients. */
@FunctionalInterface
public interface RecipeModule {

    void register(RecipeRegistry registry);
}
