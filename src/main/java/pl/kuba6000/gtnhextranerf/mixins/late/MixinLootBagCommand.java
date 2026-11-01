package pl.kuba6000.gtnhextranerf.mixins.late;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eu.usrv.enhancedlootbags.EnhancedLootBags;
import eu.usrv.enhancedlootbags.server.LootBagCommand;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

@Mixin(value = LootBagCommand.class, remap = false)
public abstract class MixinLootBagCommand {

    @Inject(method = "processCommand", at = @At("HEAD"), cancellable = true, require = 1, remap = true)
    private void gtnhextranerf$freeze(ICommandSender sender, String[] args, CallbackInfo callback) {
        if (args.length == 0) return;
        String command = args[0];
        if (command.equalsIgnoreCase("reload")) {
            boolean loaded = false;
            try {
                loaded = EnhancedLootBags.LootGroupHandler.reload();
            } catch (RuntimeException failure) {
                GTNHExtraNerf.LOG.error("Native loot reload rejected; previous tables retained", failure);
            }
            sender.addChatMessage(
                new ChatComponentTranslation(
                    loaded ? "gtnhextranerf.loot.reload_ok" : "gtnhextranerf.loot.reload_failed"));
            callback.cancel();
            return;
        }
        if (command.equalsIgnoreCase("addloot") || command.equalsIgnoreCase("addinventory")
            || command.equalsIgnoreCase("addgroup")) {
            sender.addChatMessage(new ChatComponentTranslation("gtnhextranerf.loot.edit_file"));
            callback.cancel();
        }
    }
}
