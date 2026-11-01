package pl.kuba6000.gtnhextranerf.components;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** A distinct, inert item whose visual appearance comes from an existing GregTech component. */
public final class ComponentItem extends Item {

    private final ComponentTier tier;
    private final ComponentType type;
    private final Supplier<ItemStack> appearance;

    ComponentItem(ComponentTier tier, ComponentType type, Supplier<ItemStack> appearance) {
        this.tier = tier;
        this.type = type;
        this.appearance = appearance;
        setUnlocalizedName("gtnhextranerf." + registryName());
        setCreativeTab(null);
    }

    public String registryName() {
        return tier.id() + "_" + type.id();
    }

    public ComponentTier tier() {
        return tier;
    }

    public ComponentType type() {
        return type;
    }

    public ItemStack appearance() {
        ItemStack source = Objects.requireNonNull(appearance.get(), "Missing appearance for " + registryName());
        if (source.getItem() == null || source.getItem() == this) {
            throw new IllegalStateException("Invalid appearance for " + registryName());
        }
        return source.copy();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted(
            "gtnhextranerf.component.name",
            StatCollector.translateToLocal("gtnhextranerf.component." + type.id()),
            tier.abbreviation());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
        tooltip.add(
            EnumChatFormatting.DARK_GRAY
                + StatCollector.translateToLocal("gtnhextranerf.tier." + tier.id() + ".tooltip"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icons) {
        // GregTech owns the source icons. Borrow them after its texture registration completes.
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        ItemStack source = appearance();
        return source.getItem()
            .getIcon(source, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int metadata, int pass) {
        ItemStack source = appearance();
        return source.getItem()
            .getIcon(source, pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        ItemStack source = appearance();
        return source.getItem()
            .getIcon(source, pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        ItemStack source = appearance();
        return source.getItem()
            .getColorFromItemStack(source, pass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return appearance().getItem()
            .requiresMultipleRenderPasses();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int metadata) {
        ItemStack source = appearance();
        return source.getItem()
            .getRenderPasses(source.getItemDamage());
    }

    @Override
    public int getSpriteNumber() {
        return appearance().getItem()
            .getSpriteNumber();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        ItemStack source = appearance();
        return source.getItem()
            .hasEffect(source, pass);
    }
}
