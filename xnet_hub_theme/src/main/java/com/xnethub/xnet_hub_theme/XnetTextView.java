package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * XnetTextView
 *
 * A specialized TextView widget for Xnet Hub applications.
 * Automatically formats company brand names (e.g., "Xnet Hub"):
 *   - The 'X' is ALWAYS forced to uppercase BOLD and Signature Neon Green (#00FF41).
 *   - "NET" matches the active theme primary text color.
 *   - "HUB" matches the active theme secondary text color.
 *
 * Usage in XML:
 * <com.xnethub.xnet_hub_theme.XnetTextView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="Xnet Hub Platform" />
 */
public class XnetTextView extends AppCompatTextView {

    private boolean isFormatting = false;

    public XnetTextView(@NonNull Context context) {
        super(context);
    }

    public XnetTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public XnetTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
