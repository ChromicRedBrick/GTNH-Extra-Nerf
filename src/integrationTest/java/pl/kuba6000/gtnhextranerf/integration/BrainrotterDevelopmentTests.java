package pl.kuba6000.gtnhextranerf.integration;

import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Opt-in fixture for the reduced runtime, built separately from full-pack tests. */
@Mod(modid = "extranerfdevbrainrottertests", version = "1", dependencies = "required-after:gtnhextranerf")
public final class BrainrotterDevelopmentTests {

    @Mod.EventHandler
    public void verify(FMLServerStartingEvent event) {
        try {
            if (!GTNHExtraNerf.isDevelopmentEnvironment()) throw new AssertionError("Expected a deobfuscated runtime");
            BrainrotterIntegrationTests.verifyDevelopment(DimensionManager.getWorld(0));
        } catch (Exception | LinkageError | AssertionError failure) {
            GTNHExtraNerf.LOG.error("Extra Nerf development Brainrotter tests FAILED", failure);
            throw new IllegalStateException("Extra Nerf development Brainrotter tests FAILED", failure);
        }
        GTNHExtraNerf.LOG.info("Extra Nerf development Brainrotter tests PASSED");
    }
}
