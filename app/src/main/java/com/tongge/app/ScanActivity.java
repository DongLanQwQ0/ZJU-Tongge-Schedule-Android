package com.tongge.app;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.view.WindowInsets;
import com.journeyapps.barcodescanner.CaptureActivity;

/** Camera preview and QR decoding are managed by ZXing, including pause/resume cleanup. */
public final class ScanActivity extends CaptureActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            applySystemBars();
        } else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        View content = findViewById(android.R.id.content);
        content.setBackgroundColor(Color.BLACK);
        content.setOnApplyWindowInsetsListener((view, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                int types = WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout();
                android.graphics.Insets bars = insets.getInsets(types);
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return new WindowInsets.Builder(insets).setInsets(types, android.graphics.Insets.NONE).build();
            }
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        content.post(content::requestApplyInsets);
    }

    private void applySystemBars() {
        if (android.os.Build.VERSION.SDK_INT >= 30) {
            // The controller may not exist until the scanner window is attached.
            android.view.WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) controller.setSystemBarsAppearance(0,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    | android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) applySystemBars();
    }
}
