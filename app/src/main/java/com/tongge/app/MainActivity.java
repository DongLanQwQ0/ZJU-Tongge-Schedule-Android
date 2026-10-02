package com.tongge.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.text.InputType;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;

public final class MainActivity extends Activity {
    private String serverBase;
    private EmbeddedWebsite website;
    private static final int PICK_FILE = 1, SAVE_FILE = 2;
    private static final int CAMERA_PERMISSION = 3;
    private WebView web;
    private FrameLayout root;
    private String integrationScript;
    private PermissionRequest cameraRequest;
    private boolean scanAfterPermission;
    private ReleaseUpdates updates;
    private ValueCallback<Uri[]> fileCallback;
    private byte[] pendingDownload;

    @Override public void onCreate(Bundle state) {
        setTheme(isDarkTheme() ? android.R.style.Theme_Material_NoActionBar
            : android.R.style.Theme_Material_Light_NoActionBar);
        super.onCreate(state);
        serverBase = getSharedPreferences("server", MODE_PRIVATE).getString("base", ServerAddress.DEFAULT);
        website = new EmbeddedWebsite(this, serverBase);
        updates = new ReleaseUpdates(this);
        try (java.io.InputStream stream = getAssets().open("android-web.js")) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) != -1) bytes.write(buffer, 0, count);
            integrationScript = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("Missing Android web integration", e); }
        web = new WebView(this);
        root = new FrameLayout(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        if (android.os.Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                int types = WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime();
                Insets bars = insets.getInsets(types);
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                // The native container owns these spaces; WebView receives zero residual insets.
                return new WindowInsets.Builder(insets).setInsets(types, Insets.NONE).build();
            } else {
                int left = insets.getSystemWindowInsetLeft(), top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight(), bottom = insets.getSystemWindowInsetBottom();
                if (android.os.Build.VERSION.SDK_INT >= 28 && insets.getDisplayCutout() != null) {
                    android.view.DisplayCutout cutout = insets.getDisplayCutout();
                    left = Math.max(left, cutout.getSafeInsetLeft()); top = Math.max(top, cutout.getSafeInsetTop());
                    right = Math.max(right, cutout.getSafeInsetRight()); bottom = Math.max(bottom, cutout.getSafeInsetBottom());
                }
                view.setPadding(left, top, right, bottom);
                WindowInsets remaining = insets.consumeSystemWindowInsets();
                return android.os.Build.VERSION.SDK_INT >= 28 ? remaining.consumeDisplayCutout() : remaining;
            }
        });
        root.post(root::requestApplyInsets);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (android.os.Build.VERSION.SDK_INT >= 33) settings.setAlgorithmicDarkeningAllowed(false);
        CookieManager.getInstance().setAcceptCookie(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return website.intercept(request);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("tongge".equals(uri.getScheme()) && "scan".equals(uri.getHost())) {
                    if (request.isForMainFrame() && view.getUrl() != null && isTrustedSite(Uri.parse(view.getUrl()))) requestScan();
                    return true;
                }
                if ("tongge".equals(uri.getScheme()) && "updates".equals(uri.getHost())) {
                    if (request.isForMainFrame() && view.getUrl() != null && isTrustedSite(Uri.parse(view.getUrl())))
                        updates.check(true);
                    return true;
                }
                if ("tongge".equals(uri.getScheme()) && "server".equals(uri.getHost())) {
                    if (request.isForMainFrame() && view.getUrl() != null && isTrustedSite(Uri.parse(view.getUrl())))
                        showServerSettings();
                    return true;
                }
                if (isTrustedSite(uri)) return false;
                if ("data".equals(uri.getScheme())) { saveData(uri.toString(), "同格课表.png"); return true; }
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception e) { toast("无法打开链接"); }
                return true;
            }
            @Override public void onPageCommitVisible(WebView view, String url) { applySystemTheme(); }
            @Override public void onPageFinished(WebView view, String url) { applySystemTheme(); }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                if (!ServerAddress.sameOrigin(serverBase, request.getOrigin().toString())
                        || web.getUrl() == null || !isTrustedSite(Uri.parse(web.getUrl()))
                        || !java.util.Arrays.asList(request.getResources())
                        .contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) { request.deny(); return; }
                if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    request.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
                } else if (cameraRequest == null) {
                    cameraRequest = request;
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                } else request.deny();
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (cameraRequest == request) cameraRequest = null;
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                    FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.setType("*/*");
                picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                try { startActivityForResult(picker, PICK_FILE); }
                catch (Exception e) { fileCallback.onReceiveValue(null); fileCallback = null; toast("无法打开文件选择器"); }
                return true;
            }
        });
        applySystemTheme();
        web.setDownloadListener((url, userAgent, disposition, mime, length) -> {
            String name = android.webkit.URLUtil.guessFileName(url, disposition, mime);
            if (url.startsWith("data:")) { saveData(url, name); return; }
            if (url.startsWith("blob:")) { toast("暂不支持此下载格式"); return; }
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) request.addRequestHeader("Cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
                toast("已开始下载");
            } catch (Exception e) { toast("下载失败"); }
        });
        if (state == null || !serverBase.equals(state.getString("serverBase"))) {
            web.loadUrl(ServerAddress.localHome(serverBase));
        } else {
            web.restoreState(state);
            if (web.getUrl() == null || !isTrustedSite(Uri.parse(web.getUrl())))
                web.loadUrl(ServerAddress.localHome(serverBase));
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                () -> { if (web.canGoBack()) web.goBack(); else finish(); });
        }
        root.postDelayed(() -> updates.check(false), 800);
    }

    private boolean isTrustedSite(Uri uri) {
        return ServerAddress.sameOrigin(serverBase, uri.toString())
            && (ServerAddress.LOCAL_PATH + "index.html").equals(uri.getPath());
    }

    private void showServerSettings() {
        EditText address = new EditText(this);
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        address.setText(serverBase);
        address.setSingleLine(false);
        int margin = (int) (24 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(margin, 0, margin, 0);
        container.addView(address, new FrameLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("服务器设置")
            .setMessage("填写完整 HTTPS 地址，包含部署路径。更换后需重新登录。")
            .setView(container).setNegativeButton("取消", null).setPositiveButton("保存", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            final String next;
            try { next = ServerAddress.normalize(address.getText().toString()); }
            catch (IllegalArgumentException e) { address.setError(e.getMessage()); return; }
            if (next.equals(serverBase)) { dialog.dismiss(); return; }
            // Clear the old deployment's login before changing either host or base path.
            web.evaluateJavascript("if(window.API)API.setToken('');", result -> {
                getSharedPreferences("server", MODE_PRIVATE).edit().putString("base", next).apply();
                dialog.dismiss();
                recreate();
            });
        }));
        dialog.show();
    }
    @Override protected void onResume() {
        super.onResume();
        if (updates != null) updates.onResume();
    }
    @Override protected void onPause() {
        if (updates != null) updates.onPause();
        super.onPause();
    }
    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused && updates != null) updates.deliverPending();
    }
    private boolean isDarkTheme() {
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
    }
    private void applySystemTheme() {
        boolean dark = isDarkTheme();
        int background = dark ? Color.BLACK : Color.rgb(242, 242, 247);
        root.setBackgroundColor(background);
        web.setBackgroundColor(background);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int light = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(dark ? 0 : light, light);
            }
        } else {
            View decor = getWindow().getDecorView();
            int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            decor.setSystemUiVisibility((decor.getSystemUiVisibility() & ~light) | (dark ? 0 : light));
        }
        getWindow().setStatusBarColor(background);
        getWindow().setNavigationBarColor(background);
        String url = web.getUrl();
        if (url != null && isTrustedSite(Uri.parse(url))) {
            web.evaluateJavascript("window.__tonggeSystemDark=" + dark + ";" + integrationScript, null);
        }
    }
    @Override public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        applySystemTheme();
        root.requestApplyInsets();
    }
    private void requestScan() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) openScanner();
        else {
            scanAfterPermission = true;
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }
    private void openScanner() {
        new IntentIntegrator(this).setCaptureActivity(ScanActivity.class)
            .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE).setOrientationLocked(false)
            .setBeepEnabled(false).setPrompt("扫描同格群组二维码").initiateScan();
    }
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if (request != CAMERA_PERMISSION) return;
        boolean allowed = grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED;
        if (cameraRequest != null) {
            if (allowed) cameraRequest.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE});
            else cameraRequest.deny();
            cameraRequest = null;
        }
        if (scanAfterPermission) {
            scanAfterPermission = false;
            if (allowed) openScanner();
        }
        if (!allowed) toast("摄像头未授权，可在系统设置中开启");
    }
    private void handleScan(String text) {
        String code = text.trim();
        if (!code.matches("[0-9]{6}|[0-9]{8}")) {
            Uri uri = Uri.parse(code);
            if (!ServerAddress.sameOrigin(serverBase, uri.toString())
                    || !Uri.parse(serverBase).getPath().equals(uri.getPath())) {
                toast("请扫描同格群组二维码"); return;
            }
            code = uri.getQueryParameter("code");
        }
        if (code == null || !code.matches("[0-9]{6}|[0-9]{8}")) {
            toast("二维码中没有有效邀请码"); return;
        }
        web.loadUrl(ServerAddress.localHome(serverBase) + "?code=" + Uri.encode(code));
    }

    private void saveData(String url, String name) {
        try {
            int comma = url.indexOf(',');
            if (comma < 0 || !url.substring(0, comma).endsWith(";base64")) throw new IllegalArgumentException();
            pendingDownload = Base64.decode(url.substring(comma + 1), Base64.DEFAULT);
            String mime = url.substring(5, url.indexOf(';'));
            Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            save.addCategory(Intent.CATEGORY_OPENABLE);
            save.setType(mime);
            save.putExtra(Intent.EXTRA_TITLE, name);
            startActivityForResult(save, SAVE_FILE);
        } catch (Exception e) { pendingDownload = null; toast("保存失败"); }
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        IntentResult scan = IntentIntegrator.parseActivityResult(request, result, data);
        if (scan != null) {
            if (scan.getContents() != null) handleScan(scan.getContents());
            return;
        }
        if (request == PICK_FILE && fileCallback != null) {
            Uri[] files = null;
            if (result == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    files = new Uri[data.getClipData().getItemCount()];
                    for (int i = 0; i < files.length; i++) files[i] = data.getClipData().getItemAt(i).getUri();
                } else if (data.getData() != null) files = new Uri[]{data.getData()};
            }
            fileCallback.onReceiveValue(files);
            fileCallback = null;
        }
        if (request == SAVE_FILE && pendingDownload != null) {
            if (result == RESULT_OK && data != null && data.getData() != null) {
                try (OutputStream stream = getContentResolver().openOutputStream(data.getData())) {
                    stream.write(pendingDownload);
                    toast("已保存");
                } catch (Exception e) { toast("保存失败"); }
            }
            pendingDownload = null;
        }
    }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("serverBase", serverBase);
        web.saveState(state);
    }
    // API 33+ uses the native OnBackInvokedDispatcher registered in onCreate;
    // this callback remains exclusively for Android 26-32.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() {
        if (fileCallback != null) fileCallback.onReceiveValue(null);
        if (cameraRequest != null) cameraRequest.deny();
        if (updates != null) updates.close();
        web.destroy();
        super.onDestroy();
    }
}
