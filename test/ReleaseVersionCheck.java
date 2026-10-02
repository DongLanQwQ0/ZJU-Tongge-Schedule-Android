package com.tongge.app;

/** Run with javac/java alongside ReleaseVersion.java; no Android or test framework needed. */
public final class ReleaseVersionCheck {
    public static void main(String[] args) {
        newer("v1.10.0", "1.9.99", true);
        newer("V2.0.0", "v1.999.999", true);
        newer("1.2.4", "1.2.3", true);
        newer("v1.2.3", "1.2.3", false);
        newer("1.2.2", "1.2.3", false);
        newer("0.99.99", "1.0.0", false);
        newer("2147483647.0.0", "2147483646.99.99", true);
        String[] invalid = {null, "", "1", "1.2", "1.2.3.4", "v1.2.3-beta", "1.2.3+build",
            " 1.2.3", "1.2.3 ", "-1.2.3", "1.-2.3", "1.2.x", "vv1.2.3",
            "2147483648.0.0", "1.2147483648.0", "1.0.2147483648",
            "9999999999999999999999999999.0.0"};
        for (String value : invalid) {
            if (ReleaseVersion.parse(value) != null) throw new AssertionError("Accepted invalid version: " + value);
            newer(value, "1.0.0", false);
            newer("2.0.0", value, false);
        }
        System.out.println("PASS: numeric release comparison, invalid input and integer overflow");
    }

    private static void newer(String candidate, String installed, boolean expected) {
        if (ReleaseVersion.isNewer(candidate, installed) != expected)
            throw new AssertionError(candidate + " vs " + installed + ": expected " + expected);
    }
}
