package pl.kuba6000.gtnhextranerf.client.nei;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import codechicken.nei.recipe.ICraftingHandler;
import pl.kuba6000.gtnhextranerf.progression.ProgressionCertificates;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

class CertificateRecipeHandlerTest {

    @Test
    void errorLinkAndItemQueriesOpenOnlyTheRequestedCertificate() {
        CertificateRecipeHandler handler = new CertificateRecipeHandler();
        ItemStack certificate = ProgressionCertificates.stack(ProgressionTier.LV);
        for (ProgressionTier tier : ProgressionTier.values()) {
            ItemStack requested = ProgressionCertificates.stack(tier);
            for (ICraftingHandler page : new ICraftingHandler[] {
                handler.getRecipeHandler(CertificateRecipeHandler.ID, tier.id()),
                handler.getRecipeHandler("item", requested) }) {
                assertEquals(1, page.numRecipes());
                assertSame(requested.getItem(), page.getResultStack(0).item.getItem());
                assertTrue(
                    page.getIngredientStacks(0)
                        .isEmpty());
            }
        }
        assertEquals(
            1,
            handler.getUsageHandler("item", certificate)
                .numRecipes());
        assertEquals(
            0,
            handler.getRecipeHandler(CertificateRecipeHandler.ID, "unknown")
                .numRecipes());
        assertEquals(
            0,
            handler.getRecipeHandler("item", new ItemStack(new Item()))
                .numRecipes());
        assertEquals(
            0,
            handler.getUsageHandler("item", new ItemStack(new Item()))
                .numRecipes());
        assertEquals(
            0,
            handler.getRecipeHandler("item", new ItemStack(certificate.getItem(), 1, 7))
                .numRecipes());
    }

    @Test
    void highestRequiredCertificationFollowsTheMachineTier() {
        assertSame(ProgressionTier.LV, ProgressionTier.forMachine(1, false));
        assertSame(ProgressionTier.MV, ProgressionTier.forMachine(2, false));
        assertSame(ProgressionTier.HV, ProgressionTier.forMachine(3, false));
        assertSame(ProgressionTier.EV, ProgressionTier.forMachine(4, false));
        assertSame(ProgressionTier.IV, ProgressionTier.forMachine(5, false));
        for (int tier : new int[] { 14, 15 }) assertSame(ProgressionTier.MAX, ProgressionTier.forMachine(tier, false));
        for (int tier : new int[] { -1, 0 }) assertNull(ProgressionTier.forMachine(tier, false));
        ItemStack copy = ProgressionCertificates.stack(ProgressionTier.LV);
        copy.stackSize = 0;
        assertEquals(1, ProgressionCertificates.stack(ProgressionTier.LV).stackSize);
    }

    @Test
    void steamMachinesHaveTheirOwnCertificationRegardlessOfNumericTier() {
        for (int tier : new int[] { 0, 1, 2 }) {
            ProgressionTier certificate = ProgressionTier.forMachine(tier, true);
            assertNotNull(certificate);
            assertNotSame(ProgressionTier.LV, certificate);
        }
    }
}
