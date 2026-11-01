package pl.kuba6000.gtnhextranerf.update;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReleaseVersionTest {

    @Test
    void onlyCleanDottedNumbersAreReleases() {
        for (String release : new String[] { "0.0.6", "1", "1.2", "10.20.30.40", "0.0.10" })
            assertTrue(ReleaseVersion.isRelease(release), release);
        for (String other : new String[] { null, "", "0.0.6-86-g02984ed", "0.0.1-test", "v1.2.3", "01.2", "1..2",
            "1.2.3.4.5", "1.2.x", " 1.2", "GRADLETOKEN_VERSION", "1234567.0" })
            assertFalse(ReleaseVersion.isRelease(other), String.valueOf(other));
    }

    @Test
    void comparesNumericallyPerComponent() {
        assertTrue(ReleaseVersion.isNewer("0.0.6", "0.0.7"));
        assertTrue(ReleaseVersion.isNewer("0.0.9", "0.0.10"));
        assertTrue(ReleaseVersion.isNewer("0.9.9", "1.0"));
        assertTrue(ReleaseVersion.isNewer("1.0", "1.0.1"));
        assertFalse(ReleaseVersion.isNewer("0.0.7", "0.0.7"));
        assertFalse(ReleaseVersion.isNewer("0.0.10", "0.0.9"));
        assertFalse(ReleaseVersion.isNewer("1.0", "1.0.0"));
        assertFalse(ReleaseVersion.isNewer("1.0.0", "1.0"));
    }

    @Test
    void developmentBuildsAndUnusableValuesNeverCompare() {
        assertFalse(ReleaseVersion.isNewer("0.0.6-86-g02984ed", "0.0.7"));
        assertFalse(ReleaseVersion.isNewer("0.0.1-test", "0.0.7"));
        assertFalse(ReleaseVersion.isNewer("0.0.6", "0.0.7-rc1"));
        assertFalse(ReleaseVersion.isNewer("0.0.6", null));
        assertFalse(ReleaseVersion.isNewer(null, "0.0.7"));
    }
}
