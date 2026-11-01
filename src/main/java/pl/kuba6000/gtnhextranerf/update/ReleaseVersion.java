package pl.kuba6000.gtnhextranerf.update;

import java.util.regex.Pattern;

/** Release tags are dotted numbers. Development builds ("0.0.6-86-g02984ed") and prereleases never compare. */
public final class ReleaseVersion {

    private static final Pattern RELEASE = Pattern.compile("(0|[1-9][0-9]{0,5})(\\.(0|[1-9][0-9]{0,5})){0,3}");

    private ReleaseVersion() {}

    public static boolean isRelease(String version) {
        return version != null && RELEASE.matcher(version)
            .matches();
    }

    /** True only when both values are clean release tags and the candidate is strictly higher. */
    public static boolean isNewer(String current, String candidate) {
        if (!isRelease(current) || !isRelease(candidate)) return false;
        String[] a = current.split("\\.");
        String[] b = candidate.split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int left = i < a.length ? Integer.parseInt(a[i]) : 0;
            int right = i < b.length ? Integer.parseInt(b[i]) : 0;
            if (left != right) return right > left;
        }
        return false;
    }
}
