package com.tongge.app;

public final class ServerAddressCheck {
    public static void main(String[] args) {
        equal("https://example.com/app/", ServerAddress.normalize(" HTTPS://EXAMPLE.COM:443/app "));
        equal("https://example.com:8443/", ServerAddress.normalize("https://example.com:8443"));
        equal("https://example.com:8443/__tongge_app__/index.html",
            ServerAddress.localHome("https://example.com:8443/deployment/"));
        if (!ServerAddress.sameOrigin("https://example.com/", "https://example.com:443/other")) throw new AssertionError();
        if (ServerAddress.sameOrigin("https://example.com/", "https://example.com:8443/")) throw new AssertionError();
        for (String input : new String[]{"http://example.com/", "https://user:pass@example.com/",
                "https://example.com/?code=1", "https://example.com/#x", "https://example.com:0/",
                "https://example.com:65536/", "https://example.com/__tongge_app__",
                "https://example.com/__tongge_app__/sub/", "https://example.com/%2e%2e/"}) {
            try { ServerAddress.normalize(input); throw new AssertionError("Accepted: " + input); }
            catch (IllegalArgumentException expected) { }
        }
        System.out.println("Server address checks passed");
    }
    private static void equal(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError(actual);
    }
}
