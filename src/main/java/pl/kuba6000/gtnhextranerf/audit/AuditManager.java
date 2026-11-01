package pl.kuba6000.gtnhextranerf.audit;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

/** Static bridge required by lifecycle/mixin entry points. All state belongs to one session. */
public final class AuditManager {

    private static final AuditSession SESSION = new AuditSession(Arrays.asList(AuditScope.values()));

    private AuditManager() {}

    public static AuditSession session() {
        return SESSION;
    }

    public static void run(AuditScope scope, Consumer<AuditHandle> action) {
        SESSION.run(scope, action);
    }

    public static <T> T auditResult(AuditScope scope, Function<AuditHandle, T> action) {
        return SESSION.auditResult(scope, action);
    }

    public static AuditReport finish() {
        return SESSION.finish();
    }
}
