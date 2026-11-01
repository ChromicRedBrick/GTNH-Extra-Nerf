package pl.kuba6000.gtnhextranerf.loot;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import pl.kuba6000.gtnhextranerf.loot.storage.FileLootLedger;
import pl.kuba6000.gtnhextranerf.loot.storage.LootClaim;

/** Only global allowances. ELB decides what to roll, how much, and how to deliver it. */
public final class GlobalLootLimits {

    private final Thread owner = Thread.currentThread();
    private final FileLootLedger ledger;
    private final Map<ItemKey, GlobalItemLimit> limits = new LinkedHashMap<>();

    public GlobalLootLimits(FileLootLedger ledger, GlobalItemLimit... definitions) {
        this.ledger = ledger;
        Set<String> ids = new HashSet<>();
        for (GlobalItemLimit limit : definitions) {
            if (!ids.add(limit.id()) || limits.putIfAbsent(limit.item(), limit) != null)
                throw new IllegalArgumentException("Duplicate global loot allowance");
        }
    }

    public boolean limited(ItemKey item) {
        return limits.containsKey(item);
    }

    public boolean available(ItemKey item, int amount) {
        requireThread();
        if (amount <= 0) throw new IllegalArgumentException("Positive reward amount required");
        GlobalItemLimit limit = limits.get(item);
        return limit == null || amount <= limit.maxUnits() - ledger.snapshot()
            .consumedUnits(limit.id());
    }

    public Optional<UUID> claim(UUID player, BagVariant bag, LootReward reward) throws IOException {
        requireThread();
        GlobalItemLimit limit = limits.get(reward.item());
        if (limit == null) throw new IllegalArgumentException("Only limited items require a claim");
        if (!available(reward.item(), reward.amount())) return Optional.empty();
        LootState before = ledger.snapshot();
        Map<String, Long> next = new LinkedHashMap<>(before.consumedUnits());
        next.put(limit.id(), Math.addExact(before.consumedUnits(limit.id()), reward.amount()));
        UUID id = UUID.randomUUID();
        ledger.claim(
            id,
            player,
            "native-elb/" + limit.id() + "/" + limit.maxUnits(),
            new LootPlan(bag, before, Collections.singletonList(reward), LootState.fromConsumed(next)));
        return Optional.of(id);
    }

    public void outcome(UUID id, boolean delivered) throws IOException {
        requireThread();
        ledger.recordOutcome(id, delivered ? LootClaim.Outcome.DELIVERED : LootClaim.Outcome.FAILED);
    }

    private void requireThread() {
        if (Thread.currentThread() != owner)
            throw new IllegalStateException("Global loot claims require the server thread");
    }
}
