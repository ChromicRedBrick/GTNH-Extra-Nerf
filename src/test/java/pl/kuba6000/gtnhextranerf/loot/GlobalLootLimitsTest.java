package pl.kuba6000.gtnhextranerf.loot;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pl.kuba6000.gtnhextranerf.loot.storage.FileLootLedger;
import pl.kuba6000.gtnhextranerf.loot.storage.LootClaim;

class GlobalLootLimitsTest {

    private static final ItemKey ITEM = ItemKey.of("appliedenergistics2:tile.BlockController", 0);
    private static final GlobalItemLimit LIMIT = new GlobalItemLimit("gtnhextranerf:me_controller", ITEM, 1);
    @TempDir
    Path temporary;

    @Test
    void oneClaimAcrossPlayersBagsAndFortuneSurvivesRestartAndFailedDelivery() throws Exception {
        for (boolean delivered : new boolean[] { false, true }) {
            Path path = temporary.resolve("world-" + delivered);
            try (FileLootLedger ledger = FileLootLedger.create(path)) {
                GlobalLootLimits limits = new GlobalLootLimits(ledger, LIMIT);
                UUID claim = limits.claim(new UUID(0, 1), BagVariant.of(38, 3), new LootReward(ITEM, 1))
                    .get();
                assertFalse(
                    limits.claim(new UUID(0, 2), BagVariant.of(37, 0), new LootReward(ITEM, 1))
                        .isPresent());
                limits.outcome(claim, delivered);
            }
            try (FileLootLedger ledger = FileLootLedger.open(path)) {
                GlobalLootLimits limits = new GlobalLootLimits(ledger, LIMIT);
                assertFalse(limits.available(ITEM, 1));
                assertFalse(
                    limits.claim(new UUID(0, 3), BagVariant.of(37, 2), new LootReward(ITEM, 1))
                        .isPresent());
                if (!delivered) assertEquals(
                    LootClaim.Outcome.FAILED,
                    ledger.unresolved()
                        .get(0)
                        .outcome());
            }
        }
    }

    @Test
    void previewsAndUnrelatedItemsDoNotConsumeAllowanceAndWorldsAreIndependent() throws Exception {
        for (String world : new String[] { "first", "second" })
            try (FileLootLedger ledger = FileLootLedger.create(temporary.resolve(world))) {
                GlobalLootLimits limits = new GlobalLootLimits(ledger, LIMIT);
                assertTrue(limits.available(ITEM, 1));
                assertTrue(limits.available(ITEM, 1));
                assertTrue(limits.available(ItemKey.of("test:unlimited", 0), 64));
                assertFalse(limits.available(ITEM, 4));
                assertTrue(
                    limits.claim(UUID.randomUUID(), BagVariant.of(38, 0), new LootReward(ITEM, 1))
                        .isPresent());
            }
    }

    @Test
    void closedLedgerCannotAuthorizeARewardAndOffThreadCallsAreRejected() throws Exception {
        FileLootLedger ledger = FileLootLedger.create(temporary.resolve("closed"));
        GlobalLootLimits limits = new GlobalLootLimits(ledger, LIMIT);
        Throwable failure = CompletableFuture.supplyAsync(() -> {
            try {
                limits.claim(UUID.randomUUID(), BagVariant.of(37, 0), new LootReward(ITEM, 1));
                return null;
            } catch (Exception e) {
                return e;
            }
        })
            .get();
        assertInstanceOf(IllegalStateException.class, failure);
        ledger.close();
        assertThrows(
            IllegalStateException.class,
            () -> limits.claim(UUID.randomUUID(), BagVariant.of(37, 0), new LootReward(ITEM, 1)));
    }

    @Test
    void malformedPayloadCannotSpendAnAllowanceAndPendingClaimNeverReplays() throws Exception {
        Path path = temporary.resolve("payload");
        try (FileLootLedger ledger = FileLootLedger.create(path)) {
            GlobalLootLimits limits = new GlobalLootLimits(ledger, LIMIT);
            assertThrows(
                IOException.class,
                () -> limits.claim(UUID.randomUUID(), BagVariant.of(37, 0), new LootReward(ITEM, 1, "\uD800", 64)));
            assertTrue(limits.available(ITEM, 1));
            limits.claim(UUID.randomUUID(), BagVariant.of(37, 0), new LootReward(ITEM, 1, "{tag:1b}", 64));
        }
        try (FileLootLedger ledger = FileLootLedger.open(path)) {
            assertFalse(new GlobalLootLimits(ledger, LIMIT).available(ITEM, 1));
            assertEquals(
                LootClaim.Outcome.PENDING,
                ledger.unresolved()
                    .get(0)
                    .outcome());
            assertEquals(
                "{tag:1b}",
                ledger.unresolved()
                    .get(0)
                    .rewards()
                    .get(0)
                    .nbt());
        }
    }

    @Test
    void readsThePreviousEngineJournalWithoutResettingControllerOrPlayerHistory() throws Exception {
        Path path = temporary.resolve("legacy");
        Files.createDirectory(path);
        try (java.io.InputStream input = getClass().getResourceAsStream("/loot/legacy-controller-claims.journal")) {
            Files.copy(input, path.resolve("claims.journal"));
        }
        try (FileLootLedger ledger = FileLootLedger.open(path)) {
            assertFalse(new GlobalLootLimits(ledger, LIMIT).available(ITEM, 1));
            assertEquals(
                6,
                ledger.snapshot()
                    .consumedUnits("elb-player/00000000-0000-0000-0000-000000000001/37/old-native"));
            assertEquals(
                1,
                ledger.unresolved()
                    .size());
        }
    }
}
