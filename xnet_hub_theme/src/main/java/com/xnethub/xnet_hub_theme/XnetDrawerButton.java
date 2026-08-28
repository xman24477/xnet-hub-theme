package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

/**
 * XnetDrawerButton
 *
 * Custom themed Navigation Drawer Button widget.
 * Automatically switches icon based on active theme mode:
 *   - X-Cyber Theme : Custom Hexagon Contour 3-Line Hamburger Menu (ic_xnet_cyber_hamburger)
 *   - Classic Theme : Standard 3-Line Hamburger Menu (ic_xnet_classic_hamburger)
 */
public class XnetDrawerButton extends AppCompatImageButton {

    public XnetDrawerButton(@NonNull Context context) {
        this(context, null);
    }

    public XnetDrawerButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetDrawerButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        TypedValue tv = new TypedValue();
        boolean isCyberTheme = false;
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            isCyberTheme = tv.data != 0;
        }

        // Apply Icon based on active theme
        if (isCyberTheme) {
            setImageResource(R.drawable.ic_xnet_cyber_hamburger);
        } else {
            setImageResource(R.drawable.ic_xnet_classic_hamburger);
        }

        // Apply Tint
        if (context.getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            setImageTintList(ColorStateList.valueOf(tv.data));
        }

        // Apply Selectable Ripple Background & ScaleType
        if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tv, true)) {
            setBackgroundResource(tv.resourceId);
        }
        setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        setContentDescription("Open drawer");
    }
}
