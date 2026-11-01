package pl.kuba6000.gtnhextranerf.audit;

/** A count contract and its description come from the same immutable value. */
public interface AuditExpectation {

    boolean accepts(AuditCounts counts);

    String describe();

    static ExactBuilder exact() {
        return new ExactBuilder();
    }

    static AuditExpectation atLeastModified(int minimum) {
        if (minimum < 0) throw new IllegalArgumentException("Minimum cannot be negative");
        return new AuditExpectation() {

            @Override
            public boolean accepts(AuditCounts counts) {
                return counts.modified() >= minimum;
            }

            @Override
            public String describe() {
                return "at least " + minimum + " modified; added/removed unrestricted";
            }
        };
    }

    static AuditExpectation addedPerModification() {
        return new AuditExpectation() {

            @Override
            public boolean accepts(AuditCounts counts) {
                return counts.modified() > 0 && counts.added() == counts.modified() && counts.removed() == 0;
            }

            @Override
            public String describe() {
                return "at least one modified, one added per modification, zero removed";
            }
        };
    }

    static AuditExpectation atLeastRemoved(int minimum) {
        if (minimum < 0) throw new IllegalArgumentException("Minimum cannot be negative");
        return new AuditExpectation() {

            @Override
            public boolean accepts(AuditCounts counts) {
                return counts.removed() >= minimum && counts.added() == 0 && counts.modified() == 0;
            }

            @Override
            public String describe() {
                return "at least " + minimum + " removed, zero added/modified";
            }
        };
    }

    final class ExactBuilder {

        private int modified;
        private int added;
        private int removed;

        private ExactBuilder() {}

        public ExactBuilder modified(int value) {
            modified = value;
            return this;
        }

        public ExactBuilder added(int value) {
            added = value;
            return this;
        }

        public ExactBuilder removed(int value) {
            removed = value;
            return this;
        }

        public AuditExpectation build() {
            AuditCounts expected = new AuditCounts(modified, added, removed);
            return new AuditExpectation() {

                @Override
                public boolean accepts(AuditCounts actual) {
                    return actual.modified() == expected.modified() && actual.added() == expected.added()
                        && actual.removed() == expected.removed();
                }

                @Override
                public String describe() {
                    return "exactly " + expected;
                }
            };
        }
    }
}
