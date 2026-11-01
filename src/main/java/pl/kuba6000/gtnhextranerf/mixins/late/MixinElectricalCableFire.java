package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.implementations.MTECable;
import pl.kuba6000.gtnhextranerf.progression.ElectricalSafety;

@Mixin(value = BaseTileEntity.class, remap = false)
public abstract class MixinElectricalCableFire implements ElectricalSafety.CableFireHook {

    @WrapOperation(
        method = "setToFire",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;)Z",
            remap = true),
        require = 1,
        allow = 1)
    private boolean gtnhextranerf$cableBurned(World world, int x, int y, int z, Block replacement,
        Operation<Boolean> original) {
        BaseTileEntity tile = (BaseTileEntity) (Object) this;
        // Replacement invalidates the tile, so identify the live cable before delegating.
        boolean cable = tile instanceof BaseMetaPipeEntity
            && ((BaseMetaPipeEntity) tile).getMetaTileEntity() instanceof MTECable
            && world.getTileEntity(x, y, z) == tile;
        boolean changed = original.call(world, x, y, z, replacement);
        if (changed && cable && replacement == Blocks.fire)
            ElectricalSafety.record(world, ElectricalSafety.Incident.CABLE_FIRE, x, y, z);
        return changed;
    }
}
