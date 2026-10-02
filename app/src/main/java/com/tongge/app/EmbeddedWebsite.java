package com.tongge.app;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import androidx.webkit.WebViewAssetLoader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.json.JSONObject;

/** Reserved local routes never fall through to a server, even when an asset is missing. */
final class EmbeddedWebsite {
    private final String serverBase;
    private final WebViewAssetLoader loader;

    EmbeddedWebsite(Context context, String base) {
        serverBase = base;
        WebViewAssetLoader.AssetsPathHandler assets = new WebViewAssetLoader.AssetsPathHandler(context);
        loader = new WebViewAssetLoader.Builder().setDomain(Uri.parse(base).getHost())
            .addPathHandler(ServerAddress.LOCAL_PATH, path -> {
                if (!"index.html".equals(path)) return assets.handle("web/" + path);
                try (InputStream stream = context.getAssets().open("web/index.html")) {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
                    String html = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
                    String script = "<style>#btn-android-download{display:none!important}</style>"
                        + "<script>window.__tonggeServerBase=" + JSONObject.quote(base) + ";</script>";
                    html = html.replace("<head>", "<head>" + script);
                    return new WebResourceResponse("text/html", "UTF-8",
                        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) { return missing(); }
            }).build();
    }

    WebResourceResponse intercept(WebResourceRequest request) {
        Uri uri = request.getUrl();
        if (!ServerAddress.sameOrigin(serverBase, uri.toString())
                || uri.getPath() == null || !uri.getPath().startsWith(ServerAddress.LOCAL_PATH)) return null;
        if (!"GET".equals(request.getMethod())) return missing();
        WebResourceResponse response = loader.shouldInterceptRequest(uri);
        return response != null ? response : missing();
    }

    private static WebResourceResponse missing() {
        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found",
            Collections.singletonMap("Cache-Control", "no-store"), new ByteArrayInputStream(new byte[0]));
    }
}
