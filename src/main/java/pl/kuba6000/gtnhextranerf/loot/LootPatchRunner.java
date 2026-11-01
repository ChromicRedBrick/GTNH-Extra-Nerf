package pl.kuba6000.gtnhextranerf.loot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup.Drop;
import pl.kuba6000.gtnhextranerf.audit.AuditCounts;
import pl.kuba6000.gtnhextranerf.audit.AuditSession;

/** Rolls back native editor changes before allowing development to continue after a validation mismatch. */
public final class LootPatchRunner {

    private LootPatchRunner() {}

    public static boolean apply(LootModule module, LootGroups tables, AuditSession audit, boolean development) {
        List<LootGroup> groups = new ArrayList<>(tables.getLootTable());
        Map<LootGroup, List<Drop>> originalDrops = new LinkedHashMap<>();
        for (LootGroup group : groups) originalDrops.put(group, new ArrayList<>(group.getDrops()));
        try {
            audit.run(module.scope(), handle -> {
                AuditCounts changes = module.apply(tables);
                module.verify(tables);
                handle.record(changes);
            });
            return true;
        } catch (RuntimeException | Error failure) {
            try {
                tables.getLootTable()
                    .clear();
                tables.getLootTable()
                    .addAll(groups);
                originalDrops.forEach((group, drops) -> {
                    group.getDrops()
                        .clear();
                    group.getDrops()
                        .addAll(drops);
                    group.updateMaxWeight();
                });
            } catch (RuntimeException | Error rollbackFailure) {
                rollbackFailure.addSuppressed(failure);
                throw new IllegalStateException("Could not roll back native loot patch", rollbackFailure);
            }
            if (development && failure instanceof LootValidationException) return false;
            throw failure;
        }
    }
}
