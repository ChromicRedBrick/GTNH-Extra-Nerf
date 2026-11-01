package pl.kuba6000.gtnhextranerf.loot;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import pl.kuba6000.gtnhextranerf.audit.AuditCounts;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.audit.AuditSession;
import pl.kuba6000.gtnhextranerf.loot.modules.MeControllerLootModule;

class LootPatchRunnerTest {

    @Test
    void missingPackTablesAreNonfatalOnlyInDevelopment() {
        AuditSession development = audit();
        assertFalse(LootPatchRunner.apply(new MeControllerLootModule(), new LootGroups(), development, true));
        assertThrows(
            LootValidationException.class,
            () -> LootPatchRunner.apply(new MeControllerLootModule(), new LootGroups(), audit(), false));
    }

    @Test
    void failedVerificationRestoresNativeDropsAndCachedWeights() {
        LootGroups tables = NativeLootTablesTest.controllerTables();
        LootModule rejected = new LootModule() {

            public AuditScope scope() {
                return AuditScope.MODIFY_ME_CONTROLLER_LOOT;
            }

            public AuditCounts apply(LootGroups groups) {
                NativeLootTables editor = new NativeLootTables(groups);
                editor.amount(
                    38,
                    editor.group(38)
                        .getDrops()
                        .get(0),
                    1);
                editor.weight(
                    37,
                    editor.group(37)
                        .getDrops()
                        .get(0),
                    99);
                return editor.commit();
            }

            public void verify(LootGroups groups) {
                throw new LootValidationException("Rejected fixture");
            }
        };
        assertFalse(LootPatchRunner.apply(rejected, tables, audit(), true));
        assertEquals(
            4,
            tables.getLootTable()
                .get(1)
                .getDrops()
                .get(0)
                .getAmount());
        assertEquals(
            3,
            tables.getLootTable()
                .get(0)
                .getMaxWeight());
        assertEquals(
            7,
            tables.getLootTable()
                .get(1)
                .getMaxWeight());
    }

    @Test
    void programmingErrorsAreStillFatalInDevelopment() {
        LootModule broken = new LootModule() {

            public AuditScope scope() {
                return AuditScope.MODIFY_ME_CONTROLLER_LOOT;
            }

            public AuditCounts apply(LootGroups groups) {
                throw new NullPointerException("Bug");
            }

            public void verify(LootGroups groups) {}
        };
        assertThrows(NullPointerException.class, () -> LootPatchRunner.apply(broken, new LootGroups(), audit(), true));
    }

    private static AuditSession audit() {
        return new AuditSession(Collections.singletonList(AuditScope.MODIFY_ME_CONTROLLER_LOOT));
    }
}
