package pl.kuba6000.gtnhextranerf.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;

/** Actual generated GT items, unification, fluid identities and a pre-change item save. */
final class MaterialIntegrationTests {

    private static final List<ItemStack> alternativesFixture = new ArrayList<>();

    private MaterialIntegrationTests() {}

    static void verify() {
        verifyConcurrentAlternatives();
        Materials finished = Materials.TungstenSteel;
        Materials raw = Materials.get("RawTungstenSteel");
        check(raw != Materials._NULL && raw != finished, "Raw tungstensteel was not registered separately");
        check(
            Arrays.equals(raw.mRGBa, finished.mRGBa),
            "Raw Tungstensteel does not preserve the original material color");
        check(
            finished.getLocalizedName()
                .equals("TungTungTungstensteel"),
            "Finished material name was not applied");
        check(
            raw.getLocalizedName()
                .equals("Tungstensteel"),
            "Raw material display name was not applied");
        check(
            finished.getChemicalFormula()
                .isEmpty(),
            "Finished material still exposes a chemical formula instead of its tooltip image");
        check(
            !raw.getChemicalFormula()
                .isEmpty(),
            "Raw material lost its chemical formula");
        for (OrePrefixes prefix : new OrePrefixes[] { OrePrefixes.dust, OrePrefixes.ingot, OrePrefixes.ingotHot,
            OrePrefixes.plate, OrePrefixes.plateDouble, OrePrefixes.block }) {
            ItemStack rawItem = GTOreDictUnificator.get(prefix, raw, 1);
            ItemStack finishedItem = GTOreDictUnificator.get(prefix, finished, 1);
            check(rawItem != null && finishedItem != null, "Missing generated form: " + prefix);
            check(!rawItem.isItemEqual(finishedItem), "Raw and finished " + prefix + " share the same item");
            check(
                !GTOreDictUnificator.get(rawItem)
                    .isItemEqual(finishedItem),
                "Raw item unified into finished material");
            check(
                !OreDictionary.getOres(
                    prefix.get(finished)
                        .toString())
                    .stream()
                    .anyMatch(rawItem::isItemEqual),
                "Raw item was registered under the finished ore name");
        }
        check(raw.getMolten(144) != null && finished.getMolten(144) != null, "Missing molten material");
        check(
            !raw.getMolten(144)
                .isFluidEqual(finished.getMolten(144)),
            "Raw and finished molten are unified");
        check(
            FluidRegistry.getFluid("molten.tungstensteel") == finished.getMolten(1)
                .getFluid(),
            "Existing fluid identity was replaced");
        NBTTagCompound oldSave = new NBTTagCompound();
        // 1.7 item saves use numeric IDs, resolved through Forge's saved name-to-ID mapping.
        Item oldItem = GameRegistry.findItem("gregtech", "gt.metaitem.01");
        check(oldItem != null, "Existing GT item registry name was removed");
        oldSave.setShort("id", (short) Item.getIdFromItem(oldItem));
        oldSave.setShort("Damage", (short) 11316);
        oldSave.setByte("Count", (byte) 1);
        ItemStack restored = ItemStack.loadItemStackFromNBT(oldSave);
        check(
            restored != null && restored.isItemEqual(GTOreDictUnificator.get(OrePrefixes.ingot, finished, 1)),
            "Existing tungstensteel item save no longer resolves to the finished material");
        ItemStack finishedPlate = GTOreDictUnificator.get(OrePrefixes.plateDouble, finished, 1);
        ItemStack rawPlate = GTOreDictUnificator.get(OrePrefixes.plateDouble, raw, 1);
        ItemStack wrench = GTOreDictUnificator.getFirstOre("craftingToolWrench", 1);
        check(
            ItemList.Casing_IV.isStackEqual(
                CasingFinishingIntegrationTests.finish(
                    LvRecipeIntegrationTests.craft(
                        finishedPlate,
                        finishedPlate,
                        finishedPlate,
                        finishedPlate,
                        wrench,
                        finishedPlate,
                        finishedPlate,
                        finishedPlate,
                        finishedPlate))),
            "Finished plates no longer craft IV casing");
        check(
            !ItemList.Casing_IV.isStackEqual(
                CasingFinishingIntegrationTests.finish(
                    LvRecipeIntegrationTests.craft(
                        rawPlate,
                        rawPlate,
                        rawPlate,
                        rawPlate,
                        wrench,
                        rawPlate,
                        rawPlate,
                        rawPlate,
                        rawPlate))),
            "Raw plates incorrectly craft IV casing");
        boolean assemblerUsesFinished = false;
        for (GTRecipe recipe : RecipeMaps.assemblerRecipes.getAllRecipes()) {
            boolean casing = false;
            for (ItemStack output : recipe.mOutputs) if (output.isItemEqual(
                new ItemStack(GameRegistry.findBlock("gtnhextranerf", "raw_machine_casing"), 1, 4))) casing = true;
            if (!casing) continue;
            for (ItemStack input : recipe.mInputs) if (input != null) {
                check(!input.isItemEqual(rawPlate), "IV casing assembler recipe consumes raw plates");
                if (input.isItemEqual(finishedPlate)) assemblerUsesFinished = true;
            }
        }
        check(assemblerUsesFinished, "IV casing assembler recipe no longer consumes finished plates");
    }

    static void prepareAlternatives() {
        ItemStack copper = GTOreDictUnificator.get(OrePrefixes.ingot, Materials.Copper, 1);
        // Public association registration provides aliases independently of the pack's duplicate ingots.
        Item aliasItem = new Item().setHasSubtypes(true)
            .setUnlocalizedName("integration.copper_alias");
        GameRegistry.registerItem(aliasItem, "unification_alias");
        for (int i = 0; i < 1024; i++) {
            ItemStack alias = new ItemStack(aliasItem, 1, i);
            GTOreDictUnificator.setItemData(alias, GTOreDictUnificator.getAssociation(copper));
            check(
                GTOreDictUnificator.get_nocopy(false, alias)
                    .isItemEqual(copper),
                "fixture alias did not unify to Copper");
            alternativesFixture.add(alias);
        }
    }

    private static void verifyConcurrentAlternatives() {
        ItemStack copper = GTOreDictUnificator.get(OrePrefixes.ingot, Materials.Copper, 3);
        check(!alternativesFixture.isEmpty(), "Unification aliases were not prepared during loading");
        ExecutorService workers = Executors.newFixedThreadPool(8);
        CyclicBarrier start = new CyclicBarrier(8);
        try {
            List<Future<List<ItemStack>>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) results.add(workers.submit(() -> {
                start.await(10, TimeUnit.SECONDS);
                return GTOreDictUnificator.getNonUnifiedStacks(copper.copy());
            }));
            for (Future<List<ItemStack>> result : results) {
                List<ItemStack> alternatives = result.get(30, TimeUnit.SECONDS);
                for (ItemStack alias : alternativesFixture) check(
                    alternatives.stream()
                        .anyMatch(alias::isItemEqual),
                    "parallel unification lookup returned incomplete Copper alternatives: count=" + alternatives.size()
                        + ", missing metadata="
                        + alias.getItemDamage());
                check(
                    alternatives.stream()
                        .allMatch(stack -> stack.stackSize == 3),
                    "unification alternatives changed the requested stack size");
            }
        } catch (Exception failure) {
            throw new AssertionError("parallel native unification lookup failed", failure);
        } finally {
            workers.shutdownNow();
        }
    }

    private static void check(boolean success, String detail) {
        if (!success) throw new AssertionError(detail);
    }
}
