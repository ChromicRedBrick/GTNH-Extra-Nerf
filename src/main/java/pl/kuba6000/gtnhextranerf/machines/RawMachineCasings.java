package pl.kuba6000.gtnhextranerf.machines;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import com.cricketcraft.chisel.api.carving.CarvingUtils;
import com.cricketcraft.chisel.api.carving.ICarvingRegistry;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Mods;
import gregtech.api.recipe.RecipeMaps;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

/** One persisted metadata block; the ordinary Chisel registry owns finishing. */
public final class RawMachineCasings extends Block {

    // Persisted metadata order: never reorder tiers in existing saves. Casing_MAX is UHV.
    private static final ItemList[] FINISHED = { ItemList.Casing_LV, ItemList.Casing_MV, ItemList.Casing_HV,
        ItemList.Casing_EV, ItemList.Casing_IV, ItemList.Casing_LuV, ItemList.Casing_ZPM, ItemList.Casing_UV,
        ItemList.Casing_MAX, ItemList.Casing_UEV, ItemList.Casing_UIV, ItemList.Casing_UMV, ItemList.Casing_UXV,
        ItemList.Casing_MAXV };
    private static RawMachineCasings block;

    private RawMachineCasings() {
        super(Material.iron);
        setBlockName("gtnhextranerf.raw_machine_casing");
        setHardness(3.5f);
        setResistance(10);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public static void register() {
        block = new RawMachineCasings();
        GameRegistry.registerBlock(block, Subtypes.class, "raw_machine_casing");
    }

    /** ULV and a reduced dev instance without Chisel keep their original output. */
    public static ItemStack productionOutput(ItemStack finished) {
        if (Mods.Chisel.isModLoaded()) {
            for (int tier = 0; tier < FINISHED.length; tier++)
                if (FINISHED[tier].isStackEqual(finished)) return new ItemStack(block, finished.stackSize, tier);
        }
        return finished.copy();
    }

    public static void registerGroups() {
        if (!Mods.Chisel.isModLoaded()) return;
        ICarvingRegistry carving = CarvingUtils.getChiselRegistry();
        check(carving != null, "Native Chisel registry is missing");
        for (int tier = 0; tier < FINISHED.length; tier++) {
            ItemStack finished = FINISHED[tier].get(1);
            String group = GTNHExtraNerf.MODID + ".machine_casing." + GTValues.VN[tier + 1];
            check(carving.getGroup(group) == null, "Casing carving group is already registered: " + group);
            carving.addVariation(group, block, tier, 0);
            carving.addVariation(group, Block.getBlockFromItem(finished.getItem()), finished.getItemDamage(), 1);
        }
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_RAW_CASING_FINISHING, audit -> {
            check(Mods.Chisel.isModLoaded(), "Chisel is required for raw casing finishing");
            ICarvingRegistry carving = CarvingUtils.getChiselRegistry();
            check(carving != null, "Native Chisel registry is missing");
            for (int tier = 0; tier < FINISHED.length; tier++) {
                ItemStack raw = new ItemStack(block, 1, tier), finished = FINISHED[tier].get(1);
                for (ItemStack input : new ItemStack[] { raw, finished }) {
                    List<ItemStack> options = carving.getItemsForChiseling(input);
                    check(
                        options.size() == 2 && options.stream()
                            .anyMatch(raw::isItemEqual)
                            && options.stream()
                                .anyMatch(finished::isItemEqual),
                        "Casing group is not an isolated pair");
                }
                check(
                    RecipeMaps.industrialChiselRecipes.getAllRecipes()
                        .stream()
                        .anyMatch(
                            recipe -> finished.isItemEqual(recipe.mOutputs[0]) && raw.isItemEqual(recipe.mInputs[0])),
                    "Native Chisel NEI cache omitted casing finishing");
                if (tier == FINISHED.length - 1) continue;
                check(
                    RecipeMaps.assemblerRecipes.getAllRecipes()
                        .stream()
                        .noneMatch(recipe -> finished.isItemEqual(recipe.mOutputs[0])),
                    "Assembler bypasses casing finishing");
                long rawCrafts = 0;
                for (Object entry : CraftingManager.getInstance()
                    .getRecipeList()) {
                    ItemStack output = ((IRecipe) entry).getRecipeOutput();
                    if (output == null) continue;
                    check(!finished.isItemEqual(output), "Hand crafting bypasses casing finishing");
                    if (raw.isItemEqual(output)) rawCrafts++;
                }
                check(rawCrafts == 1, "Raw casing hand recipe is missing or ambiguous");
                check(
                    RecipeMaps.assemblerRecipes.getAllRecipes()
                        .stream()
                        .filter(recipe -> raw.isItemEqual(recipe.mOutputs[0]))
                        .count() == 1,
                    "Raw casing assembler recipe is missing or ambiguous");
            }
            audit.success();
        });
    }

    @Override
    public int damageDropped(int metadata) {
        return metadata;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        ItemStack finished = FINISHED[Math.max(0, Math.min(metadata, FINISHED.length - 1))].get(1);
        return Block.getBlockFromItem(finished.getItem())
            .getIcon(side, finished.getItemDamage());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int metadata) {
        return 0x999999;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> stacks) {
        if (Mods.Chisel.isModLoaded())
            for (int tier = 0; tier < FINISHED.length; tier++) stacks.add(new ItemStack(item, 1, tier));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new RecipeValidationException(message);
    }

    public static final class Subtypes extends ItemBlock {

        public Subtypes(Block block) {
            super(block);
            setHasSubtypes(true);
            setMaxDamage(0);
        }

        @Override
        public int getMetadata(int damage) {
            return damage;
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            int tier = Math.max(0, Math.min(stack.getItemDamage(), FINISHED.length - 1));
            return StatCollector
                .translateToLocalFormatted("tile.gtnhextranerf.raw_machine_casing.name", GTValues.VN[tier + 1]);
        }
    }
}
