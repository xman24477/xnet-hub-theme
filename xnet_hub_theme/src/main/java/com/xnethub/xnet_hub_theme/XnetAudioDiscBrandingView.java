package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * XnetAudioDiscBrandingView
 *
 * Renders arc text on a CD disc:
 *   TOP arc    → App's own label (auto-read from PackageManager), UPPERCASE
 *   BOTTOM arc → "· XNET HUB ·" (library branding, always present)
 *
 * Refined Brand Formatting Rules across themes:
 *   - 'X' in "Xnet" / "XNET" : Signature Green (#00CC33 in Light theme, #00FF41 in others).
 *   - 'NET'                 : Primary theme text color (?attr/xnetTextPrimary).
 *   - Colored Cyber themes  : Entire 'HUB' matches vibrant theme accent color (?attr/xnetAccentPrimary).
 *   - Normal & CyberBlack   : 'H' in 'HUB' is ALSO Signature Green, 'UB' matches theme text color.
 *   - Divider dots          : Accent positive color.
 */
public final class XnetAudioDiscBrandingView extends View {

    private final Paint outerRingPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baseTextPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path topTextPath     = new Path();
    private final Path bottomTextPath  = new Path();
    private final RectF textRingBounds = new RectF();
    private final PathMeasure pathMeasure = new PathMeasure();

    /** Top arc: the host app's name (auto-resolved from PackageManager) */
    private String topText = "";
    /** Bottom arc: permanent library branding */
    private static final String BOTTOM_BRANDING = "· XNET HUB ·";

    private float centerX, centerY, outerRadius, dividerRadius;

    // Palette colors resolved per theme
    private boolean isColoredCyberTheme;
    private int colorGreen;
    private int colorPrimaryText;
    private int colorSecondaryText;
    private int colorAccentPrimary;
    private int colorAccentPositive;

    public XnetAudioDiscBrandingView(Context context) {
        super(context);
        init();
    }

    public XnetAudioDiscBrandingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XnetAudioDiscBrandingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        topText = resolveAppName();

        baseTextPaint.setStyle(Paint.Style.FILL);
        baseTextPaint.setTextSize(dp(10f));
        baseTextPaint.setSubpixelText(true);
        baseTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));

        outerRingPaint.setStyle(Paint.Style.STROKE);
        outerRingPaint.setStrokeWidth(dp(1f));

        dividerDotPaint.setStyle(Paint.Style.FILL);

        refreshPalette();
    }

    /** Reads the host app's human-readable name from PackageManager. */
    @NonNull
    private String resolveAppName() {
        try {
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(getContext().getPackageName(), 0);
            CharSequence label = pm.getApplicationLabel(info);
            if (!TextUtils.isEmpty(label)) {
                return label.toString().toUpperCase();
            }
        } catch (Exception ignored) {}
        return "XNET APP";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshPalette();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float size = Math.min(w, h);
        centerX = w / 2f;
        centerY = h / 2f;
        outerRadius   = (size / 2f) - dp(8f);
        dividerRadius = outerRadius - dp(18f);

        float textRadius = outerRadius - dp(18f);
        textRingBounds.set(
                centerX - textRadius, centerY - textRadius,
                centerX + textRadius, centerY + textRadius
        );

        topTextPath.reset();
        topTextPath.addArc(textRingBounds, 195f, 150f);

        bottomTextPath.reset();
        bottomTextPath.addArc(textRingBounds, 165f, -150f);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(centerX, centerY, outerRadius, outerRingPaint);

        drawDividerDots(canvas);
        drawBrandedTextOnPath(canvas, topText,         topTextPath,     dp(4.5f), false);
        drawBrandedTextOnPath(canvas, BOTTOM_BRANDING, bottomTextPath, -dp(2.0f), true);
    }

    private void drawDividerDots(@NonNull Canvas canvas) {
        float dotRadius = dp(2.2f);
        drawDot(canvas,   0f, dotRadius);
        drawDot(canvas, 180f, dotRadius);
    }

    private void drawDot(@NonNull Canvas canvas, float angleDeg, float dotRadius) {
        double rad = Math.toRadians(angleDeg);
        float x = centerX + (float)(Math.cos(rad) * dividerRadius);
        float y = centerY + (float)(Math.sin(rad) * dividerRadius);
        canvas.drawCircle(x, y, dotRadius, dividerDotPaint);
    }

    private void drawBrandedTextOnPath(
            @NonNull Canvas canvas, @Nullable String text,
            @NonNull Path path, float vOffset, boolean isBottomBranding) {
        if (TextUtils.isEmpty(text)) return;

        List<TextSegment> segments = parseSegments(text, isBottomBranding);

        pathMeasure.setPath(path, false);
        float pathLength = pathMeasure.getLength();

        float totalWidth = 0f;
        for (TextSegment seg : segments) {
            totalWidth += seg.paint.measureText(seg.text);
        }

        float currentHOffset = Math.max(0f, (pathLength - totalWidth) / 2f);

        for (TextSegment seg : segments) {
            canvas.drawTextOnPath(seg.text, path, currentHOffset, vOffset, seg.paint);
            currentHOffset += seg.paint.measureText(seg.text);
        }
    }

    private List<TextSegment> parseSegments(String text, boolean isBottomBranding) {
        List<TextSegment> list = new ArrayList<>();
        if (text == null || text.length() == 0) return list;

        int idx = 0;
        int len = text.length();

        while (idx < len) {
            char c = text.charAt(idx);

            // Handle dot symbol '·'
            if (c == '·') {
                Paint dotPaint = new Paint(baseTextPaint);
                dotPaint.setColor(colorAccentPositive);
                list.add(new TextSegment("·", dotPaint));
                idx++;
                continue;
            }

            // Check if string matches "XNET" starting at idx
            if (idx + 4 <= len && text.substring(idx, idx + 4).equalsIgnoreCase("XNET")) {
                // 1. "X" -> Signature Green (#00CC33 in Light, #00FF41 in others)
                Paint xPaint = new Paint(baseTextPaint);
                xPaint.setColor(colorGreen);
                list.add(new TextSegment("X", xPaint));

                // 2. "NET" -> Primary theme text color
                Paint netPaint = new Paint(baseTextPaint);
                netPaint.setColor(colorPrimaryText);
                list.add(new TextSegment("NET", netPaint));

                idx += 4;
                continue;
            }

            // Check if string matches "HUB" starting at idx
            if (idx + 3 <= len && text.substring(idx, idx + 3).equalsIgnoreCase("HUB")) {
                if (isColoredCyberTheme) {
                    // CyberGreen, CyberBlue, CyberOrange: Entire HUB gets vibrant theme accent color
                    Paint hubPaint = new Paint(baseTextPaint);
                    hubPaint.setColor(colorAccentPrimary);
                    list.add(new TextSegment("HUB", hubPaint));
                } else {
                    // Normal Light/Dark & CyberBlack: 'H' gets Signature Green, 'UB' gets theme text color
                    Paint hPaint = new Paint(baseTextPaint);
                    hPaint.setColor(colorGreen);
                    list.add(new TextSegment("H", hPaint));

                    Paint ubPaint = new Paint(baseTextPaint);
                    ubPaint.setColor(colorPrimaryText);
                    list.add(new TextSegment("UB", ubPaint));
                }
                idx += 3;
                continue;
            }

            // Regular text segment
            Paint defaultPaint = new Paint(baseTextPaint);
            defaultPaint.setColor(isBottomBranding ? colorSecondaryText : colorAccentPrimary);
            list.add(new TextSegment(String.valueOf(c), defaultPaint));
            idx++;
        }

        return list;
    }

    private static class TextSegment {
        final String text;
        final Paint paint;

        TextSegment(String text, Paint paint) {
            this.text = text;
            this.paint = paint;
        }
    }

    /** Force-set the top arc label. Pass null to revert to the auto-resolved app name. */
    public void setTopText(@Nullable String text) {
        topText = (text != null) ? text.toUpperCase() : resolveAppName();
        invalidate();
    }

    /** Re-apply all theme colours after a theme switch. */
    public void refreshPalette() {
        String activeTheme = XnetThemeManager.getTheme(getContext());
        isColoredCyberTheme = XnetThemeManager.THEME_CYBER_GREEN.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_BLUE.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_ORANGE.equals(activeTheme)
                || XnetThemeManager.THEME_CYBER_RGB.equals(activeTheme);

        colorGreen          = XnetTextFormatter.getBrandGreenColor(getContext());
        colorPrimaryText    = withAlpha(R.attr.xnetTextPrimary, 235);
        colorSecondaryText  = withAlpha(R.attr.xnetTextSecondary, 200);
        colorAccentPrimary  = withAlpha(R.attr.xnetAccentPrimary, 240);
        colorAccentPositive = withAlpha(R.attr.xnetAccentPositive, 242);

        outerRingPaint.setColor(withAlpha(R.attr.xnetStroke, 206));
        dividerDotPaint.setColor(colorAccentPositive);

        invalidate();
    }

    private int withAlpha(@AttrRes int attrRes, int alpha) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attrRes, tv, true)) {
            return ColorUtils.setAlphaComponent(tv.data, alpha);
        }
        return 0;
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }
}
