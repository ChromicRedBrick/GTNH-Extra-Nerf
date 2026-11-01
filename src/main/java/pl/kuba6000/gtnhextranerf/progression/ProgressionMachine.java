package pl.kuba6000.gtnhextranerf.progression;

/** Machine UI state; the earliest missing certificate is synchronized while the window is open. */
public interface ProgressionMachine {

    ProgressionTier requiredCertificate();

    boolean isCertificationMissing();
}
