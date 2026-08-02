package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

/**
 * XnetButton
 *
 * Official Xnet Hub Theme Library Custom Button Widget.
 * Automatically integrates with all 7 Xnet Core themes (Normal & X-Cyber),
 * applying dynamic cut-corner shapes, theme strokes, fonts, and Cyber RGB effects.
 */
public class XnetButton extends MaterialButton {

    private boolean isFormatting = false;

    public XnetButton(@NonNull Context context) {
        this(context, null);
    }

    public XnetButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialButtonStyle);
    }

    public XnetButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Auto-apply brand formatting if button text contains Xnet / Xnet Hub
        if (getText() != null && getText().length() > 0) {
            CharSequence formatted = XnetTextFormatter.formatBrandText(getContext(), getText());
            setText(formatted);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (isFormatting || text == null || isInEditMode()) {
            super.setText(text, type);
            return;
        }
        isFormatting = true;
        CharSequence formatted = XnetTextFormatter.formatBrandText(getContext(), text);
        super.setText(formatted, type);
        isFormatting = false;
    }
}
