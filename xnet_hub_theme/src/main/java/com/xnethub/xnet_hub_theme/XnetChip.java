package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.google.android.material.chip.Chip;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * XnetChip
 *
 * Official Xnet Hub Theme Library Custom Cyber Chip Widget.
 * Built on top of Material {@link Chip}.
 *
 * Features:
 *   - Cyber Theme : Top-Right (8dp) & Bottom-Left (8dp) Cut-Corner Hexagon shape
 *   - Classic Theme : Rounded pill shape (16dp)
 *   - Checked / Tab Selected State :
 *       - Automatically fills background with active X-Cyber accent color (xnetAccentPrimary)
 *       - Automatically calculates high-contrast text color (#020A02 on bright neon, white on dark)
 *   - Unchecked State :
 *       - Semi-transparent surface background (xnetSurfaceAlt) + accent stroke border
 *
 * Perfect for replacing traditional TabLayouts, category filters, and tags!
 */
public class XnetChip extends Chip {

    private static final float STROKE_WIDTH_DP     = 1f;
    private static final float CYBER_CUT_DP        = 8f;
    private static final float CLASSIC_CORNER_DP   = 16f;
    private static final int   BG_ALPHA            = 0xCC; // ~80% opacity

    public XnetChip(Context context) {
        this(context, null);
    }

    public XnetChip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetChip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        boolean isCyber       = resolveBoolean(context, R.attr.xnetIsCyberTheme,  false);
        int     accentColor   = resolveColor(context, R.attr.xnetAccentPrimary,    0xFF00FFCC);
        int     surfaceAlt    = resolveColor(context, R.attr.xnetSurfaceAlt,       0xFF1A1A2E);
        int     textPrimary   = resolveColor(context, R.attr.xnetTextPrimary,      0xFFEEEEEE);

        float density = context.getResources().getDisplayMetrics().density;

        // Default checkable to true unless explicitly disabled in XML attributes
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.checkable});
            try {
                setCheckable(ta.getBoolean(0, true));
            } finally {
                ta.recycle();
            }
        } else {
            setCheckable(true);
        }

        // --- Multi-State Background Color (Checked = Accent Fill, Unchecked = SurfaceAlt) ---
        int unselectedBg = applyAlpha(surfaceAlt, BG_ALPHA);
        int[][] states = new int[][]{
            new int[]{android.R.attr.state_checked},
            new int[]{android.R.attr.state_selected},
            new int[]{}
        };

        int[] bgColors = new int[]{
            accentColor,  // Checked
            accentColor,  // Selected
            unselectedBg  // Unchecked
        };
        setChipBackgroundColor(new ColorStateList(states, bgColors));

        // --- Multi-State Text Color (Checked = Contrast Dark/Light, Unchecked = TextPrimary) ---
        int checkedTextColor = calculateContrastingColor(accentColor);
        int[] textColors = new int[]{
            checkedTextColor, // Checked
            checkedTextColor, // Selected
            textPrimary       // Unchecked
        };
        setTextColor(new ColorStateList(states, textColors));

        // --- Custom Clean Checked Icon (Web matching style) ---
        setCheckedIconResource(R.drawable.ic_xnet_chip_checked);
        setCheckedIconVisible(true);
        setCheckedIconTint(new ColorStateList(states, textColors));

        // --- Stroke ---
        setChipStrokeColor(ColorStateList.valueOf(accentColor));
        setChipStrokeWidth(STROKE_WIDTH_DP * density);

        // --- Shape ---
        ShapeAppearanceModel shape;
        if (isCyber) {
            float cutSize = CYBER_CUT_DP * density;
            shape = ShapeAppearanceModel.builder()
                    .setTopRightCorner(CornerFamily.CUT, cutSize)
                    .setBottomLeftCorner(CornerFamily.CUT, cutSize)
                    .build();
        } else {
            float cornerRadius = CLASSIC_CORNER_DP * density;
            shape = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                    .build();
        }
        setShapeAppearanceModel(shape);
    }

    /**
     * Calculates contrasting text color based on background perceived luminance.
     */
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
