package pl.kuba6000.gtnhextranerf.loot.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import pl.kuba6000.gtnhextranerf.loot.BagVariant;
import pl.kuba6000.gtnhextranerf.loot.LootReward;

/** Durable issuance evidence; a pending/failed claim never authorizes automatic redelivery. */
public final class LootClaim {

    public enum Outcome {
        PENDING,
        DELIVERED,
        FAILED
    }

    private final UUID id;
    private final UUID player;
    private final String policy;
    private final BagVariant bag;
    private final List<LootReward> rewards;
    private final Outcome outcome;

    LootClaim(UUID id, UUID player, String policy, BagVariant bag, List<LootReward> rewards, Outcome outcome) {
        this.id = id;
        this.player = player;
        this.policy = policy;
        this.bag = bag;
        this.rewards = Collections.unmodifiableList(new ArrayList<>(rewards));
        this.outcome = outcome;
    }

    public UUID id() {
        return id;
    }

    public UUID player() {
        return player;
    }

    public String policy() {
        return policy;
    }

    public BagVariant bag() {
        return bag;
    }

    public List<LootReward> rewards() {
        return rewards;
    }

    public Outcome outcome() {
        return outcome;
    }

    LootClaim withOutcome(Outcome outcome) {
        return new LootClaim(id, player, policy, bag, rewards, outcome);
    }
}
