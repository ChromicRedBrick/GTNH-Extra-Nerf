package pl.kuba6000.gtnhextranerf.loader;

import cpw.mods.fml.common.registry.GameRegistry;
import pl.kuba6000.gtnhextranerf.components.ComponentItem;
import pl.kuba6000.gtnhextranerf.components.ComponentRegistry;
import pl.kuba6000.gtnhextranerf.fabrication.FabricationItem;
import pl.kuba6000.gtnhextranerf.fabrication.UfoAssemblyRegistry;

/** Owns the mod's component catalog and registers its items during common pre-initialization. */
public final class ComponentLoader {

    private static final ComponentRegistry COMPONENTS = new ComponentRegistry();
    private static final UfoAssemblyRegistry FABRICATION = new UfoAssemblyRegistry();
    private static boolean registered;

    private ComponentLoader() {}

    public static void register() {
        if (registered) throw new IllegalStateException("Components have already been registered");
        for (ComponentItem item : COMPONENTS.items()) {
            GameRegistry.registerItem(item, item.registryName());
        }
        for (FabricationItem item : FABRICATION.items()) {
            GameRegistry.registerItem(item, item.registryName());
        }
        registered = true;
    }

    public static ComponentRegistry components() {
        return COMPONENTS;
    }

    public static UfoAssemblyRegistry fabrication() {
        return FABRICATION;
    }
}
