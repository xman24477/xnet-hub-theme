package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.tabs.TabLayout;

/**
 * XnetTabLayout
 *
 * Official Xnet Hub Theme Library Custom Cyber TabLayout Widget.
 * Built on top of Material {@link TabLayout}.
 *
 * Features:
 *   - Cyber Theme :
 *       - Header Container Shell : 14dp Cut-Corner (Top-Right & Bottom-Left) + Accent Stroke Border
 *       - Selected Tab : Solid 8dp Cut-Corner Hexagon Fill with Active X-Cyber Accent Color (xnetAccentPrimary)
 *                        + Dynamic High-Contrast Text Color (#020A02 on bright neon, white on dark)
 *                        + Rajdhani Typeface
 *       - Unselected Tab : Clean Transparent Background + Secondary Text Color
 *       - Indicator Line : Removed for Cyber theme (replaced by full Cut-Corner Tab Fill)
 *
 *   - Classic / Normal / System Theme :
 *       - Standard Material TabLayout behavior with accent indicator bar and clean surface header.
 */
public class XnetTabLayout extends TabLayout {

    private boolean mIsCyberTheme = false;
    private int mAccentColor = 0xFF00FFCC;
    private int mSurfaceAlt = 0xFF1A1A2E;
    private int mTextPrimary = Color.WHITE;
    private int mTextSecondary = Color.GRAY;
    private int mStrokeColor = 0xFF00FFCC;
    private Typeface mRajdhaniFont;

    public XnetTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public XnetTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mIsCyberTheme = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);
        mAccentColor = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mSurfaceAlt = resolveColor(context, R.attr.xnetSurfaceAlt, 0xFF1A1A2E);
        mTextPrimary = resolveColor(context, R.attr.xnetTextPrimary, Color.WHITE);
        mTextSecondary = resolveColor(context, R.attr.xnetTextSecondary, Color.GRAY);
        mStrokeColor = resolveColor(context, R.attr.xnetStroke, mAccentColor);

        float density = context.getResources().getDisplayMetrics().density;

        if (mIsCyberTheme) {
            try {
                mRajdhaniFont = context.getResources().getFont(R.font.rajdhani);
            } catch (Exception ignored) {}

            // Outer Header Shell — 14dp Cut Corner (Top-Right & Bottom-Left) + Stroke
            Drawable headerShell = createCyberHeaderShell(mSurfaceAlt, mStrokeColor, 1.5f, 14f, density);
            setBackground(headerShell);

            // Remove standard bottom indicator line for Cyber theme
            setSelectedTabIndicatorHeight(0);
            setSelectedTabIndicator(null);
            setTabRippleColor(ColorStateList.valueOf(applyAlpha(mAccentColor, 0x33)));

            // Equal 5dp padding on all 4 sides (top, bottom, left, right) for uniform spacing around outer shell stroke
            post(() -> {
                if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup) {
                    ViewGroup tabStrip = (ViewGroup) getChildAt(0);
                    int pad = (int) (5 * density);
                    tabStrip.setPadding(pad, pad, pad, pad);
                    tabStrip.setClipToPadding(false);
                }
            });
        } else {
            // Classic Theme Setup
            setSelectedTabIndicatorColor(mAccentColor);
            setTabTextColors(mTextSecondary, mAccentColor);
        }

        // Selection Listener for dynamic tab shape & color transitions
        addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                updateTabStyles();
            }

            @Override
            public void onTabUnselected(Tab tab) {
                updateTabStyles();
            }

            @Override
            public void onTabReselected(Tab tab) {
                updateTabStyles();
            }
        });
    }

    @Override
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        super.addTab(tab, setSelected);
        post(this::updateTabStyles);
    }

    @Override
    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);
        post(this::updateTabStyles);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        updateTabStyles();
    }

    public void updateTabStyles() {
        if (!mIsCyberTheme) return;

        float density = getResources().getDisplayMetrics().density;
        int checkedTextColor = calculateContrastingColor(mAccentColor);
        int totalTabs = getTabCount();

        for (int i = 0; i < totalTabs; i++) {
            Tab tab = getTabAt(i);
            if (tab == null || tab.view == null) continue;

            View tabView = tab.view;
            boolean isSelected = tab.isSelected();

            if (isSelected) {
                // Selected Tab: Uniform Symmetrical 8dp Cut Corner (Top-Right & Bottom-Left)
                Drawable selectedShape = createCyberTabShape(mAccentColor, 8f, density);
                tabView.setBackground(selectedShape);
            } else {
                // Unselected Tab: Transparent Background
                tabView.setBackground(null);
            }

            // Style inner TextView
            if (tabView instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) tabView;
                for (int j = 0; j < vg.getChildCount(); j++) {
                    View child = vg.getChildAt(j);
                    if (child instanceof TextView) {
                        TextView tv = (TextView) child;
                        tv.setTextColor(isSelected ? checkedTextColor : mTextSecondary);
                        if (mRajdhaniFont != null) {
                            tv.setTypeface(mRajdhaniFont, isSelected ? Typeface.BOLD : Typeface.NORMAL);
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    private static Drawable createCyberHeaderShell(int fillColor, int strokeColor, float strokeWidthDp, float cutSizeDp, float density) {
        ShapeAppearanceModel shape = ShapeAppearanceModel.builder()
                .setTopRightCorner(CornerFamily.CUT, cutSizeDp * density)
                .setBottomLeftCorner(CornerFamily.CUT, cutSizeDp * density)
                .build();
        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shape);
        shapeDrawable.setFillColor(ColorStateList.valueOf(fillColor));
        shapeDrawable.setStroke(strokeWidthDp * density, strokeColor);
        return shapeDrawable;
    }

    public static Drawable createCyberTabShape(int fillColor, float cutSizeDp, float density) {
        float cutSize = cutSizeDp * density;
        ShapeAppearanceModel shape = ShapeAppearanceModel.builder()
                .setTopRightCorner(CornerFamily.CUT, cutSize)
                .setBottomLeftCorner(CornerFamily.CUT, cutSize)
                .build();
        MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shape);
        shapeDrawable.setFillColor(ColorStateList.valueOf(fillColor));
        return shapeDrawable;
    }

    private static int calculateContrastingColor(int bgColor) {
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 130 ? 0xFF121212 : Color.WHITE;
    }

    private static int applyAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static int resolveColor(Context context, int attr, int fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }

    private static boolean resolveBoolean(Context context, int attr, boolean fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return ta.getBoolean(0, fallback);
        } finally {
            ta.recycle();
        }
    }
}
