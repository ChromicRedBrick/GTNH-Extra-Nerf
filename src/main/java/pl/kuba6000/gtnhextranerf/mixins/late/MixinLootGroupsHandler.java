package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.entity.player.EntityPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import eu.usrv.enhancedlootbags.core.LootGroupsHandler;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup.Drop;
import eu.usrv.yamcore.persisteddata.PersistedDataBase;
import pl.kuba6000.gtnhextranerf.loader.LootLoader;
import pl.kuba6000.gtnhextranerf.loot.runtime.NativeLootStorage;
import pl.kuba6000.gtnhextranerf.loot.runtime.ServerLootRuntime;

@Mixin(value = LootGroupsHandler.class, remap = false)
public abstract class MixinLootGroupsHandler implements NativeLootStorage {

    @Shadow
    private PersistedDataBase _mPersistedDB;
    @Unique
    private RuntimeException gtnhextranerf$patchFailure;

    @WrapOperation(
        method = "ReloadLootGroups",
        at = @At(
            value = "INVOKE",
            target = "Leu/usrv/enhancedlootbags/core/LootGroupsHandler;VerifyConfig(Leu/usrv/enhancedlootbags/core/serializer/LootGroups;Z)Z"),
        require = 1)
    private boolean gtnhextranerf$patch(LootGroupsHandler handler, LootGroups tables, boolean local,
        Operation<Boolean> original) {
        if (!original.call(handler, tables, local)) return false;
        if (local) {
            try {
                LootLoader.patch(tables);
            } catch (RuntimeException failure) {
                gtnhextranerf$patchFailure = failure;
                return false;
            }
        }
        return true;
    }

    // ELB catches Exception inside ReloadLootGroups. Throw outside that catch so a failed patch cannot use sample
    // tables.
    @Inject(method = "ReloadLootGroups", at = @At("RETURN"), require = 1)
    private void gtnhextranerf$rejectFailure(String xml, CallbackInfoReturnable<Boolean> result) {
        if (gtnhextranerf$patchFailure != null) {
            RuntimeException failure = gtnhextranerf$patchFailure;
            gtnhextranerf$patchFailure = null;
            throw new IllegalStateException("GTNH Extra Nerf integrity check failed: native loot patch", failure);
        }
    }

    @Inject(method = "getDropCount", at = @At("RETURN"), cancellable = true, require = 1)
    private void gtnhextranerf$legacyCounts(EntityPlayer player, LootGroup group, Drop drop,
        CallbackInfoReturnable<Integer> result) {
        result.setReturnValue(
            Math.max(result.getReturnValue(), ServerLootRuntime.previousPlayerCount(player, group, drop)));
    }

    @Override
    public void resetNativeLootStorage() {
        _mPersistedDB = null;
    }
}
