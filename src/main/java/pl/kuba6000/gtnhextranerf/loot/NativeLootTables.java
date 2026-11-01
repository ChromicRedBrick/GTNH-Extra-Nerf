package pl.kuba6000.gtnhextranerf.loot;

import java.util.ArrayList;
import java.util.List;

import eu.usrv.enhancedlootbags.core.serializer.LootGroups;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup;
import eu.usrv.enhancedlootbags.core.serializer.LootGroups.LootGroup.Drop;
import eu.usrv.enhancedlootbags.core.serializer.LootGroupsFactory;
import pl.kuba6000.gtnhextranerf.audit.AuditCounts;

/** Edits ELB's actual objects. Targets are validated before commit; no second catalog or RNG. */
public final class NativeLootTables {

    private final LootGroups tables;
    private final List<Runnable> edits = new ArrayList<>();
    private int modified;
    private int added;
    private int removed;

    public NativeLootTables(LootGroups tables) {
        this.tables = tables;
    }

    public LootGroup group(int id) {
        LootGroup found = null;
        for (LootGroup group : tables.getLootTable()) if (group.getGroupID() == id) {
            if (found != null) throw new LootValidationException("Duplicate loot group " + id);
            found = group;
        }
        if (found == null) throw new LootValidationException("Missing loot group " + id);
        return found;
    }

    public Drop require(int group, String id, ItemKey item) {
        Drop found = null;
        for (Drop drop : group(group).getDrops()) if (drop.getIdentifier()
            .equals(id) && item(drop).equals(item)) {
                if (found != null) throw new LootValidationException("Ambiguous loot target " + group + "/" + id);
                found = drop;
            }
        if (found == null) throw new LootValidationException("Missing loot target " + group + "/" + id + ": " + item);
        return found;
    }

    public void replace(int group, Drop original, Drop replacement) {
        List<Drop> drops = group(group).getDrops();
        if (!drops.contains(original)) throw new IllegalStateException("Foreign loot entry");
        validate(replacement);
        edits.add(() -> drops.set(drops.indexOf(original), replacement));
        modified++;
    }

    public void amount(int group, Drop original, int amount) {
        replace(group, original, copy(original, amount, false, original.getChance()));
    }

    public void weight(int group, Drop original, int weight) {
        replace(group, original, copy(original, original.getAmount(), original.getIsRandomAmount(), weight));
    }

    public void add(int group, Drop drop) {
        LootGroup target = group(group);
        validate(drop);
        edits.add(
            () -> target.getDrops()
                .add(drop));
        added++;
    }

    public void remove(int group, Drop drop) {
        LootGroup target = group(group);
        if (!target.getDrops()
            .contains(drop)) throw new IllegalStateException("Foreign loot entry");
        edits.add(
            () -> target.getDrops()
                .remove(drop));
        removed++;
    }

    public AuditCounts commit() {
        for (Runnable edit : edits) edit.run();
        refreshWeights(tables);
        edits.clear();
        return new AuditCounts(modified, added, removed);
    }

    public static void refreshWeights(LootGroups tables) {
        for (LootGroup group : tables.getLootTable()) group.updateMaxWeight();
        verifyWeights(tables);
    }

    public static void verifyWeights(LootGroups tables) {
        for (LootGroup group : tables.getLootTable()) {
            long weight = 0;
            for (Drop drop : group.getDrops()) {
                validate(drop);
                weight += drop.getChance();
            }
            if (weight > Integer.MAX_VALUE)
                throw new LootValidationException("Loot weight overflow in " + group.getGroupID());
            if (group.getMaxWeight() != weight) throw new LootValidationException("Stale native loot weight");
        }
    }

    public static ItemKey item(Drop drop) {
        String value = drop.getItemName();
        int last = value.lastIndexOf(':');
        return value.indexOf(':') == last ? ItemKey.of(value, 0)
            : ItemKey.of(value.substring(0, last), Integer.parseInt(value.substring(last + 1)));
    }

    private static Drop copy(Drop source, int amount, boolean randomAmount, int weight) {
        return new LootGroupsFactory().createDrop(
            source.getItemName(),
            source.getIdentifier(),
            source.getNBTTag(),
            amount,
            randomAmount,
            weight,
            source.getLimitedDropCount(),
            source.getItemDropGroup());
    }

    private static void validate(Drop drop) {
        if (drop == null || drop.getAmount() <= 0 || drop.getChance() < 0)
            throw new LootValidationException("Invalid native loot entry");
    }
}
