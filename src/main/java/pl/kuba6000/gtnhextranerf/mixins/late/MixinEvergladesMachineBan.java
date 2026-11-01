package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Mods;
import gregtech.api.enums.SoundResource;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.util.GTUtility;
import pl.kuba6000.gtnhextranerf.machines.ProgressionMachineNerf;
import toxiceverglades.world.WorldProviderMod;

@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinEvergladesMachineBan implements ProgressionMachineNerf.EvergladesSafetyHook {

    @Inject(method = "updateEntityProfiled", at = @At("HEAD"), cancellable = true, require = 1, allow = 1)
    private void gtnhextranerf$toxicMachines(CallbackInfo callback) {
        BaseMetaTileEntity tile = (BaseMetaTileEntity) (Object) this;
        World world = tile.getWorld();
        if (world == null || world.isRemote
            || !tile.canAccessData()
            || !Mods.GTPlusPlusEverglades.isModLoaded()
            || !(world.provider instanceof WorldProviderMod)) return;
        int x = tile.xCoord, y = tile.yCoord, z = tile.zCoord;
        // Native inventory drops, pollution and certificate accident handling run first.
        tile.doExplosion(GTValues.V[1]);
        if (world.getTileEntity(x, y, z) == tile) {
            // TecTech and some GT++ machines intentionally override doExplosion with an empty body.
            GTUtility
                .sendSoundToPlayers(world, SoundResource.IC2_MACHINES_MACHINE_OVERLOAD, 1f, -1, x + .5, y + .5, z + .5);
            world.setBlockToAir(x, y, z);
            if (GregTechAPI.sMachineExplosions) world.createExplosion(
                null,
                x + .5,
                y + .5,
                z + .5,
                GTValues.getExplosionPowerForVoltage(GTValues.V[1]),
                true);
        }
        callback.cancel();
    }
}
