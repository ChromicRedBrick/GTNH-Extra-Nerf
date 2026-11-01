package pl.kuba6000.gtnhextranerf.audit;

/** Definition only: activation and expectations are resolved when a session finishes. */
public interface AuditDefinition {

    String id();

    AuditExpectation expectation();

    default boolean isActive() {
        return true;
    }
}
