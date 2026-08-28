package com.xnethub.xnet_hub_theme;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class XnetEdgeToEdge {

    private XnetEdgeToEdge() {}

    /**
     * Enable Edge-to-Edge window layout with transparent status bar and themed navigation bar.
     */
    public static void enable(@NonNull AppCompatActivity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            String theme = XnetThemeManager.getTheme(activity);
            boolean isLight = XnetThemeManager.THEME_LIGHT.equals(theme);
            if (XnetThemeManager.THEME_SYSTEM.equals(theme)) {
                int currentNightMode = activity.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                isLight = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_NO;
            }
            controller.setAppearanceLightStatusBars(isLight);
            controller.setAppearanceLightNavigationBars(isLight);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }
    }

    /**
     * Expands a 0dp spacer View at the top to match the exact status bar height.
     */
    public static void applyTopSpacer(@NonNull View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            return;
        }
        final int initialHeight = Math.max(0, layoutParams.height);
        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams params = target.getLayoutParams();
            if (params != null) {
                int targetHeight = initialHeight + systemBars.top;
                if (params.height != targetHeight) {
                    params.height = targetHeight;
                    target.setLayoutParams(params);
                }
            }
            return insets;
        });
        requestInsets(view);
    }

    /**
     * Expands a container View at the top to status bar height AND attaches a dedicated
     * animated backdrop graphics layer directly inside the status bar background area.
     */
    public static void applyTopSpacerWithAnimation(@NonNull View view) {
        applyTopSpacer(view);
    }

    /**
     * Expands a container View at the bottom to navigation bar height.
     */
    public static void applyBottomSpacerWithAnimation(@NonNull View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            return;
        }
        final int initialHeight = Math.max(0, layoutParams.height);
        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams params = target.getLayoutParams();
            if (params != null) {
                int targetHeight = initialHeight + systemBars.bottom;
                if (params.height != targetHeight) {
                    params.height = targetHeight;
                    target.setLayoutParams(params);
                }
            }
            return insets;
        });
        requestInsets(view);
    }

    /**
     * Applies top and bottom margins to a drawer panel so its cut-corner shell sits inside visible bounds.
     */
    public static void applyDrawerInsets(@NonNull View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        final InsetsState initial = InsetsState.captureMargins((ViewGroup.MarginLayoutParams) layoutParams);
        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams params = target.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                marginParams.leftMargin = initial.left;
                marginParams.topMargin = initial.top + systemBars.top;
                marginParams.rightMargin = initial.right;
                marginParams.bottomMargin = initial.bottom + systemBars.bottom;
                target.setLayoutParams(marginParams);
            }
            return insets;
        });
        requestInsets(view);
    }

    /**
     * Applies bottom margin inset for FAB or bottom buttons.
     */
    public static void applyBottomInsetMargin(@NonNull View view) {
        applyMarginInsets(view, false, true);
    }

    public static void applyMarginInsets(@NonNull View view, boolean applyTop, boolean applyBottom) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        final InsetsState initial = InsetsState.captureMargins((ViewGroup.MarginLayoutParams) layoutParams);
        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.LayoutParams params = target.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                marginParams.leftMargin = initial.left + systemBars.left;
                marginParams.topMargin = initial.top + (applyTop ? systemBars.top : 0);
                marginParams.rightMargin = initial.right + systemBars.right;
                marginParams.bottomMargin = initial.bottom + (applyBottom ? systemBars.bottom : 0);
                target.setLayoutParams(marginParams);
            }
            return insets;
        });
        requestInsets(view);
    }

    public static void applyContentInsets(@NonNull View view, boolean applyTop, boolean applyBottom) {
        final InsetsState initial = InsetsState.capturePadding(view);
        ViewCompat.setOnApplyWindowInsetsListener(view, (target, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            target.setPadding(
                    initial.left + systemBars.left,
                    initial.top + (applyTop ? systemBars.top : 0),
                    initial.right + systemBars.right,
                    initial.bottom + (applyBottom ? systemBars.bottom : 0)
            );
            return insets;
        });
        requestInsets(view);
    }

    private static void requestInsets(@NonNull View view) {
        if (ViewCompat.isAttachedToWindow(view)) {
            ViewCompat.requestApplyInsets(view);
            return;
        }
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                ViewCompat.requestApplyInsets(v);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
    }

    private static final class InsetsState {
        final int left;
        final int top;
        final int right;
        final int bottom;

        private InsetsState(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        static InsetsState capturePadding(@NonNull View view) {
            return new InsetsState(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
        }

        static InsetsState captureMargins(@NonNull ViewGroup.MarginLayoutParams params) {
            return new InsetsState(
                    params.leftMargin,
                    params.topMargin,
                    params.rightMargin,
                    params.bottomMargin
            );
        }
    }
}
