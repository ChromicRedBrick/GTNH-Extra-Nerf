package pl.kuba6000.gtnhextranerf.recipes;

import java.util.List;

/** Adapter for a registry. Snapshots retain recipe identity; replaceAll must also update lookup indexes. */
public interface RecipeStore<T> {

    String name();

    List<T> snapshot();

    void replaceAll(List<T> recipes);
}
