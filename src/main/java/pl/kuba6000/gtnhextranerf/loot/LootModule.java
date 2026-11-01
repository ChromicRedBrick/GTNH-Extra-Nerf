package pl.kuba6000.gtnhextranerf.loot;

import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import pl.kuba6000.gtnhextranerf.audit.AuditCounts;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;

/** A deterministic patch of native ELB tables plus a post-publication invariant. */
public interface LootModule {

    AuditScope scope();

    AuditCounts apply(LootGroups tables);

    void verify(LootGroups tables);
}
