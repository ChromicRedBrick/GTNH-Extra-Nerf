package pl.kuba6000.gtnhextranerf.loot.runtime;

/** ELB keeps its native per-player DB in a static handler; reset the handle when switching saves. */
public interface NativeLootStorage {

    void resetNativeLootStorage();
}
