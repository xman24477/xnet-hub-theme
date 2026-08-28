package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import java.util.List;

/**
 * XnetSpinner
 *
 * Official Xnet Hub Theme Library Custom Spinner Widget.
 * Replaces default AppCompatSpinner with an out-of-the-box cyber-themed dropdown spinner.
 *
 * Features:
 *   - Cyber Theme : Hexagonal cut-corner shell drawable (bg_xnet_spinner_shell) + Rajdhani font
 *   - Classic Theme : Rounded shell drawable (bg_xnet_spinner_shell_classic)
 *   - Popup Background : Dark / Elevated surface tint for popup menu
 *   - Helper Method : createThemedAdapter() for single-line setup of items with themed styling
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetSpinner
 *       android:id="@+id/mySpinner"
 *       android:layout_width="match_parent"
 *       android:layout_height="56dp" />
 */
public class XnetSpinner extends AppCompatSpinner {

    private boolean mIsCyberTheme = false;

    public XnetSpinner(@NonNull Context context) {
        this(context, null);
    }

    public XnetSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.spinnerStyle);
    }

    public XnetSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

        private void init() {
        Context context = getContext();
        TypedValue tv = new TypedValue();

        // 1. Resolve Cyber vs Classic theme
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            mIsCyberTheme = tv.data != 0;
        }

        // 2. Set Theme Shell Background
        if (mIsCyberTheme) {
            setBackgroundResource(R.drawable.bg_xnet_spinner_shell);
        } else {
            setBackgroundResource(R.drawable.bg_xnet_spinner_shell_classic);
        }

        // 3. Set Spacing & Padding
        float density = context.getResources().getDisplayMetrics().density;
        int padH = (int) (14 * density);
        int padV = (int) (10 * density);
        setPadding(padH, padV, (int)(36 * density), padV);

        int minH = (int) context.getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_shell_height);
        setMinimumHeight(minH);
        
        // Ensure dropdown appears below the spinner anchor
        setDropDownVerticalOffset(minH);

        // 4. Set Popup Background
        int popupBgColor = 0xFF1E1E1E;
        if (context.getTheme().resolveAttribute(R.attr.xnetSurfaceRaised, tv, true)) {
            popupBgColor = tv.data;
        } else if (context.getTheme().resolveAttribute(R.attr.xnetBackgroundAlt, tv, true)) {
            popupBgColor = tv.data;
        }

        int strokeColor = 0x44FFFFFF;
        if (context.getTheme().resolveAttribute(R.attr.xnetStroke, tv, true)) {
            strokeColor = tv.data;
        }

        if (mIsCyberTheme) {
            float cutRadius = context.getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_shell_cut_top_right);
            float strokeWidth = context.getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_popup_stroke_width);
            
            com.google.android.material.shape.ShapeAppearanceModel shapeModel = new com.google.android.material.shape.ShapeAppearanceModel.Builder()
                    .setTopRightCorner(new com.google.android.material.shape.CutCornerTreatment())
                    .setTopRightCornerSize(cutRadius)
                    .setBottomLeftCorner(new com.google.android.material.shape.CutCornerTreatment())
                    .setBottomLeftCornerSize(cutRadius)
                    .build();
            com.google.android.material.shape.MaterialShapeDrawable popupBg = new com.google.android.material.shape.MaterialShapeDrawable(shapeModel);
            popupBg.setFillColor(android.content.res.ColorStateList.valueOf(popupBgColor));
            popupBg.setStroke((int)strokeWidth, strokeColor);
            setPopupBackgroundDrawable(popupBg);
        } else {
            float cornerRadius = context.getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_popup_radius);
            float strokeWidthClassic = context.getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_popup_stroke_width_classic);
            GradientDrawable popupBg = new GradientDrawable();
            popupBg.setColor(popupBgColor);
            popupBg.setCornerRadius(cornerRadius);
            popupBg.setStroke((int)strokeWidthClassic, strokeColor);
            setPopupBackgroundDrawable(popupBg);
        }


    }

    @Override
    public boolean performClick() {
        try {
            java.lang.reflect.Field popupField = androidx.appcompat.widget.AppCompatSpinner.class.getDeclaredField("mPopup");
            popupField.setAccessible(true);
            Object popupObj = popupField.get(this);
            if (popupObj instanceof androidx.appcompat.widget.ListPopupWindow) {
                androidx.appcompat.widget.ListPopupWindow popup = (androidx.appcompat.widget.ListPopupWindow) popupObj;
                int maxItems = (int) getResources().getInteger(R.integer.xnet_bg_xnet_spinner_popup_max_items);
                int count = getAdapter() != null ? getAdapter().getCount() : 0;
                if (count > maxItems) {
                    popup.setHeight((int) getResources().getDimension(R.dimen.xnet_bg_xnet_spinner_popup_max_height));
                } else {
                    popup.setHeight(android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }
        } catch (Exception ignored) {}

        boolean handled = super.performClick();
        
        try {
            java.lang.reflect.Field popupField = androidx.appcompat.widget.AppCompatSpinner.class.getDeclaredField("mPopup");
            popupField.setAccessible(true);
            Object popupObj = popupField.get(this);
            if (popupObj instanceof androidx.appcompat.widget.ListPopupWindow) {
                android.widget.ListView listView = ((androidx.appcompat.widget.ListPopupWindow) popupObj).getListView();
                if (listView != null) {
                    listView.setVerticalScrollBarEnabled(false);
                }
            }
        } catch (Exception ignored) {}
        
        return handled;
    }

    /**
     * Helper to set a list of String items with full theme styling (colors, font, padding).
     */
    public void setItems(@NonNull List<String> items) {
        setAdapter(createThemedAdapter(getContext(), items));
    }

    /**
     * Creates an ArrayAdapter tailored for Xnet Hub Theme styling.
     */
    public static ArrayAdapter<String> createThemedAdapter(@NonNull Context context, @NonNull List<String> items) {
        TypedValue tv = new TypedValue();

        int textColor = Color.WHITE;
        if (context.getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            textColor = tv.data;
        }

        boolean isCyber = false;
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            isCyber = tv.data != 0;
        }

        final int finalTextColor = textColor;
        final boolean finalIsCyber = isCyber;

        return new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(finalTextColor);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
                    if (finalIsCyber) {
                        try {
                            Typeface f = context.getResources().getFont(R.font.rajdhani);
                            if (f != null) tv.setTypeface(f, Typeface.BOLD);
                        } catch (Exception ignored) {}
                    }
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(finalTextColor);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                    int pad = (int) (14 * context.getResources().getDisplayMetrics().density);
                    tv.setPadding(pad, pad, pad, pad);
                    if (finalIsCyber) {
                        try {
                            Typeface f = context.getResources().getFont(R.font.rajdhani);
                            if (f != null) tv.setTypeface(f);
                        } catch (Exception ignored) {}
                    }
                }
                return v;
            }
        };
    }
}
