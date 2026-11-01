package pl.kuba6000.gtnhextranerf.integration;

import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.oredict.OreDictionary;

import com.cricketcraft.chisel.api.carving.CarvingUtils;
import com.cricketcraft.chisel.api.carving.ICarvingRegistry;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;
import team.chisel.init.ChiselItems;
import team.chisel.inventory.ContainerChisel;
import team.chisel.inventory.InventoryChiselSelection;

/** Native carving groups and GT Chisel discovery, without a separate finishing recipe system. */
final class CasingFinishingIntegrationTests {

    private CasingFinishingIntegrationTests() {}

    static void verify() {
        Block block = GameRegistry.findBlock("gtnhextranerf", "raw_machine_casing");
        check(block != null, "Raw machine casings are not registered");
        ItemList[] finished = { ItemList.Casing_LV, ItemList.Casing_MV, ItemList.Casing_HV, ItemList.Casing_EV,
            ItemList.Casing_IV, ItemList.Casing_LuV, ItemList.Casing_ZPM, ItemList.Casing_UV, ItemList.Casing_MAX,
            ItemList.Casing_UEV, ItemList.Casing_UIV, ItemList.Casing_UMV, ItemList.Casing_UXV, ItemList.Casing_MAXV };
        ICarvingRegistry carving = CarvingUtils.getChiselRegistry();
        check(carving != null, "Native Chisel registry is missing");
        for (int tier = 0; tier < finished.length; tier++) {
            ItemStack raw = new ItemStack(block, 1, tier), result = finished[tier].get(1);
            for (ItemStack input : new ItemStack[] { raw, result }) {
                List<ItemStack> options = carving.getItemsForChiseling(input);
                check(options.size() == 2, "Casing carving crosses tiers: " + finished[tier]);
                check(
                    options.stream()
                        .anyMatch(raw::isItemEqual),
                    "Raw variant missing from carving");
                check(
                    options.stream()
                        .anyMatch(result::isItemEqual),
                    "Finished variant missing from carving");
            }
            check(OreDictionary.getOreIDs(raw).length == 0, "Raw casing aliases a finished ingredient");
            GTRecipe conversion = RecipeMaps.industrialChiselRecipes.findRecipeQuery()
                .items(raw.copy(), GTUtility.getIntegratedCircuit(2))
                .voltage(TierEU.RECIPE_LV)
                .find();
            check(conversion != null && result.isItemEqual(conversion.mOutputs[0]), "GT Chisel cannot finish casing");
            ItemStack paid = raw.copy();
            check(
                conversion.isRecipeInputEqual(true, null, paid, GTUtility.getIntegratedCircuit(2)),
                "GT Chisel rejects raw input");
            check(paid.stackSize == 0, "GT Chisel did not consume raw casing");
            check(
                RecipeMaps.industrialChiselRecipes.getAllRecipes()
                    .stream()
                    .anyMatch(recipe -> result.isItemEqual(recipe.mOutputs[0]) && recipe.mInputs[0].isItemEqual(raw)),
                "GT Chisel NEI omitted raw casing");
            if (tier == 13) continue; // MAX has a group but no invented production route.
            check(
                RecipeMaps.assemblerRecipes.getAllRecipes()
                    .stream()
                    .noneMatch(recipe -> result.isItemEqual(recipe.mOutputs[0])),
                "Assembler bypasses finishing");
            long count = RecipeMaps.assemblerRecipes.getAllRecipes()
                .stream()
                .filter(recipe -> raw.isItemEqual(recipe.mOutputs[0]))
                .count();
            check(count == 1, "Raw casing assembler production is missing or ambiguous");
        }
    }

    static ItemStack finish(ItemStack input) {
        if (input == null) return null;
        List<ItemStack> variants = CarvingUtils.getChiselRegistry()
            .getItemsForChiseling(input);
        return variants.size() == 2 ? variants.get(1) : input;
    }

    static void verifyHandChisel(WorldServer world) {
        FakePlayer player = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "CasingChiselTest"));
        ItemStack tool = new ItemStack(ChiselItems.chisel);
        player.inventory.currentItem = 0;
        player.inventory.setInventorySlotContents(0, tool);
        InventoryChiselSelection inventory = new InventoryChiselSelection(tool);
        ContainerChisel container = new ContainerChisel(player.inventory, inventory);
        ItemStack raw = new ItemStack(GameRegistry.findBlock("gtnhextranerf", "raw_machine_casing"), 3, 0);
        container.getSlot(InventoryChiselSelection.normalSlots)
            .putStack(raw);
        check(
            ItemList.Casing_LV.isStackEqual(
                container.getSlot(1)
                    .getStack()),
            "Hand Chisel omitted finished LV casing");
        container.slotClick(1, 0, 0, player);
        ItemStack result = player.inventory.getItemStack();
        check(
            result != null && ItemList.Casing_LV.isStackEqual(result) && result.stackSize == 3,
            "Hand Chisel did not finish the selected batch");
        check(inventory.getStackInSpecialSlot() == null, "Hand Chisel duplicated raw casings");
        player.inventory.setItemStack(null);
        container.onContainerClosed(player);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
