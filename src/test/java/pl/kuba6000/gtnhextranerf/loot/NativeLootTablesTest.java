package pl.kuba6000.gtnhextranerf.loot;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.EnumRarity;

import org.junit.jupiter.api.Test;

import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup.Drop;
import eu.usrv.enhancedlootbags.core.serializer.LootGroupsFactory;
import pl.kuba6000.gtnhextranerf.loot.modules.MeControllerLootModule;

class NativeLootTablesTest {

    @Test
    void changesTheActualElbTableAndVerifiesBothControllerSources() {
        LootGroups groups = controllerTables();
        MeControllerLootModule module = new MeControllerLootModule();
        Drop original = groups.getLootTable()
            .get(1)
            .getDrops()
            .get(0);
        assertEquals(
            1,
            module.apply(groups)
                .modified());
        module.verify(groups);
        Drop changed = groups.getLootTable()
            .get(1)
            .getDrops()
            .get(0);
        assertEquals(1, changed.getAmount());
        assertEquals(original.getChance(), changed.getChance());
        assertEquals(original.getIdentifier(), changed.getIdentifier());
        assertEquals(original.getNBTTag(), changed.getNBTTag());
        assertEquals(original.getItemDropGroup(), changed.getItemDropGroup());
        assertEquals(original.getLimitedDropCount(), changed.getLimitedDropCount());
        assertEquals(
            1,
            groups.getLootTable()
                .get(0)
                .getDrops()
                .get(0)
                .getAmount());
        assertEquals(
            changed.getChance(),
            groups.getLootTable()
                .get(1)
                .getMaxWeight());
    }

    @Test
    void missingOrChangedSourceCannotPartiallyPublishAPatch() {
        LootGroups groups = controllerTables();
        groups.getLootTable()
            .get(0)
            .getDrops()
            .clear();
        assertThrows(IllegalStateException.class, () -> new MeControllerLootModule().apply(groups));
        assertEquals(
            4,
            groups.getLootTable()
                .get(1)
                .getDrops()
                .get(0)
                .getAmount());
    }

    @Test
    void weightAndMembershipEditsRecalculateTheNativeCachedTotals() {
        LootGroups groups = controllerTables();
        LootGroup basic = groups.getLootTable()
            .get(0);
        LootGroup good = groups.getLootTable()
            .get(1);
        basic.getMaxWeight();
        good.getMaxWeight();
        NativeLootTables editor = new NativeLootTables(groups);
        editor.weight(
            37,
            basic.getDrops()
                .get(0),
            13);
        editor.remove(
            38,
            good.getDrops()
                .get(0));
        editor.add(38, new LootGroupsFactory().createDrop("test:replacement", "replacement", 2, false, 5, 0));
        pl.kuba6000.gtnhextranerf.audit.AuditCounts counts = editor.commit();
        assertEquals(1, counts.modified());
        assertEquals(1, counts.removed());
        assertEquals(1, counts.added());
        assertEquals(13, basic.getMaxWeight());
        assertEquals(5, good.getMaxWeight());
        assertEquals(
            "test:replacement",
            good.getDrops()
                .get(0)
                .getItemName());
    }

    @Test
    void postPublicationVerificationRejectsLaterTampering() {
        LootGroups groups = controllerTables();
        MeControllerLootModule module = new MeControllerLootModule();
        module.apply(groups);
        groups.getLootTable()
            .get(0)
            .getDrops()
            .clear();
        assertThrows(IllegalStateException.class, () -> module.verify(groups));
        assertThrows(IllegalStateException.class, () -> NativeLootTables.verifyWeights(groups));
    }

    static LootGroups controllerTables() {
        LootGroups groups = new LootGroups();
        LootGroupsFactory factory = new LootGroupsFactory();
        for (int id : new int[] { 37, 38 }) {
            LootGroup group = factory.createLootGroup(id, "group-" + id, EnumRarity.common, 1, 1, false);
            group.getDrops()
                .add(
                    factory.createDrop(
                        "appliedenergistics2:tile.BlockController",
                        id == 37 ? "c86156c4-b72c-4ada-9ad2-3bbb229c5664" : "13a284df-df59-4389-8543-9c3b65fc28dc",
                        "",
                        id == 37 ? 1 : 4,
                        false,
                        id == 37 ? 3 : 7,
                        0,
                        ""));
            group.updateMaxWeight();
            groups.getLootTable()
                .add(group);
        }
        return groups;
    }
}
