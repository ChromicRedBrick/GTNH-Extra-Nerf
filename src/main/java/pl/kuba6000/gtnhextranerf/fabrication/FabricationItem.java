package pl.kuba6000.gtnhextranerf.fabrication;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A hidden, inert item with an owned pixel texture and localized fictional description. */
public final class FabricationItem extends Item {

    private final String id;

    FabricationItem(String id) {
        this.id = id;
        setUnlocalizedName("gtnhextranerf." + id);
        setTextureName("gtnhextranerf:" + id);
        setCreativeTab(null);
    }

    public String registryName() {
        return id;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add(
            EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("item.gtnhextranerf." + id + ".tooltip"));
    }
}
