package pl.kuba6000.gtnhextranerf.recipes;

/** Plans one audited, atomic group of recipe edits. Do not mutate live registries here. */
@FunctionalInterface
public interface RecipeModifier {

    void modify(RecipeContext recipes);
}
