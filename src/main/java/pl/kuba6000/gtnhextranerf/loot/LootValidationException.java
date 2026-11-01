package pl.kuba6000.gtnhextranerf.loot;

/** An expected mismatch with pack loot data, distinct from a programming or rollback failure. */
public final class LootValidationException extends IllegalStateException {

    public LootValidationException(String message) {
        super(message);
    }
}
