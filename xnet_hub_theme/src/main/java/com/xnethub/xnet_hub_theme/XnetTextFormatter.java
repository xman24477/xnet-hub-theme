package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class XnetTextFormatter {

    // Signature Brand Neon Green (#00FF41) for Dark, CyberBlack, and Cyber themes
    public static final int BRAND_NEON_GREEN = 0xFF00FF41;
    // Slightly deeper green (#00CC33) for White/Light theme contrast
    public static final int BRAND_LIGHT_THEME_GREEN = 0xFF00CC33;

    /**
     * Resolves the appropriate brand green color for the active theme.
     */
    public static int getBrandGreenColor(@NonNull Context context) {
        String activeTheme = XnetThemeManager.getTheme(context);
        if (XnetThemeManager.THEME_LIGHT.equals(activeTheme)) {
            return BRAND_LIGHT_THEME_GREEN;
        }
        return BRAND_NEON_GREEN;
    }

    /**
     * Applies signature brand formatting to any TextView.
     */
    public static void applyBrandName(@Nullable TextView textView, @Nullable String text) {
        if (textView == null || textView.getContext() == null || text == null) return;
        textView.setText(formatBrandText(textView.getContext(), text));
    }

    /**
     * Formats a CharSequence according to Xnet Hub branding rules across all 6 themes:
     *   - 'X' in "Xnet" / "XNET" : Signature Green (#00CC33 in Light theme, #00FF41 in others).
     *   - 'NET'                 : Primary theme text color (?attr/xnetTextPrimary).
     *   - For Colored Cyber themes (Green, Blue, Orange): Entire 'HUB' matches vibrant theme accent color.
     *   - For Normal Light/Dark & CyberBlack : 'H' in 'HUB' is ALSO Signature Green, 'UB' matches theme text.
     */
    @NonNull
    public static CharSequence formatBrandText(@NonNull Context context, @Nullable CharSequence rawText) {
        if (rawText == null || rawText.length() == 0) {
            return "";
        }
        String original = rawText.toString();
        String lower = original.toLowerCase();

        String activeTheme = XnetThemeManager.getTheme(context);
        boolean isColoredCyber = XnetThemeManager.THEME_CYBER_GREEN.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_BLUE.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_ORANGE.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_RGB.equals(activeTheme);

        boolean isCyberRGB = XnetThemeManager.THEME_CYBER_RGB.equals(activeTheme);
        int greenColor = getBrandGreenColor(context);
        int primaryText = isCyberRGB ? 0xFF00D4FF : resolveThemeColor(context, R.attr.xnetTextPrimary, 0xFFFFFFFF);
        int accentColor = isCyberRGB ? 0xFFFF6B00 : resolveThemeColor(context, R.attr.xnetAccentPrimary, 0xFF00FF41);

        SpannableStringBuilder builder = new SpannableStringBuilder(original);

        int index = 0;
        while ((index = lower.indexOf("xnet", index)) != -1) {
            int spanStart = index;
            // Force 'X' to uppercase
            builder.replace(index, index + 1, "X");

            // 1. 'X' -> Signature Green (#00FF41)
            builder.setSpan(new ForegroundColorSpan(greenColor), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 2. 'NET' -> Primary Theme Text Color (or Cyber Cyan #00D4FF in CyberRGB)
            builder.setSpan(new ForegroundColorSpan(primaryText), index + 1, index + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Check if followed by " hub" or " smart"
            int secondWordStart = index + 4;
            int spanEnd = index + 4;
            if (secondWordStart < lower.length() && lower.charAt(secondWordStart) == ' ') {
                secondWordStart++;
            }
            String secondWord = null;
            if (secondWordStart + 3 <= lower.length() && lower.substring(secondWordStart, secondWordStart + 3).equalsIgnoreCase("hub")) {
                secondWord = "hub";
                spanEnd = secondWordStart + 3;
            } else if (secondWordStart + 5 <= lower.length() && lower.substring(secondWordStart, secondWordStart + 5).equalsIgnoreCase("smart")) {
                secondWord = "smart";
                spanEnd = secondWordStart + 5;
            }
            
            if (secondWord != null) {
                if (isColoredCyber) {
                    // CyberGreen, CyberBlue, CyberOrange, CyberRGB: Entire second word gets vibrant accent color
                    builder.setSpan(new ForegroundColorSpan(accentColor), secondWordStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), secondWordStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    // Normal Light, Normal Dark & CyberBlack: First letter gets Signature Green, rest gets theme text color
                    String firstLetter = builder.subSequence(secondWordStart, secondWordStart + 1).toString().toUpperCase();
                    builder.replace(secondWordStart, secondWordStart + 1, firstLetter);
                    builder.setSpan(new ForegroundColorSpan(greenColor), secondWordStart, secondWordStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), secondWordStart, secondWordStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new ForegroundColorSpan(primaryText), secondWordStart + 1, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // Apply marker span so XnetBrandTextView can locate it for glitch/glitter effects
            boolean hasSecondWord = (spanEnd > index + 4);
            if (hasSecondWord) {
                // Only the second word ("Hub" or "Smart") glitches, matching xnethub.xyz design
                builder.setSpan(new XnetBrandMarkerSpan(), secondWordStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                // No second word: "Xnet" itself glitches
                builder.setSpan(new XnetBrandMarkerSpan(), spanStart, index + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            index += 4;
        }

        return builder;
    }

    /**
     * Marker span used to locate brand names in a TextView for applying animations.
     */
    public static class XnetBrandMarkerSpan { }

    /**
     * Applies a dual-tone color to the TextView.
     */
    public static void applyDualTone(TextView textView, String accentPart, String primaryPart) {
        if (textView == null || textView.getContext() == null) return;
        Context context = textView.getContext();

        int accentColor = resolveThemeColor(context, R.attr.xnetAccentPrimary, 0xFF00FF41);
        int primaryColor = resolveThemeColor(context, R.attr.xnetTextPrimary, 0xFFFFFFFF);

        String fullText = accentPart + primaryPart;
        SpannableString spannable = new SpannableString(fullText);

        spannable.setSpan(new ForegroundColorSpan(accentColor), 0, accentPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(primaryColor), accentPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
    }

    /**
     * Applies a dual-tone color to the TextView with custom colors.
     */
    public static void applyDualToneCustom(TextView textView, String firstPart, String secondPart, int firstColor, int secondColor) {
        if (textView == null) return;
        String fullText = firstPart + secondPart;
        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new ForegroundColorSpan(firstColor), 0, firstPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(secondColor), firstPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    private static int resolveThemeColor(Context context, int attrResId, int fallback) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return fallback;
    }
}
