package com.tongge.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Background release checks; persist offers until they can be shown in the foreground. */
final class ReleaseUpdates {
    static final String REPOSITORY = "DongLanQwQ0/ZJU-Tongge-Schedule-Android";
    private static final String API = "https://api.github.com/repos/" + REPOSITORY + "/releases/latest";
    private static final String DOWNLOAD_PREFIX = "https://github.com/" + REPOSITORY + "/releases/download/";
    private static final long CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L;
    private static final long RETRY_INTERVAL_MS = 15 * 60 * 1000L;
    private static final int MAX_RESPONSE_BYTES = 1024 * 1024;
    private final Activity activity;
    private final SharedPreferences prefs;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean checking, manualRequested, resumed, closed;
    private String offeredVersion, pendingFeedback;
    private AlertDialog dialog;

    ReleaseUpdates(Activity activity) {
        this.activity = activity;
        prefs = activity.getSharedPreferences("release_updates", Activity.MODE_PRIVATE);
        if (!ReleaseVersion.isNewer(prefs.getString("pending_version", ""), BuildConfig.VERSION_NAME)) clearPending();
    }
    void onResume() { resumed = true; deliverPending(); check(false); }
    void onPause() { resumed = false; }

    void check(boolean manual) {
        if (closed) return;
        if (manual) { manualRequested = true; toast("正在检查更新…"); }
        if (checking) return;
        long now = System.currentTimeMillis();
        if (!manual && (recent(now, prefs.getLong("last_success", 0), CHECK_INTERVAL_MS)
                || recent(now, prefs.getLong("last_attempt", 0), RETRY_INTERVAL_MS))) {
            deliverPending(); return;
        }
        checking = true;
        prefs.edit().putLong("last_attempt", now).apply();
        worker.execute(() -> {
            JSONObject release = null;
            String error = null;
            try { release = fetchRelease(); }
            catch (Exception e) { error = e.getMessage() == null ? "网络连接失败" : e.getMessage(); }
            JSONObject result = release;
            String failure = error;
            main.post(() -> complete(result, failure));
        });
    }
    private static boolean recent(long now, long then, long interval) {
        return then > 0 && now >= then && now - then < interval;
    }
    private JSONObject fetchRelease() throws Exception {
        HttpURLConnection connection = (HttpURLConnection)new URL(API).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "Tongge-Android/" + BuildConfig.VERSION_NAME);
        try {
            int status = connection.getResponseCode();
            if (status == 404) throw new IllegalStateException("仓库暂未发布正式版本");
            if (status == 403 || status == 429) throw new IllegalStateException("检查过于频繁，请稍后重试");
            if (status != 200) throw new IllegalStateException("检查失败（HTTP " + status + "）");
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (output.size() + count > MAX_RESPONSE_BYTES) throw new IllegalStateException("更新信息过大");
                    output.write(buffer, 0, count);
                }
                return new JSONObject(new String(output.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally { connection.disconnect(); }
    }
    private void complete(JSONObject release, String failure) {
        checking = false;
        boolean manual = manualRequested;
        manualRequested = false;
        if (failure != null) {
            if (manual && !closed) feedback(failure.startsWith("检查") || failure.startsWith("仓库")
                ? failure : "无法连接 GitHub，请稍后重试");
            return;
        }
        try {
            if (release.optBoolean("draft") || release.optBoolean("prerelease"))
                throw new IllegalStateException("暂无正式版本");
            String version = release.getString("tag_name");
            if (ReleaseVersion.parse(version) == null) throw new IllegalStateException("版本信息格式不正确");
            if (!ReleaseVersion.isNewer(version, BuildConfig.VERSION_NAME)) {
                clearPending();
                prefs.edit().putLong("last_success", System.currentTimeMillis()).apply();
                if (manual && !closed) feedback("当前已是最新版本 " + BuildConfig.VERSION_NAME);
                return;
            }
            String download = findApk(release.getJSONArray("assets"), version);
            if (download == null) throw new IllegalStateException("新版暂未提供 APK，请稍后重试");
            prefs.edit().putString("pending_version", version)
                .putString("pending_notes", release.optString("body", ""))
                .putString("pending_url", download).putLong("last_success", System.currentTimeMillis()).apply();
            if (manual) offeredVersion = null;
            deliverPending();
        } catch (Exception e) { if (manual && !closed) feedback(e.getMessage()); }
    }
    private static String findApk(JSONArray assets, String version) {
        String requiredName = "Tongge-" + version.replaceFirst("^[vV]", "") + ".apk";
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null || !requiredName.equals(asset.optString("name"))
                    || !"uploaded".equals(asset.optString("state"))) continue;
            String url = asset.optString("browser_download_url");
            if (url.startsWith(DOWNLOAD_PREFIX + version + "/") && asset.optLong("size") > 0) return url;
        }
        return null;
    }
    void deliverPending() {
        if (closed || !resumed || !activity.hasWindowFocus() || activity.isFinishing() || activity.isDestroyed()) return;
        if (pendingFeedback != null) { toast(pendingFeedback); pendingFeedback = null; }
        if (dialog != null && dialog.isShowing()) return;
        String version = prefs.getString("pending_version", "");
        if (!ReleaseVersion.isNewer(version, BuildConfig.VERSION_NAME) || version.equals(offeredVersion)) return;
        String url = prefs.getString("pending_url", "");
        if (!url.startsWith(DOWNLOAD_PREFIX + version + "/")) { clearPending(); return; }
        String notes = prefs.getString("pending_notes", "").trim();
        if (notes.length() > 3000) notes = notes.substring(0, 3000) + "…";
        String message = "当前 " + BuildConfig.VERSION_NAME + " → " + version;
        if (!notes.isEmpty()) message += "\n\n" + notes;
        offeredVersion = version;
        dialog = new AlertDialog.Builder(activity).setTitle("发现新版 " + version)
            .setMessage(message).setNegativeButton("稍后", null)
            .setPositiveButton("下载 APK", (ignored, which) -> {
                try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception e) { toast("无法打开下载链接"); }
            }).create();
        dialog.setOnDismissListener(ignored -> dialog = null);
        dialog.show();
    }
    private void feedback(String message) { pendingFeedback = message; deliverPending(); }
    private void toast(String message) { Toast.makeText(activity, message, Toast.LENGTH_SHORT).show(); }
    private void clearPending() {
        prefs.edit().remove("pending_version").remove("pending_notes").remove("pending_url").apply();
    }
    void close() {
        closed = true;
        // A request already running may still persist its result after Activity teardown.
        // deliverPending guards all UI access using closed/resumed/focus state.
        worker.shutdownNow();
        if (dialog != null) dialog.dismiss();
    }
}
