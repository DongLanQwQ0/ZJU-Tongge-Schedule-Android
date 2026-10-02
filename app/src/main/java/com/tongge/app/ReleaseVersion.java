package com.tongge.app;

/** Numeric comparison for stable vMAJOR.MINOR.PATCH release tags. */
final class ReleaseVersion {
    static boolean isNewer(String candidate, String installed) {
        int[] next = parse(candidate), current = parse(installed);
        if (next == null || current == null) return false;
        for (int i = 0; i < next.length; i++) {
            if (next[i] != current[i]) return next[i] > current[i];
        }
        return false;
    }
    static int[] parse(String version) {
        if (version == null || !version.matches("[vV]?[0-9]+\\.[0-9]+\\.[0-9]+")) return null;
        String[] parts = version.replaceFirst("^[vV]", "").split("\\.");
        int[] numbers = new int[3];
        try {
            for (int i = 0; i < numbers.length; i++) numbers[i] = Integer.parseInt(parts[i]);
            return numbers;
        } catch (NumberFormatException e) { return null; }
    }
}
