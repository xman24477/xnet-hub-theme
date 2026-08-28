package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

/**
 * XnetEditText
 *
 * Official Xnet Hub Theme Library Custom EditText Widget.
 * Pairs with XnetTextInputLayout to provide a cyber-themed text input field.
 * Automatically clears default background so XnetTextInputLayout's cut-corner outline
 * is 100% visible, and applies themed fonts and colors.
 */
public class XnetEditText extends TextInputEditText {

    public XnetEditText(@NonNull Context context) {
        this(context, null);
    }

    public XnetEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public XnetEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Clear default Android/Material background so XnetTextInputLayout's cut-corner box outline is 100% visible
        setBackground(null);

        // Set premium spacing and size defaults
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 54f, getResources().getDisplayMetrics()));

        int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14f, getResources().getDisplayMetrics());
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        // Resolve and apply primary text color
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            setTextColor(tv.data);
        }

        // Resolve and apply secondary hint text color
        if (getContext().getTheme().resolveAttribute(R.attr.xnetTextSecondary, tv, true)) {
            setHintTextColor(tv.data);
        }

        // Apply Rajdhani font for Cyber themes
        try {
            boolean isCyber = false;
            if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
                isCyber = tv.data != 0;
            }
            if (isCyber) {
                Typeface font = getContext().getResources().getFont(R.font.rajdhani);
                if (font != null) setTypeface(font);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            clearFocus(); // Automatically dismiss active focus when keyboard is closed with back button
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
