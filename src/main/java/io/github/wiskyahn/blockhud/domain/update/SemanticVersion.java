package io.github.wiskyahn.blockhud.domain.update;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 시맨틱 버전 (major.minor.patch). 원본 Convert-ToVersion 대체. (DESIGN.md §8.6)
 * {@code v1.2.1}, {@code 1.2}, {@code 1.2.1-beta} 등 관용 표기를 허용한다.
 */
public record SemanticVersion(int major, int minor, int patch)
        implements Comparable<SemanticVersion> {

    private static final Pattern PATTERN =
            Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");

    public static SemanticVersion parse(String text) {
        if (text == null) {
            return new SemanticVersion(0, 0, 0);
        }
        Matcher m = PATTERN.matcher(text);
        if (!m.find()) {
            return new SemanticVersion(0, 0, 0);
        }
        return new SemanticVersion(
                Integer.parseInt(m.group(1)),
                m.group(2) != null ? Integer.parseInt(m.group(2)) : 0,
                m.group(3) != null ? Integer.parseInt(m.group(3)) : 0);
    }

    @Override
    public int compareTo(SemanticVersion o) {
        if (major != o.major) {
            return Integer.compare(major, o.major);
        }
        if (minor != o.minor) {
            return Integer.compare(minor, o.minor);
        }
        return Integer.compare(patch, o.patch);
    }

    public boolean isNewerThan(SemanticVersion o) {
        return compareTo(o) > 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
