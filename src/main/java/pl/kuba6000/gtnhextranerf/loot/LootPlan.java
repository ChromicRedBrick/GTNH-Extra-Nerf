package pl.kuba6000.gtnhextranerf.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Proposed durable allowance claim, bound to one immutable ledger snapshot; never performs selection. */
public final class LootPlan {

    private final List<LootReward> rewards;
    private final LootState stateAfterCommit;
    private final LootState beforeState;
    private final BagVariant bag;

    LootPlan(BagVariant bag, LootState beforeState, List<LootReward> rewards, LootState stateAfterCommit) {
        this.bag = bag;
        this.beforeState = beforeState;
        this.rewards = Collections.unmodifiableList(new ArrayList<>(rewards));
        this.stateAfterCommit = stateAfterCommit;
    }

    public List<LootReward> rewards() {
        return rewards;
    }

    public BagVariant bag() {
        return bag;
    }

    /** Identity token owned by the ledger; equivalent reconstructed maps cannot authorize a stale plan. */
    public LootState beforeState() {
        return beforeState;
    }

    /** Publish this snapshot only after a future opening service durably commits the claim. */
    public LootState stateAfterCommit() {
        return stateAfterCommit;
    }
}
