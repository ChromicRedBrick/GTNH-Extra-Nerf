package pl.kuba6000.gtnhextranerf.integration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.util.WorldCoord;
import appeng.spatial.StorageWorldProvider;
import ggfab.GGItemList;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Mods;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.structure.error.StructureError;
import gregtech.api.structure.error.StructureErrorRegistry;
import gregtech.api.structure.error.StructureErrors;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTScannerResult;
import gregtech.loaders.postload.ScannerHandlerLoader;
import io.netty.buffer.Unpooled;
import pl.kuba6000.gtnhextranerf.GTNHExtraNerf;

/** Native structures, real AE2-created Spatial worlds, powered processing and client error serialization. */
final class SpatialAssemblyIntegrationTests {

    private static final String ERROR_KEY = "gtnhextranerf.structure_error.unfluix_world";
    private static final StructureError DIMENSION_ERROR = StructureErrors.of(ERROR_KEY);

    private SpatialAssemblyIntegrationTests() {}

    static void verify(WorldServer overworld) throws Exception {
        for (ItemStack controller : new ItemStack[] { ItemList.Machine_Multi_Assemblyline.get(1),
            GGItemList.AdvAssLine.get(1) }) {
            try (Line line = new Line(overworld, controller, 1200, 210, 320)) {
                check(!line.machine.checkStructure(true, line.tile), "Assembly Line formed outside AE2 Spatial");
                List<StructureError> errors = line.errors();
                check(
                    errors.size() == 1 && errors.contains(DIMENSION_ERROR),
                    "Complete line lacks its dimension error: " + errors);
                PacketBuffer packet = new PacketBuffer(Unpooled.buffer());
                try {
                    StructureErrorRegistry.serialize(packet, errors.get(0));
                    check(
                        DIMENSION_ERROR.equals(StructureErrorRegistry.deserialize(packet)),
                        "Dimension error did not reach the client");
                } finally {
                    packet.release();
                }
                int[] block = line.casings(5, line.tile.yCoord - 1)
                    .get(0);
                overworld.setBlockToAir(block[0], block[1], block[2]);
                check(!line.machine.checkStructure(true, line.tile), "Incomplete Assembly Line formed");
                check(
                    !line.errors()
                        .contains(DIMENSION_ERROR),
                    "Incomplete structure revealed the dimension gate");
                overworld.setBlock(block[0], block[1], block[2], GregTechAPI.sBlockCasings2, 5, 3);
                check(!line.machine.checkStructure(true, line.tile), "Repaired line bypassed the dimension gate");
                line.loadRecipe();
                int items = line.items();
                int fluid = line.input.getFluidAmount();
                check(
                    line.tick(200) == 0 && line.items() == items
                        && line.input.getFluidAmount() == fluid
                        && line.output() == 0,
                    "Dimension-locked Assembly Line consumed resources or produced an item");
            }

            WorldServer spatial = spatialWorld(overworld);
            try (Line line = new Line(spatial, controller, 6, 68, 6)) {
                check(
                    line.machine.checkStructure(true, line.tile),
                    "Assembly Line rejected a real Spatial world: " + line.errors());
                line.loadRecipe();
                int items = line.items();
                long used = 0;
                for (int tick = 0; tick < 4000 && line.output() == 0; tick++) used += line.tick(1);
                check(
                    used > 0 && line.items() < items && line.input.getFluidAmount() == 0 && line.output() == 1,
                    "Spatial Assembly Line did not consume inputs/energy and produce its Controller");
            }
        }
        GTNHExtraNerf.LOG.info("Extra Nerf Spatial Assembly Line integration tests PASSED");
    }

    private static WorldServer spatialWorld(WorldServer source) {
        ItemStack cell = GTModHandler
            .getModItem(Mods.AppliedEnergistics2.ID, "item.ItemSpatialStorageCell.16Cubed", 1, 0);
        ISpatialStorageCell spatial = (ISpatialStorageCell) cell.getItem();
        check(
            spatial.doSpatialTransition(
                cell,
                source,
                new WorldCoord(1280, 200, 352),
                new WorldCoord(1297, 217, 369),
                true).success,
            "Native 16-cubed Spatial cell did not create a world");
        WorldServer world = (WorldServer) spatial.getWorld(cell);
        check(
            world != null && world.provider instanceof StorageWorldProvider,
            "AE2 cell did not create its native dimension");
        return world;
    }

    private static final class Line implements AutoCloseable {

        final WorldServer world;
        final BaseMetaTileEntity tile;
        final MTEExtendedPowerMultiBlockBase<?> machine;
        final List<BaseMetaTileEntity> parts = new ArrayList<>();
        final int minX, maxX, minY, maxY, minZ, maxZ;
        BaseMetaTileEntity energy;
        MTEHatchInput input;

        Line(WorldServer world, ItemStack controller, int x, int y, int z) {
            this.world = world;
            boolean spatial = world.provider instanceof StorageWorldProvider;
            minX = spatial ? 1 : x - 18;
            maxX = spatial ? 16 : x + 18;
            minY = spatial ? 65 : y - 3;
            maxY = spatial ? 80 : y + 2;
            minZ = spatial ? 1 : z - 18;
            maxZ = spatial ? 16 : z + 18;
            clear();
            tile = TransformerIntegrationTests.placeAt(world, controller, x, y, z);
            tile.setFrontFacing(ForgeDirection.NORTH);
            parts.add(tile);
            machine = (MTEExtendedPowerMultiBlockBase<?>) tile.getMetaTileEntity();
            machine.setBatchMode(false);
            ((IConstructable) machine).construct(controller, false);
            List<int[]> slices = casings(9, y);
            slices.sort(Comparator.comparingInt(pos -> Math.abs(pos[0] - x) + Math.abs(pos[2] - z)));
            check(
                slices.size() >= 5 && slices.size() <= 16,
                "Native line builder did not create its advertised slices");
            for (int i = 0; i < slices.size(); i++) {
                int[] slice = slices.get(i);
                hatch(
                    (i == slices.size() - 1 ? ItemList.Hatch_Output_Bus_ZPM : ItemList.Hatch_Input_Bus_ZPM).get(1),
                    slice[0],
                    y - 2,
                    slice[2],
                    ForgeDirection.DOWN);
            }
            List<int[]> base = casings(0, y - 2);
            check(base.size() >= 2, "Native line lacks maintenance/fluid hatch positions");
            int[] pos = base.get(0);
            hatch(ItemList.Hatch_Maintenance.get(1), pos[0], pos[1], pos[2], ForgeDirection.DOWN);
            pos = base.get(1);
            input = (MTEHatchInput) hatch(ItemList.Hatch_Input_ZPM.get(1), pos[0], pos[1], pos[2], ForgeDirection.DOWN)
                .getMetaTileEntity();
            pos = slices.get(0);
            energy = hatch(ItemList.Hatch_Energy_ZPM.get(1), pos[0], y + 1, pos[2], ForgeDirection.UP);
            List<StructureError> errors = errors();
            check(
                errors.isEmpty() || errors.size() == 1 && errors.contains(DIMENSION_ERROR),
                "Invalid native Assembly Line fixture: " + errors);
            machine.fixAllIssues();
        }

        List<int[]> casings(int metadata, int y) {
            List<int[]> positions = new ArrayList<>();
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++) if (world.getBlock(x, y, z) == GregTechAPI.sBlockCasings2
                    && world.getBlockMetadata(x, y, z) == metadata) positions.add(new int[] { x, y, z });
            return positions;
        }

        BaseMetaTileEntity hatch(ItemStack stack, int x, int y, int z, ForgeDirection facing) {
            BaseMetaTileEntity part = TransformerIntegrationTests.placeAt(world, stack, x, y, z);
            part.setFrontFacing(facing);
            parts.add(part);
            return part;
        }

        List<StructureError> errors() {
            machine.clearHatches();
            List<StructureError> errors = new ArrayList<>();
            machine.checkMachine(tile, null, errors);
            return errors;
        }

        void loadRecipe() {
            ItemStack cell = GTModHandler
                .getModItem(Mods.AppliedEnergistics2.ID, "item.ItemBasicStorageCell.64k", 1, 0);
            GTScannerResult.ALScannerResult research = (GTScannerResult.ALScannerResult) ScannerHandlerLoader
                .doAssemblyLineResearch(null, cell, ItemList.Tool_DataStick.get(1), null);
            GTRecipe.RecipeAssemblyLine recipe = research.alRecipe;
            check(
                machine.mInputBusses.size() >= recipe.mInputs.length,
                "Line has too few buses for Controller research");
            for (int i = 0; i < recipe.mInputs.length; i++) machine.mInputBusses.get(i)
                .setInventorySlotContents(0, recipe.mInputs[i].copy());
            input.fill(ForgeDirection.UNKNOWN, recipe.mFluidInputs[0].copy(), true);
            machine.setInventorySlotContents(1, research.output.copy());
            tile.enableWorking();
        }

        long tick(int count) {
            long used = 0;
            for (int i = 0; i < count; i++) {
                energy.setStoredEU(energy.getEUCapacity());
                long before = energy.getStoredEU();
                for (BaseMetaTileEntity part : parts) part.updateEntityProfiled();
                used += before - energy.getStoredEU();
            }
            return used;
        }

        int items() {
            return machine.mInputBusses.stream()
                .mapToInt(bus -> {
                    ItemStack stack = bus.getStackInSlot(0);
                    return stack == null ? 0 : stack.stackSize;
                })
                .sum();
        }

        int output() {
            return machine.mOutputBusses.stream()
                .mapToInt(bus -> {
                    int count = 0;
                    for (int slot = 0; slot < bus.getSizeInventory(); slot++) {
                        ItemStack stack = bus.getStackInSlot(slot);
                        if (stack != null) count += stack.stackSize;
                    }
                    return count;
                })
                .sum();
        }

        private void clear() {
            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) world.setBlockToAir(x, y, z);
        }

        @Override
        public void close() {
            clear();
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
