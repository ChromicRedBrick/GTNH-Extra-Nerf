package pl.kuba6000.gtnhextranerf.integration;

import java.util.UUID;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import com.cleanroommc.modularui.api.RecipeViewerSettings;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.gameevent.TickEvent;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEBasicMachineWithRecipe;
import gregtech.api.metatileentity.implementations.MTECable;
import gregtech.api.metatileentity.implementations.MTEFluidPipe;
import gregtech.api.util.GTOreDictUnificator;
import pl.kuba6000.gtnhextranerf.progression.CertificateStatusPanel;
import pl.kuba6000.gtnhextranerf.progression.MachineProgression;
import pl.kuba6000.gtnhextranerf.progression.ProgressionCertificates;
import pl.kuba6000.gtnhextranerf.progression.ProgressionMachine;
import pl.kuba6000.gtnhextranerf.progression.ProgressionServerTasks;
import pl.kuba6000.gtnhextranerf.progression.ProgressionTier;

/** Exercises actual GT tile ticks and native item activation on the disposable integration server. */
final class ProgressionIntegrationTests {

    private ProgressionIntegrationTests() {}

    static void verify(WorldServer world) {
        for (ProgressionTier tier : ProgressionTier.values()) if (!tier.hasExam()) {
            check(MachineProgression.isCertified(world, tier), "paid " + tier + " was not saved globally");
            MachineProgression.revoke(world, tier);
        }
        BaseMetaTileEntity first = furnace(world, ItemList.Machine_LV_E_Furnace, 8);
        BaseMetaTileEntity second = furnace(world, ItemList.Machine_LV_E_Furnace, 9);
        BaseMetaTileEntity mv = furnace(world, ItemList.Machine_MV_E_Furnace, 10);
        BaseMetaTileEntity hv = furnace(world, ItemList.Machine_HV_E_Furnace, 14);
        BaseMetaTileEntity ev = furnace(world, ItemList.Machine_EV_E_Furnace, 15);
        BaseMetaTileEntity iv = furnace(world, ItemList.Machine_IV_E_Furnace, 16);
        BaseMetaTileEntity waiting = furnace(world, ItemList.Machine_LV_E_Furnace, 12);
        MTEBasicMachine idle = (MTEBasicMachine) waiting.getMetaTileEntity();
        idle.mMaxProgresstime = 0;
        idle.mEUt = 0;
        idle.setInventorySlotContents(idle.getInputSlot(), new ItemStack(Blocks.cobblestone, 3));
        MTEBasicMachine machine = (MTEBasicMachine) first.getMetaTileEntity();
        check(machine instanceof ProgressionMachine, "missing machine status mixin");
        check(MachineProgression.isLocked(machine), "new world must require LV certification");
        first.updateEntityProfiled();
        second.updateEntityProfiled();
        mv.updateEntityProfiled();
        waiting.updateEntityProfiled();
        check(
            idle.mMaxProgresstime == 0 && idle.getStackInSlot(idle.getInputSlot()).stackSize == 3,
            "locked idle machine started a recipe");
        check(
            machine.mProgresstime == 0 && machine.mMaxProgresstime == 100,
            "locked machine advanced or discarded work");
        check(first.getStoredEU() == 1_000, "locked machine consumed energy");
        check(machine.getStackInSlot(machine.getInputSlot()).stackSize == 3, "locked machine consumed inputs");
        check(((MTEBasicMachine) mv.getMetaTileEntity()).mProgresstime == 0, "MV bypassed electrical certification");

        FakePlayer player = FakePlayerFactory.get(world, new GameProfile(UUID.randomUUID(), "CertificateTest"));
        BaseMetaTileEntity wiremill = furnace(world, ItemList.Machine_LV_Wiremill, 13);
        PanelSyncManager guiSync = new PanelSyncManager(new ModularSyncManager(false), false);
        ((MTEBasicMachineWithRecipe) wiremill.getMetaTileEntity()).buildUI(
            new PosGuiData(player, wiremill.xCoord, wiremill.yCoord, wiremill.zCoord),
            guiSync,
            new UISettings(RecipeViewerSettings.DUMMY));
        IntSyncValue certificateStatus = (IntSyncValue) guiSync
            .findSyncHandlerNullable(CertificateStatusPanel.SYNC_ID, 0);
        check(
            certificateStatus != null
                && ProgressionTier.fromSyncId(certificateStatus.getIntValue()) == ProgressionTier.STEAM,
            "actual Wiremill MUI2 GUI did not register its missing-certificate status");
        check(!certificateStatus.isAllowC2S(), "certificate status must not accept client-to-server updates");
        player.capabilities.isCreativeMode = false;
        ItemStack certificate = ProgressionCertificates.issue(world, ProgressionTier.LV);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, certificate);
        certificate.getItem()
            .onItemRightClick(certificate, world, player);
        check(certificate.stackSize == 0, "first certification must consume the certificate");
        check(MachineProgression.isLocked(machine), "electrical certificate bypassed Steam prerequisite");
        ItemStack steamCertificate = ProgressionCertificates.issue(world, ProgressionTier.STEAM);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, steamCertificate);
        steamCertificate.getItem()
            .onItemRightClick(steamCertificate, world, player);
        check(steamCertificate.stackSize == 0, "Steam activation did not consume its certificate");
        check(!MachineProgression.isLocked(machine), "certificate did not unlock LV");
        MTEBasicMachine mvMachine = (MTEBasicMachine) mv.getMetaTileEntity();
        check(MachineProgression.missingTier(mvMachine) == ProgressionTier.MV, "MV did not require its own payment");
        mv.updateEntityProfiled();
        check(mvMachine.mProgresstime == 0, "MV processed without its own certification");
        MachineProgression.certify(world, ProgressionTier.MV);
        MTEBasicMachine hvMachine = (MTEBasicMachine) hv.getMetaTileEntity();
        check(MachineProgression.missingTier(hvMachine) == ProgressionTier.HV, "HV did not require its own payment");
        hv.updateEntityProfiled();
        check(hvMachine.mProgresstime == 0, "HV processed without its own certification");
        MachineProgression.certify(world, ProgressionTier.HV);
        MTEBasicMachine evMachine = (MTEBasicMachine) ev.getMetaTileEntity();
        MTEBasicMachine ivMachine = (MTEBasicMachine) iv.getMetaTileEntity();
        check(MachineProgression.missingTier(evMachine) == ProgressionTier.EV, "EV did not require its own payment");
        check(MachineProgression.missingTier(ivMachine) == ProgressionTier.EV, "IV skipped its EV prerequisite");
        ev.updateEntityProfiled();
        iv.updateEntityProfiled();
        check(evMachine.mProgresstime == 0 && ivMachine.mProgresstime == 0, "unpaid EV/IV machines processed");
        MachineProgression.certify(world, ProgressionTier.EV);
        check(MachineProgression.missingTier(ivMachine) == ProgressionTier.IV, "IV did not require its own payment");
        MachineProgression.certify(world, ProgressionTier.IV);
        certificateStatus.updateCacheFromSource(false);
        check(certificateStatus.getIntValue() == 0, "open Wiremill GUI did not observe certification");
        check(!MachineProgression.isLocked((MTEBasicMachine) second.getMetaTileEntity()), "unlock was not global");
        first.updateEntityProfiled();
        waiting.updateEntityProfiled();
        check(
            idle.mMaxProgresstime > 0 && idle.getStackInSlot(idle.getInputSlot()).stackSize < 3,
            "certified idle machine did not immediately recheck its waiting inputs");
        check(machine.mProgresstime > 0 && first.getStoredEU() < 1_000, "certified machine did not resume");

        ItemStack spare = ProgressionCertificates.issue(world, ProgressionTier.LV);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, spare);
        spare.getItem()
            .onItemRightClick(spare, world, player);
        check(spare.stackSize == 1, "duplicate certificate must not be consumed");
        if (DimensionManager.getWorld(-1) == null) DimensionManager.initDimension(-1);
        BaseMetaTileEntity nether = furnace(DimensionManager.getWorld(-1), ItemList.Machine_LV_E_Furnace, 11);
        check(
            !MachineProgression.isLocked((MTEBasicMachine) nether.getMetaTileEntity()),
            "certification differs by dimension");

        ItemStack cableStack = GTOreDictUnificator.get(OrePrefixes.cableGt01, Materials.Copper, 1);
        world.setBlock(20, 239, 8, Blocks.stone);
        ItemStack fluidPipeStack = GTOreDictUnificator.get(OrePrefixes.pipeTiny, Materials.Steel, 1);
        world.setBlock(21, 239, 8, Blocks.stone);
        check(
            ((ItemBlock) fluidPipeStack.getItem())
                .placeBlockAt(fluidPipeStack, player, world, 21, 240, 8, 1, 0.5f, 0.5f, 0.5f, 0),
            "could not place the integration fluid pipe");
        BaseMetaPipeEntity fluidPipe = (BaseMetaPipeEntity) world.getTileEntity(21, 240, 8);
        check(fluidPipe.getMetaTileEntity() instanceof MTEFluidPipe, "integration pipe is not a fluid pipe");
        fluidPipe.setToFire();
        new ProgressionServerTasks().onTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        check(!MachineProgression.isLocked(machine), "a fluid-pipe fire incorrectly revoked electrical certification");
        check(
            ((ItemBlock) cableStack.getItem())
                .placeBlockAt(cableStack, player, world, 20, 240, 8, 1, 0.5f, 0.5f, 0.5f, 0),
            "could not place the integration cable");
        BaseMetaPipeEntity cable = (BaseMetaPipeEntity) world.getTileEntity(20, 240, 8);
        check(cable.getMetaTileEntity() instanceof MTECable, "placed integration pipe is not an electrical cable");
        cable.setToFire();
        check(world.getBlock(20, 240, 8) == Blocks.fire, "native cable fire did not replace the cable");
        new ProgressionServerTasks().onTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        check(MachineProgression.isLocked(machine), "cable fire did not revoke global certification");
        check(
            MachineProgression.isCertified(world, ProgressionTier.MV),
            "electrical accident erased later MV certification");
        check(
            MachineProgression.isCertified(world, ProgressionTier.HV),
            "electrical accident erased later HV certification");
        check(
            MachineProgression.isCertified(world, ProgressionTier.EV)
                && MachineProgression.isCertified(world, ProgressionTier.IV),
            "electrical accident erased later EV/IV certification");
        check(
            MachineProgression.isLocked((MTEBasicMachine) nether.getMetaTileEntity()),
            "cable fire did not revoke certification in another dimension");
        int pausedProgress = machine.mProgresstime;
        first.updateEntityProfiled();
        mv.updateEntityProfiled();
        hv.updateEntityProfiled();
        ev.updateEntityProfiled();
        iv.updateEntityProfiled();
        check(machine.mProgresstime == pausedProgress, "revoked LV machine kept processing");
        check(((MTEBasicMachine) mv.getMetaTileEntity()).mProgresstime == 0, "revoked MV machine kept processing");
        check(hvMachine.mProgresstime == 0, "revoked HV machine kept processing");
        check(evMachine.mProgresstime == 0 && ivMachine.mProgresstime == 0, "revoked EV/IV machines kept processing");
        certificateStatus.updateCacheFromSource(false);
        check(
            ProgressionTier.fromSyncId(certificateStatus.getIntValue()) == ProgressionTier.LV,
            "open GUI did not observe revoked certification");
        player.inventory.setInventorySlotContents(player.inventory.currentItem, spare);
        spare.getItem()
            .onItemRightClick(spare, world, player);
        check(MachineProgression.isLocked(machine), "pre-accident spare certificate restored revoked certification");
        check(spare.stackSize == 1, "expired certificate must not be consumed");

        ItemStack renewed = ProgressionCertificates.issue(world, ProgressionTier.LV);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, renewed);
        renewed.getItem()
            .onItemRightClick(renewed, world, player);
        check(!MachineProgression.isLocked(machine), "fresh certificate did not restore certification");
        ItemStack explodingStack = ItemList.Machine_LV_E_Furnace.get(1);
        check(
            ((ItemBlock) explodingStack.getItem())
                .placeBlockAt(explodingStack, player, world, 24, 240, 8, 1, 0.5f, 0.5f, 0.5f, 0),
            "could not place the integration machine");
        BaseMetaTileEntity exploding = (BaseMetaTileEntity) world.getTileEntity(24, 240, 8);
        exploding.doExplosion(128);
        check(world.getBlock(24, 240, 8) != GregTechAPI.sBlockMachines, "native explosion did not remove the machine");
        new ProgressionServerTasks().onTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
        check(MachineProgression.isLocked(machine), "native machine explosion did not revoke certification");
        ItemStack finalCertificate = ProgressionCertificates.issue(world, ProgressionTier.LV);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, finalCertificate);
        finalCertificate.getItem()
            .onItemRightClick(finalCertificate, world, player);
        check(!MachineProgression.isLocked(machine), "second recertification did not restore machines");
        mv.updateEntityProfiled();
        check(((MTEBasicMachine) mv.getMetaTileEntity()).mProgresstime > 0, "recertified MV machine did not resume");
        hv.updateEntityProfiled();
        check(hvMachine.mProgresstime > 0, "recertified HV machine did not resume");
        ev.updateEntityProfiled();
        iv.updateEntityProfiled();
        check(evMachine.mProgresstime > 0 && ivMachine.mProgresstime > 0, "recertified EV/IV machines did not resume");
        world.setBlockToAir(20, 240, 8);
        world.setBlockToAir(20, 239, 8);
        world.setBlockToAir(21, 240, 8);
        world.setBlockToAir(21, 239, 8);
        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
    }

    static BaseMetaTileEntity furnace(WorldServer world, ItemList type, int x) {
        BaseMetaTileEntity tile = new BaseMetaTileEntity();
        tile.setWorldObj(world);
        tile.xCoord = x;
        tile.yCoord = 240;
        tile.zCoord = 8;
        tile.setInitialValuesAsNBT(
            new NBTTagCompound(),
            (short) type.get(1)
                .getItemDamage());
        tile.setStoredEU(1_000);
        MTEBasicMachine machine = (MTEBasicMachine) tile.getMetaTileEntity();
        if (machine.isSteampowered()) tile.setStoredSteam(10_000);
        machine.mMaxProgresstime = 100;
        machine.mProgresstime = 0;
        machine.mEUt = 8;
        machine.setInventorySlotContents(machine.getInputSlot(), new ItemStack(Blocks.iron_ore, 3));
        return tile;
    }

    private static void check(boolean success, String detail) {
        if (!success) throw new AssertionError(detail);
    }
}
