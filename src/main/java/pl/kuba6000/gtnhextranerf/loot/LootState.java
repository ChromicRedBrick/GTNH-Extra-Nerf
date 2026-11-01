package pl.kuba6000.gtnhextranerf.loot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Immutable planning snapshot; creating or reading it never commits an opening. */
public final class LootState {

    private final Map<String, Long> consumed;

    private LootState(Map<String, Long> consumed) {
        Map<String, Long> copy = new LinkedHashMap<>();
        consumed.forEach((id, count) -> {
            Objects.requireNonNull(id, "Limit ID");
            Objects.requireNonNull(count, "Consumed units");
            if (id.trim()
                .isEmpty() || count < 0) throw new IllegalArgumentException("Invalid allowance state");
            copy.put(id, count);
        });
        this.consumed = Collections.unmodifiableMap(copy);
    }

    public static LootState empty() {
        return new LootState(Collections.emptyMap());
    }

    /** Preserve all historical keys, including rules temporarily absent from the active policy. */
    public static LootState fromConsumed(Map<String, Long> consumed) {
        return new LootState(consumed);
    }

    public long consumedUnits(String limitId) {
        return consumed.getOrDefault(limitId, 0L);
    }

    public Map<String, Long> consumedUnits() {
        return consumed;
    }
}
