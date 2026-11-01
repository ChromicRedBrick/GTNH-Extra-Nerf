package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rwtema.extrautils.item.ItemHealingAxe;

import pl.kuba6000.gtnhextranerf.items.HungryAxe;

/** The Healing Axe becomes the Hungry Axe once it is in a player's inventory: it takes hunger instead of giving it. */
@Mixin(value = ItemHealingAxe.class, remap = false)
public abstract class MixinHealingAxeHunger extends Item {

    /** The inventory ticks every stack it holds, so this is the moment a player has taken the axe. */
    @Inject(method = "onUpdate", remap = true, at = @At("HEAD"), require = 1)
    private void gtnhextranerf$taken(ItemStack stack, World world, Entity holder, int slot, boolean held,
        CallbackInfo callback) {
        if (!world.isRemote && holder instanceof EntityPlayer) HungryAxe.mark(stack);
    }

    /** The axe's held effect gives one hunger point every two seconds; the nerf takes one instead. */
    @WrapOperation(
        method = "onUpdate",
        remap = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FoodStats;addStats(IF)V"),
        require = 1)
    private void gtnhextranerf$hunger(FoodStats stats, int food, float saturation, Operation<Void> original) {
        HungryAxe.drain(stats);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return HungryAxe.isMarked(stack) ? HungryAxe.displayName() : super.getItemStackDisplayName(stack);
    }
}
