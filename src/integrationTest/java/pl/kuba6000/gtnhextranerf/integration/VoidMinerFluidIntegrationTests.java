package pl.kuba6000.gtnhextranerf.integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import bartworks.common.loaders.ItemRegistry;
import bartworks.system.material.WerkstoffLoader;
import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import bwcrossmod.galacticgreg.MTEVoidMiners;
import gregtech.api.GregTechAPI;
import gregtech.api.casing.Casings;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Real formed miners, native gas selection and powered output; tuning is checked through relationships. */
final class VoidMinerFluidIntegrationTests {

    private VoidMinerFluidIntegrationTests() {}

    static void verify(WorldServer world) {
        int[] mk1 = verifyTier(world, MTEVoidMiners.VMLUV.class, Casings.MiningOsmiridiumCasing);
        int[] mk2 = verifyTier(world, MTEVoidMiners.VMZPM.class, Casings.MiningBlackPlutoniumCasing);
        int[] mk3 = verifyTier(world, MTEVoidMiners.VMUV.class, Casings.MiningNeutroniumCasing);
        check(mk1[0] < mk1[1] && mk1[1] < mk1[2] && mk1[2] < mk1[3], "MK1 lost its Neon preference");
        check(mk2[1] < mk2[0] && mk2[0] < mk2[2] && mk2[2] < mk2[3], "MK2 lost its Krypton preference");
        check(mk3[0] > mk3[1] && mk3[1] > mk3[2] && mk3[2] == mk3[3], "MK3 lost its Xenon/Oganesson preference");
        check(mk1[0] == mk2[1] && mk2[1] == mk3[2], "Preferred gases have different minimum costs");
        check(mk1[0] < mk2[0] && mk2[0] < mk3[0], "Neon did not become costlier in higher miners");
        check(mk1[2] > mk2[2] && mk2[2] > mk3[2], "Xenon did not become cheaper in higher miners");
        check(mk1[3] > mk2[3] && mk2[3] > mk3[3], "Oganesson did not become cheaper in higher miners");
        GTNHExtraNerf.LOG.info("Extra Nerf Void Miner fluid integration tests PASSED");
    }

    private static int[] verifyTier(WorldServer world, Class<? extends MTEVoidMinerBase<?>> type, Casings casing) {
        try (Miner miner = new Miner(world, type, casing)) {
            miner.tick(160);
            miner.start(null, false);
            int baseline = miner.finish();
            int previous = baseline;
            FluidStack[] gases = { WerkstoffLoader.Neon.getFluidOrGas(miner.first.getCapacity()),
                WerkstoffLoader.Krypton.getFluidOrGas(miner.first.getCapacity()),
                WerkstoffLoader.Xenon.getFluidOrGas(miner.first.getCapacity()),
                WerkstoffLoader.Oganesson.getFluidOrGas(miner.first.getCapacity()) };
            int[] costs = new int[gases.length];
            for (int i = 0; i < gases.length; i++) {
                FluidStack gas = gases[i];
                int cost = miner.start(gas, false);
                int duration = miner.machine.mMaxProgresstime;
                check(cost > 1, type.getSimpleName() + " retained vanilla gas consumption");
                int boosted = miner.finish();
                check(boosted > previous, "Native gas boost ordering changed: " + gas.getLocalizedName());
                previous = boosted;
                costs[i] = cost;
                GTNHExtraNerf.LOG.info(
                    "Void Miner gas cost: {} / {} = {} mB per cycle",
                    type.getSimpleName(),
                    gas.getLocalizedName(),
                    cost);

                FluidStack shortSupply = gas.copy();
                shortSupply.amount = cost - 1;
                check(miner.start(shortSupply, false) == 0, "Insufficient gas was partially consumed");
                check(miner.finish() == baseline, "Insufficient gas granted a boost or stopped baseline mining");

                FluidStack exactSupply = gas.copy();
                exactSupply.amount = cost;
                check(miner.start(exactSupply, false) == cost, "The exact gas cost was not accepted");
                check(miner.first.getFluidAmount() == 0, "Exact payment left gas behind");
                check(miner.finish() == boosted, "Exact payment lost the native boost");

                int batchCost = miner.start(gas, true);
                int batchDuration = miner.machine.mMaxProgresstime;
                check(batchDuration > duration && batchDuration % duration == 0, "Native batch duration changed");
                check(
                    batchCost == (long) cost * (batchDuration / duration),
                    "Batch gas cost overflowed or scaled incorrectly");
                miner.machine.stopMachine(ShutDownReasonRegistry.NONE);
            }
            FluidStack water = Materials.Water.getFluid(1_000);
            check(miner.start(water, false) == 0, "Non-booster fluid was consumed");
            check(miner.finish() == baseline, "Non-booster fluid changed baseline mining");
            return costs;
        }
    }

    private static final class Miner implements AutoCloseable {

        final WorldServer world;
        final BaseMetaTileEntity controller, energy, firstTile, secondTile, output;
        final MTEVoidMinerBase<?> machine;
        final MTEHatchInput first, second;
        final BaseMetaTileEntity[] tiles;

        Miner(WorldServer world, Class<? extends MTEVoidMinerBase<?>> type, Casings casing) {
            this.world = world;
            MetaTileEntity prototype = (MetaTileEntity) Arrays.stream(GregTechAPI.METATILEENTITIES)
                .filter(type::isInstance)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing native miner: " + type));
            controller = TransformerIntegrationTests.placeAt(world, prototype.getStackForm(1), 940, 210, 192);
            // Remove generated terrain before constructing the isolated fixture.
            for (int x = 934; x <= 946; x++) for (int y = 200; y <= 220; y++) for (int z = 186; z <= 204; z++)
                if (x != controller.xCoord || y != controller.yCoord || z != controller.zCoord)
                    world.setBlockToAir(x, y, z);
            controller.setFrontFacing(ForgeDirection.NORTH);
            machine = (MTEVoidMinerBase<?>) controller.getMetaTileEntity();
            machine.setBatchMode(false);
            machine.construct(prototype.getStackForm(1), false);
            List<int[]> floor = new ArrayList<>();
            int lowest = Integer.MAX_VALUE;
            for (int x = 934; x <= 946; x++) for (int y = 200; y <= 220; y++) for (int z = 186; z <= 204; z++) {
                if (world.getBlock(x, y, z) != casing.getBlock()
                    || world.getBlockMetadata(x, y, z) != casing.getBlockMeta()) continue;
                if (y < lowest) {
                    lowest = y;
                    floor.clear();
                }
                if (y == lowest) floor.add(new int[] { x, y, z });
            }
            check(floor.size() >= 5, "Native miner builder omitted hatch positions");
            int[] position = floor.get(0);
            BaseMetaTileEntity maintenance = TransformerIntegrationTests
                .placeAt(world, ItemList.Hatch_Maintenance.get(1), position[0], position[1], position[2]);
            position = floor.get(1);
            output = TransformerIntegrationTests
                .placeAt(world, ItemList.Hatch_Output_Bus_UV.get(1), position[0], position[1], position[2]);
            position = floor.get(2);
            energy = TransformerIntegrationTests
                .placeAt(world, ItemList.Hatch_Energy_UV.get(1), position[0], position[1], position[2]);
            position = floor.get(3);
            firstTile = TransformerIntegrationTests
                .placeAt(world, ItemRegistry.humongousInputHatch.copy(), position[0], position[1], position[2]);
            position = floor.get(4);
            secondTile = TransformerIntegrationTests
                .placeAt(world, ItemList.Hatch_Input_UV.get(1), position[0], position[1], position[2]);
            first = (MTEHatchInput) firstTile.getMetaTileEntity();
            second = (MTEHatchInput) secondTile.getMetaTileEntity();
            tiles = new BaseMetaTileEntity[] { maintenance, output, energy, firstTile, secondTile, controller };
            check(machine.checkStructure(true, controller), "Native Void Miner structure did not form");
            machine.fixAllIssues();
            machine.onFirstTick(controller);
            check(
                machine.checkProcessing()
                    .wasSuccessful(),
                "Void miner fixture cannot mine without gas: " + machine.getCheckRecipeResult()
                    .writeToNBT(new NBTTagCompound()));
            machine.stopMachine(ShutDownReasonRegistry.NONE);
            controller.enableWorking();
        }

        long tick(int count) {
            long used = 0;
            for (int tick = 0; tick < count; tick++) {
                energy.setStoredEU(energy.getEUCapacity());
                long before = energy.getStoredEU();
                for (BaseMetaTileEntity tile : tiles) tile.updateEntityProfiled();
                used += before - energy.getStoredEU();
            }
            return used;
        }

        int start(FluidStack gas, boolean batch) {
            machine.stopMachine(ShutDownReasonRegistry.NONE);
            machine.setBatchMode(batch);
            clearOutput();
            first.drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, true);
            if (gas != null)
                check(first.fill(ForgeDirection.UNKNOWN, gas, true) == gas.amount, "Fixture cannot hold gas");
            controller.enableWorking();
            check(
                machine.checkProcessing()
                    .wasSuccessful(),
                "Miner rejected a native booster or baseline cycle");
            controller.disableWorking();
            return gas == null ? 0 : gas.amount - first.getFluidAmount();
        }

        int finish() {
            check(tick(240) > 0, "Started miner cycle did not use energy");
            int produced = outputAmount();
            check(produced > 0, "Started miner cycle did not produce ore");
            return produced;
        }

        int outputAmount() {
            int count = 0;
            for (int slot = 0; slot < output.getSizeInventory(); slot++)
                if (output.getStackInSlot(slot) != null) count += output.getStackInSlot(slot).stackSize;
            return count;
        }

        void clearOutput() {
            for (int slot = 0; slot < output.getSizeInventory(); slot++) output.setInventorySlotContents(slot, null);
        }

        @Override
        public void close() {
            for (int x = 934; x <= 946; x++)
                for (int y = 200; y <= 220; y++) for (int z = 186; z <= 204; z++) world.setBlockToAir(x, y, z);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
