package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

/**
 * XnetSnackbar
 *
 * A themed Snackbar utility that automatically applies Xnet Hub Theme colors.
 * Provides static factory methods to show themed snackbars from any Activity/Fragment.
 *
 * Usage:
 *   XnetSnackbar.show(rootView, "Connection established.");
 *   XnetSnackbar.showError(rootView, "Authorization failed.");
 */
public class XnetSnackbar {

    private XnetSnackbar() {} // utility class

    /** Show a standard themed informational snackbar. */
    public static Snackbar show(@NonNull View rootView, @NonNull String message) {
        return show(rootView, message, Snackbar.LENGTH_SHORT, false);
    }

    /** Show a themed error/warning snackbar. */
    public static Snackbar showError(@NonNull View rootView, @NonNull String message) {
        return show(rootView, message, Snackbar.LENGTH_LONG, true);
    }

    /** Show a long-duration snackbar with an action. */
    public static Snackbar showWithAction(
            @NonNull View rootView,
            @NonNull String message,
            @NonNull String actionLabel,
            @NonNull View.OnClickListener actionListener) {
        Snackbar snackbar = build(rootView, message, Snackbar.LENGTH_INDEFINITE, false);
        snackbar.setAction(actionLabel, actionListener);

        // Style the action button text color
        TypedValue tv = new TypedValue();
        if (rootView.getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            snackbar.setActionTextColor(tv.data);
        }
        snackbar.show();
        return snackbar;
    }

    private static Snackbar show(
            @NonNull View rootView, @NonNull String message, int duration, boolean isError) {
        Snackbar snackbar = build(rootView, message, duration, isError);
        snackbar.show();
        return snackbar;
    }

    private static Snackbar build(
            @NonNull View rootView, @NonNull String message, int duration, boolean isError) {
        Snackbar snackbar = Snackbar.make(rootView, message, duration);

        Context ctx = rootView.getContext();
        TypedValue tv = new TypedValue();

        // Background color
        int bgColor = androidx.core.content.ContextCompat.getColor(ctx, R.color.xnet_color_classic_dark_surface_raised);
        if (ctx.getTheme().resolveAttribute(R.attr.xnetSurfaceRaised, tv, true)) {
            bgColor = tv.data;
        }

        // Text color
        int textColor = Color.WHITE;
        if (ctx.getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            textColor = tv.data;
        }

        // Error: use a hard-coded red tone for action since xnetAccentNegative attr is not yet declared
        if (isError) {
            snackbar.setActionTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.xnet_color_cyber_rgb_accent_primary));
        }

        snackbar.setBackgroundTint(bgColor);
        snackbar.setTextColor(textColor);

        return snackbar;
    }
}
