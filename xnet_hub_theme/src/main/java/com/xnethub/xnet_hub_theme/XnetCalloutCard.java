package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;



/**
 * XnetCalloutCard
 *
 * A themed info/warning callout block matching the Hacker Theme Design System guideline.
 *
 * Displays an icon + title + message in a left-bordered card:
 *  - INFO    : accent color (green/theme) left border
 *  - WARNING : amber/gold left border
 *  - ERROR   : red left border
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetCalloutCard
 *       android:layout_width="match_parent"
 *       android:layout_height="wrap_content"
 *       app:calloutType="info"
 *       app:calloutMessage="System initialized successfully." />
 */



public class XnetCalloutCard extends LinearLayout {

    public enum CalloutType { INFO, WARNING, ERROR }

    private TextView tvPrefix;
    private TextView tvMessage;
    private View leftBorder;

    private CalloutType calloutType = CalloutType.INFO;

    public XnetCalloutCard(@NonNull Context context) {
        this(context, null);
    }

    public XnetCalloutCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetCalloutCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        int paddingPx = dp(12);
        setPadding(0, paddingPx, paddingPx, paddingPx);

        // Left border strip
        leftBorder = new View(context);
        LayoutParams borderParams = new LayoutParams(dp(3), LayoutParams.MATCH_PARENT);
        borderParams.setMarginEnd(dp(14));
        leftBorder.setLayoutParams(borderParams);
        addView(leftBorder);

        // Text container
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(VERTICAL);
        textContainer.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        addView(textContainer);

        // Prefix label ("> NOTE" / "> WARNING" / "> ERROR")
        tvPrefix = new TextView(context);
        tvPrefix.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        tvPrefix.setLetterSpacing(0.18f);
        // Prefix strings are already uppercase literals ("> NOTE", "> WARNING", "> ERROR")
        LayoutParams prefixParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        prefixParams.bottomMargin = dp(5);
        tvPrefix.setLayoutParams(prefixParams);
        textContainer.addView(tvPrefix);

        // Message text
        tvMessage = new TextView(context);
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tvMessage.setLineSpacing(0f, 1.6f);
        textContainer.addView(tvMessage);

        applyTheme(CalloutType.INFO, "System ready.");
    }

    public void setCalloutType(CalloutType type) {
        this.calloutType = type;
        applyTheme(type, tvMessage.getText().toString());
    }

    public void setMessage(@NonNull String message) {
        tvMessage.setText(message);
    }

    public void set(CalloutType type, @NonNull String message) {
        applyTheme(type, message);
    }

    private void applyTheme(CalloutType type, String message) {
        TypedValue tv = new TypedValue();
        Context ctx = getContext();

        int accentColor;
        String prefix;

        switch (type) {
            case WARNING:
                accentColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_orange_accent_primary);
                prefix = "> WARNING";
                break;
            case ERROR:
                // Use a hard-coded red since xnetAccentNegative attr is not yet declared
                accentColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_rgb_accent_primary);
                prefix = "> ERROR";
                break;
            default: // INFO
                accentColor = Color.GREEN;
                if (ctx.getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
                    accentColor = tv.data;
                }
                prefix = "> NOTE";
                break;
        }

        // Apply left border color
        leftBorder.setBackgroundColor(accentColor);

        // Apply card background (very subtle tint of accent)
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(ColorUtils.setAlphaComponent(accentColor, 18));
        bgDrawable.setStroke(0, Color.TRANSPARENT);
        setBackground(bgDrawable);

        // Apply prefix color and text
        tvPrefix.setTextColor(accentColor);
        tvPrefix.setText(prefix);

        // Apply message color (slightly dimmer)
        int textColor = Color.WHITE;
        if (ctx.getTheme().resolveAttribute(R.attr.xnetTextSecondary, tv, true)) {
            textColor = tv.data;
        }
        tvMessage.setTextColor(textColor);
        tvMessage.setText(message);
    }

    private int dp(float dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
