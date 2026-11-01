package pl.kuba6000.gtnhextranerf.recipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import pl.kuba6000.gtnhextranerf.audit.AuditCounts;
import pl.kuba6000.gtnhextranerf.audit.AuditScope;
import pl.kuba6000.gtnhextranerf.audit.AuditSession;

/** Explicit, ordered registration. Activation and development failure handling have one owner. */
public final class RecipeRegistry {

    private final Map<AuditScope, RecipeModifier> modifiers = new LinkedHashMap<>();
    private boolean started;
    private final AuditSession audit;
    private final RecipeFailurePolicy failurePolicy;

    public RecipeRegistry(AuditSession audit, RecipeFailurePolicy failurePolicy) {
        this.audit = Objects.requireNonNull(audit, "audit");
        this.failurePolicy = Objects.requireNonNull(failurePolicy, "failurePolicy");
    }

    public RecipeRegistry register(RecipeModule module) {
        Objects.requireNonNull(module, "module")
            .register(this);
        return this;
    }

    public RecipeRegistry register(AuditScope scope, RecipeModifier modifier) {
        if (started) throw new IllegalStateException("Recipe registry has already started");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(modifier, "modifier");
        if (modifiers.containsKey(scope)) throw new IllegalArgumentException("Duplicate recipe modifier: " + scope);
        modifiers.put(scope, modifier);
        return this;
    }

    public void run() {
        if (started) throw new IllegalStateException("Recipe registry can only run once");
        started = true;
        modifiers.forEach((scope, modifier) -> {
            if (!scope.isActive()) return;
            try {
                audit.run(scope, handle -> {
                    RecipeTransaction.Result result = RecipeTransaction
                        .execute(transaction -> modifier.modify(new RecipeContext(transaction)));
                    handle.record(new AuditCounts(result.modified(), result.added(), result.removed()));
                });
            } catch (RecipeValidationException failure) {
                if (failurePolicy == RecipeFailurePolicy.FAIL_FAST) throw new RecipeValidationException(
                    "Recipe modifier " + scope + ": " + failure.getMessage(),
                    failure);
                // The session retains FAILED and its reason. Continuing never converts a failure into success.
            }
        });
    }
}
