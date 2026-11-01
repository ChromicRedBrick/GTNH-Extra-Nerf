package pl.kuba6000.gtnhextranerf.progression;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

public final class ProgressionCertificateItem extends Item {

    private final ProgressionTier tier;

    ProgressionCertificateItem(ProgressionTier tier) {
        this.tier = tier;
        setUnlocalizedName("gtnhextranerf." + registryName());
        setTextureName(GTNHExtraNerf.MODID + ":" + tier.certificateTexture());
        setCreativeTab(CreativeTabs.tabMisc);
        setMaxStackSize(1);
    }

    public String registryName() {
        return tier.id() + "_certificate";
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote || player.getHeldItem() != stack
            || stack.stackSize <= 0
            || ProgressionCertificates.tierOf(stack) != tier) return stack;
        if (!MachineProgression.isCurrentCertificate(world, tier, ProgressionCertificates.issuedEpoch(stack))) {
            player.addChatMessage(
                new ChatComponentTranslation("gtnhextranerf.progression.expired_certificate", stack.getDisplayName()));
            return stack;
        }
        boolean unlocked = MachineProgression.certify(world, tier);
        if (unlocked && !player.capabilities.isCreativeMode) stack.stackSize--;
        player.addChatMessage(
            new ChatComponentTranslation(
                unlocked ? "gtnhextranerf.progression.certified" : "gtnhextranerf.progression.already_certified",
                stack.getDisplayName()));
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal("gtnhextranerf.progression.use"));
        tooltip.add(StatCollector.translateToLocal("gtnhextranerf.progression.global"));
    }
}
