package pl.kuba6000.gtnhextranerf.progression;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import bartworks.system.material.WerkstoffLoader;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.audit.AuditManager;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.recipes.RecipeValidationException;

/** Checks the entire payment before changing any inventory slot. */
public final class CertificationCost {

    private CertificationCost() {}

    public static ItemStack material(ProgressionTier tier) {
        switch (tier) {
            case STEAM:
                return GTOreDictUnificator.get(OrePrefixes.ingot, Materials.Bronze, 1);
            case LV:
                return GTOreDictUnificator.get(OrePrefixes.cableGt01, Materials.Tin, 1);
            case MV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Steel, 1);
            case HV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Aluminium, 1);
            case EV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.StainlessSteel, 1);
            case IV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Titanium, 1);
            case LUV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.TungstenSteel, 1);
            case ZPM:
                return GTOreDictUnificator
                    .get(OrePrefixes.block, WerkstoffLoader.RhodiumPlatedPalladium.getGTMaterial(), 1);
            case UV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Iridium, 1);
            case UHV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Osmium, 1);
            case UEV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Neutronium, 1);
            case UIV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.Infinity, 1);
            case UMV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.TranscendentMetal, 1);
            case UXV:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.SpaceTime, 1);
            case MAX:
                return GTOreDictUnificator.get(OrePrefixes.block, Materials.MHDCSM, 1);
            default:
                throw new IllegalArgumentException("Missing certification fee: " + tier);
        }
    }

    public static int amount(ProgressionTier tier) {
        switch (tier) {
            case STEAM:
                return 1000;
            case LV:
                return 1000;
            case MV:
                return 556;
            case LUV:
                return 670;
            case HV:
            case EV:
            case IV:
            case ZPM:
            case UV:
            case UHV:
            case UEV:
            case UIV:
            case UMV:
            case UXV:
            case MAX:
                return 1112;
            default:
                throw new IllegalArgumentException("Missing certification fee: " + tier);
        }
    }

    public static boolean take(EntityPlayerMP player, ProgressionTier tier) {
        if (!consume(player.inventory.mainInventory, material(tier), amount(tier))) return false;
        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        player.openContainer.detectAndSendChanges();
        return true;
    }

    public static void verify() {
        AuditManager.run(AuditScope.VERIFY_CERTIFICATION_COSTS, audit -> {
            for (ProgressionTier tier : ProgressionTier.values()) {
                ItemStack material = material(tier);
                String name = material == null ? null : (String) Item.itemRegistry.getNameForObject(material.getItem());
                if (name == null) throw new RecipeValidationException("Missing certification fee material: " + tier);
                if (amount(tier) <= 0 || amount(tier) > 36 * material.getMaxStackSize())
                    throw new IllegalStateException("Invalid certification fee: " + tier);
                if (tier == ProgressionTier.STEAM && !name.startsWith("gregtech:"))
                    throw new IllegalStateException("Steam fee must use GregTech Bronze ingots: " + name);
            }
            audit.success();
        });
    }

    public static boolean consume(ItemStack[] inventory, ItemStack material, int amount) {
        if (material == null || amount <= 0) return false;
        long available = 0;
        for (ItemStack stack : inventory)
            if (stack != null && stack.stackSize > 0 && stack.isItemEqual(material)) available += stack.stackSize;
        if (available < amount) return false;
        for (int slot = 0; slot < inventory.length && amount > 0; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null || stack.stackSize <= 0 || !stack.isItemEqual(material)) continue;
            int taken = Math.min(amount, stack.stackSize);
            stack.stackSize -= taken;
            amount -= taken;
            if (stack.stackSize == 0) inventory[slot] = null;
        }
        return true;
    }
}
