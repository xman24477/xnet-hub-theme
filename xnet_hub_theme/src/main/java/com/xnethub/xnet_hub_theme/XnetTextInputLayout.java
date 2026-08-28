package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

/**
 * XnetTextInputLayout
 *
 * Official Xnet Hub Theme Library Custom Input Layout Widget.
 * Automatically applies the cut-corner shape appearance (top-right 14dp cut,
 * bottom-left 14dp cut) for X-Cyber themes, and rounded corners for classic themes.
 */
public class XnetTextInputLayout extends TextInputLayout {

    public XnetTextInputLayout(@NonNull Context context) {
        this(context, null);
    }

    public XnetTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.textInputStyle);
    }

    public XnetTextInputLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        applyCyberShape();
        applyColorsAndStrokes();
    }

    private void applyCyberShape() {
        TypedValue tv = new TypedValue();
        boolean isCyber = false;
        if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            isCyber = tv.data != 0;
        }

        // Apply Top-Right (14dp) & Bottom-Left (14dp) Cut Corners for X-Cyber Themes
        float cutSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, getResources().getDisplayMetrics());
        float roundSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());

        ShapeAppearanceModel.Builder shapeBuilder = ShapeAppearanceModel.builder();
        if (isCyber) {
            shapeBuilder.setTopRightCorner(CornerFamily.CUT, cutSizePx)
                        .setBottomLeftCorner(CornerFamily.CUT, cutSizePx)
                        .setTopLeftCorner(CornerFamily.ROUNDED, 0f)
                        .setBottomRightCorner(CornerFamily.ROUNDED, 0f);
        } else {
            shapeBuilder.setAllCorners(CornerFamily.ROUNDED, roundSizePx);
        }

        ShapeAppearanceModel shapeModel = shapeBuilder.build();
        setShapeAppearanceModel(shapeModel);
        setBoxBackgroundMode(BOX_BACKGROUND_OUTLINE);
    }

    private void applyColorsAndStrokes() {
        TypedValue tv = new TypedValue();
        int focusedColor = Color.GREEN;
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            focusedColor = tv.data;
        }

        if (XnetThemeManager.THEME_CYBER_RGB.equals(XnetThemeManager.getTheme(getContext()))) {
            Random r = new Random(hashCode());
            focusedColor = XnetThemeManager.getRgbAccents(getContext())[r.nextInt(XnetThemeManager.getRgbAccents(getContext()).length)];
        }

        int unfocusedColor = ColorUtils.setAlphaComponent(focusedColor, 130);

        int[][] states = new int[][] {
            new int[] { android.R.attr.state_focused },
            new int[] { android.R.attr.state_hovered },
            new int[] {}
        };
        int[] colors = new int[] {
            focusedColor,
            focusedColor,
            unfocusedColor
        };
        setBoxStrokeColorStateList(new ColorStateList(states, colors));

        int strokeNormalPx  = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.2f, getResources().getDisplayMetrics());
        int strokeFocusedPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        setBoxStrokeWidth(strokeNormalPx);
        setBoxStrokeWidthFocused(strokeFocusedPx);

        int hintColor = Color.GRAY;
        if (getContext().getTheme().resolveAttribute(R.attr.xnetTextSecondary, tv, true)) {
            hintColor = tv.data;
        }
        setHintTextColor(ColorStateList.valueOf(focusedColor));
        setDefaultHintTextColor(ColorStateList.valueOf(hintColor));

        int boxBgColor = Color.TRANSPARENT;
        if (getContext().getTheme().resolveAttribute(R.attr.xnetSurfaceAlt, tv, true)) {
            boxBgColor = ColorUtils.setAlphaComponent(tv.data, 190);
        }
        setBoxBackgroundColor(boxBgColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Re-enforce shape appearance model when attached to window so the cut corners render on screen
        applyCyberShape();
    }
}
