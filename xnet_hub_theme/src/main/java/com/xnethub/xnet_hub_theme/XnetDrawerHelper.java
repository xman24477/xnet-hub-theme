package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * XnetDrawerHelper
 *
 * Utility class for styling a standard DrawerLayout with XnetCore theme.
 *
 * - X-Cyber themes  → Cut-corner DrawerShell shape + accent stroke + dark scrim
 * - Classic themes  → Normal rounded DrawerShell (no cut) + muted stroke + lighter scrim
 *
 * Usage:
 *   XnetDrawerHelper.applyDrawerStyle(context, drawerLayout, drawerPanel);
 *
 * Call again after a theme switch to update.
 */
public final class XnetDrawerHelper {

    private XnetDrawerHelper() { /* static util */ }

    /**
     * Apply XnetCore-themed styling to a DrawerLayout's side panel.
     *
     * @param context       themed context (Activity)
     * @param drawerLayout  root DrawerLayout
     * @param drawerPanel   the MaterialCardView that is the drawer panel (gravity=start)
     */
    public static void applyDrawerStyle(
            @NonNull Context context,
            @NonNull DrawerLayout drawerLayout,
            @NonNull MaterialCardView drawerPanel) {

        boolean isCyber = isCyberTheme(context);

        // ── Shape (100% theme-driven from ?attr/xnetShapeDrawerShell) ────────
        int shapeStyleRes = resolveThemeResource(context, R.attr.xnetShapeDrawerShell,
                R.style.ShapeAppearanceOverlay_XnetCore_DrawerShell_Classic);
        ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder(context, shapeStyleRes, 0).build();
        drawerPanel.setShapeAppearanceModel(shapeModel);

        // ── Stroke colour ────────────────────────────────────────────────────
        int strokeColor = resolveColor(context,
                isCyber ? R.attr.xnetStroke : R.attr.xnetStrokeSoft);
        drawerPanel.setStrokeColor(ColorStateList.valueOf(strokeColor));
        drawerPanel.setStrokeWidth(dpInt(context, 1f));

        // ── Background colour ─────────────────────────────────────────────────
        int bgColor = resolveColor(context, R.attr.xnetBackground);
        drawerPanel.setCardBackgroundColor(bgColor);

        // ── DrawerLayout scrim ───────────────────────────────────────────────
        int scrimColor = isCyber
                ? Color.argb(168, 0, 0, 0)
                : Color.argb(100, 0, 0, 0);
        drawerLayout.setScrimColor(scrimColor);

        // ── Drawer Panel Edge-to-Edge Margin Inset (DriveX exact implementation) ──
        XnetEdgeToEdge.applyDrawerInsets(drawerPanel);
    }

    /**
     * Apply styling to the profile card inside the drawer.
     *
     * @param context      themed context
     * @param profileCard  the inner profile MaterialCardView
     */
    public static void applyProfileCardStyle(
            @NonNull Context context,
            @NonNull MaterialCardView profileCard) {

        int shapeStyleRes = resolveThemeResource(context, R.attr.xnetShapeDrawerProfile,
                R.style.ShapeAppearanceOverlay_XnetCore_DrawerProfile_Classic);
        ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder(context, shapeStyleRes, 0).build();
        profileCard.setShapeAppearanceModel(shapeModel);

        int bgColor = resolveColor(context, R.attr.xnetSurfaceRaised);
        profileCard.setCardBackgroundColor(bgColor);

        int strokeColor = resolveColor(context, R.attr.xnetStrokeSoft);
        profileCard.setStrokeColor(ColorStateList.valueOf(strokeColor));
        profileCard.setStrokeWidth(dpInt(context, 1f));
    }

    /**
     * Apply themed divider colour to a View used as a horizontal divider.
     */
    public static void applyDividerStyle(@NonNull Context context, @NonNull View divider) {
        divider.setBackgroundColor(resolveColor(context, R.attr.xnetStrokeSoft));
    }

    /**
     * Show or hide the profile header card in the drawer layout.
     * Useful for apps that do not require a profile section in their drawer.
     */
    public static void setProfileHeaderVisible(@Nullable View profileCardView, boolean visible) {
        if (profileCardView != null) {
            profileCardView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Theme detection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the current theme is one of the X-Cyber variants.
     * Cyber themes have xnetIsCyberTheme = true in their theme definition.
     */
    public static boolean isCyberTheme(@NonNull Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            return tv.data != 0;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static int resolveColor(@NonNull Context context, int attrRes) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attrRes, tv, true)) {
            return tv.data;
        }
        return Color.GRAY;
    }

    private static int resolveThemeResource(@NonNull Context context, int attrRes, int defaultStyleRes) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attrRes, tv, true)) {
            return tv.resourceId != 0 ? tv.resourceId : defaultStyleRes;
        }
        return defaultStyleRes;
    }

    private static int dpInt(@NonNull Context context, float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }
}
