package com.tongge.app;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** One deployment base URL owns API requests, shared invitations, and login storage. */
final class ServerAddress {
    static final String DEFAULT = "https://111.228.3.50/tongge/";
    static final String LOCAL_PATH = "/__tongge_app__/";
    private ServerAddress() {}

    static String normalize(String input) {
        try {
            URI uri = new URI(input.trim()).normalize();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || uri.getPort() < -1
                    || uri.getPort() == 0 || uri.getPort() > 65535)
                throw new IllegalArgumentException("请输入完整 HTTPS 地址，不含账号、查询参数或片段");
            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) path = "/";
            if (!path.endsWith("/")) path += "/";
            if (path.startsWith(LOCAL_PATH) || path.contains("%") || path.contains("\\"))
                throw new IllegalArgumentException("服务器路径不能包含转义字符或应用保留路径");
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            return "https://" + host + (port == -1 || port == 443 ? "" : ":" + port) + path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("请输入有效的 HTTPS 服务器地址");
        }
    }

    static String localHome(String base) {
        URI uri = URI.create(base);
        return uri.getScheme() + "://" + uri.getRawAuthority() + LOCAL_PATH + "index.html";
    }

    static boolean sameOrigin(String base, String other) {
        try {
            URI a = URI.create(base), b = URI.create(other);
            return a.getScheme().equalsIgnoreCase(b.getScheme())
                && a.getHost().equalsIgnoreCase(b.getHost())
                && port(a) == port(b) && b.getRawUserInfo() == null;
        } catch (IllegalArgumentException | NullPointerException e) { return false; }
    }

    private static int port(URI uri) { return uri.getPort() == -1 ? 443 : uri.getPort(); }
}
