package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Locale;

/**
 * XnetRadarView
 *
 * Tactical Sci-Fi radar scanning component engineered for the Xnet brand ecosystem.
 * Features:
 *  - Continuous 360° sweeping beam with smooth gradient falloff.
 *  - Micro-oscillating outer calibrated scale ring (খাস কাটা স্কেল) with 120 precision notches.
 *  - Fully dynamic range & distance partitioning (min, max, ring count, unit).
 *  - Automatic active theme font detection (Rajdhani, Orbitron, Share Tech Mono, or system font).
 *  - Frosted glass translucent backdrop disc adhering to Xnet surface depth tokens.
 *  - Memory-safe lifecycle management (auto-starts on attach, cleans up on detach).
 *  - Seamlessly respects XnetThemeManager animation settings.
 */
public class XnetRadarView extends View {

    // Default configuration values
    public static final long DEFAULT_SWEEP_DURATION_MS = 2500L;
    public static final long DEFAULT_OSCILLATE_DURATION_MS = 3800L;
    public static final float DEFAULT_MIN_RANGE = 0f;
    public static final float DEFAULT_MAX_RANGE = 100f;
    public static final int DEFAULT_RING_COUNT = 4;
    public static final String DEFAULT_RANGE_UNIT = "m";

    // Paints
    private Paint circlePaint;
    private Paint thickCirclePaint;
    private Paint crosshairPaint;
    private Paint sweepPaint;
    private Paint sweepBeamPaint;
    private Paint tickPaint;

    // Outer scale layer paints
    private Paint outerTrackPaint;
    private Paint outerScaleMajorPaint;
    private Paint outerScaleMinorPaint;

    // Translucent frosted glass backdrop disc paint
    private Paint backdropPaint;

    // Degree markings and tactical range text paints
    private Paint degreeTextPaint;
    private Paint rangeTextPaint;

    // Animation angles
    private int sweepAngle = 0;             // 0..360 continuous clockwise rotation
    private float outerScaleAngle = 0f;     // Subtle anti-clockwise <-> clockwise oscillation (-14° to +14°)

    // Animators
    private ValueAnimator sweepAnimator;
    private ValueAnimator scaleAnimator;

    // Configurable parameters
    private long sweepDurationMs = DEFAULT_SWEEP_DURATION_MS;
    private long oscillateDurationMs = DEFAULT_OSCILLATE_DURATION_MS;
    private boolean oscillateEnabled = true;
    private boolean autoStart = true;
    private boolean showDegrees = true;
    private boolean showRanges = true;
    private boolean frostedBackdropEnabled = true;

    // Dynamic range partition configuration
    private float minRange = DEFAULT_MIN_RANGE;
    private float maxRange = DEFAULT_MAX_RANGE;
    private int ringCount = DEFAULT_RING_COUNT;
    private String rangeUnit = DEFAULT_RANGE_UNIT;

    // Dynamic theme colors
    @ColorInt private int primaryColor;
    @ColorInt private int lightShadeColor;
    @ColorInt private int transparentColor;
    @ColorInt private int backdropBaseColor;
    private Integer customAccentColor = null;

    // Typeface
    private Typeface customTypeface = null;

    public XnetRadarView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public XnetRadarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XnetRadarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XnetRadarView);
            try {
                sweepDurationMs = a.getInt(R.styleable.XnetRadarView_xnetRadarSweepDuration, (int) DEFAULT_SWEEP_DURATION_MS);
                oscillateDurationMs = a.getInt(R.styleable.XnetRadarView_xnetRadarOscillateDuration, (int) DEFAULT_OSCILLATE_DURATION_MS);
                oscillateEnabled = a.getBoolean(R.styleable.XnetRadarView_xnetRadarOscillateEnabled, true);
                minRange = a.getFloat(R.styleable.XnetRadarView_xnetRadarMinRange, DEFAULT_MIN_RANGE);
                maxRange = a.getFloat(R.styleable.XnetRadarView_xnetRadarMaxRange, DEFAULT_MAX_RANGE);
                ringCount = Math.max(1, a.getInt(R.styleable.XnetRadarView_xnetRadarRingCount, DEFAULT_RING_COUNT));
                String unit = a.getString(R.styleable.XnetRadarView_xnetRadarRangeUnit);
                if (unit != null) {
                    rangeUnit = unit;
                }
                showDegrees = a.getBoolean(R.styleable.XnetRadarView_xnetRadarShowDegrees, true);
                showRanges = a.getBoolean(R.styleable.XnetRadarView_xnetRadarShowRanges, true);
                frostedBackdropEnabled = a.getBoolean(R.styleable.XnetRadarView_xnetRadarFrostedBackdrop, true);
                autoStart = a.getBoolean(R.styleable.XnetRadarView_xnetRadarAutoStart, true);

                if (a.hasValue(R.styleable.XnetRadarView_xnetRadarAccentColor)) {
                    customAccentColor = a.getColor(R.styleable.XnetRadarView_xnetRadarAccentColor, 0);
                }

                // Check for custom font attribute in XML
                int fontResId = a.getResourceId(R.styleable.XnetRadarView_android_fontFamily, 0);
                if (fontResId == 0) {
                    fontResId = a.getResourceId(R.styleable.XnetRadarView_fontFamily, 0);
                }
                if (fontResId != 0) {
                    try {
                        customTypeface = ResourcesCompat.getFont(context, fontResId);
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                a.recycle();
            }
        }

        resolveThemeColors();
        initPaints();
    }

    /**
     * Resolves colors dynamically from the active Xnet Hub Theme.
     */
    public void resolveThemeColors() {
        if (customAccentColor != null) {
            primaryColor = customAccentColor;
        } else {
            TypedValue tvAccent = new TypedValue();
            if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tvAccent, true)) {
                primaryColor = tvAccent.data;
            } else {
                primaryColor = Color.parseColor("#00FF41"); // Signature neon green fallback
            }
        }

        // Technically shift to a lighter, softer harmonious shade for the radar sweep shadow:
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(primaryColor, hsl);
        hsl[2] = Math.min(1.0f, hsl[2] * 0.65f + 0.35f); // Lighter shade of theme color
        hsl[1] = Math.max(0.0f, hsl[1] * 0.88f);         // Softer, refined saturation
        lightShadeColor = ColorUtils.HSLToColor(hsl);

        transparentColor = Color.argb(0, Color.red(lightShadeColor), Color.green(lightShadeColor), Color.blue(lightShadeColor));

        TypedValue tvBg = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetBackgroundDeep, tvBg, true)) {
            backdropBaseColor = tvBg.data;
        } else if (getContext().getTheme().resolveAttribute(R.attr.xnetBackground, tvBg, true)) {
            backdropBaseColor = tvBg.data;
        } else {
            backdropBaseColor = Color.parseColor("#060A10");
        }
    }

    /**
     * Automatically resolves the active typography from the Xnet Theme and system configuration.
     * Prioritizes:
     *  1. Explicitly supplied custom Typeface.
     *  2. XnetThemeManager active font setting (Rajdhani, Orbitron, Share Tech Mono).
     *  3. Theme's declared fontFamily attribute.
     *  4. Cyber theme fallback font (Share Tech Mono / Rajdhani).
     */
    @NonNull
    public Typeface resolveThemeTypeface() {
        if (customTypeface != null) {
            return customTypeface;
        }

        Context context = getContext();

        // 1. Check font declared via XnetThemeManager
        try {
            String fontPref = XnetThemeManager.getFont(context);
            if (fontPref != null && !XnetThemeManager.FONT_DEFAULT.equals(fontPref)) {
                if (fontPref.toLowerCase(Locale.US).contains("share_tech_mono")) {
                    Typeface tf = ResourcesCompat.getFont(context, R.font.share_tech_mono);
                    if (tf != null) return tf;
                } else if (fontPref.toLowerCase(Locale.US).contains("rajdhani")) {
                    Typeface tf = ResourcesCompat.getFont(context, R.font.rajdhani);
                    if (tf != null) return tf;
                } else if (fontPref.toLowerCase(Locale.US).contains("orbitron")) {
                    Typeface tf = ResourcesCompat.getFont(context, R.font.orbitron);
                    if (tf != null) return tf;
                }
            }
        } catch (Exception ignored) {
        }

        // 2. Check theme's fontFamily attribute
        TypedValue tvFont = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.fontFamily, tvFont, true) && tvFont.resourceId != 0) {
            try {
                Typeface tf = ResourcesCompat.getFont(context, tvFont.resourceId);
                if (tf != null) return tf;
            } catch (Exception ignored) {
            }
        }

        // 3. If cyber theme, prefer Share Tech Mono for tactical HUD aesthetics
        TypedValue tvCyber = new TypedValue();
        boolean isCyberTheme = false;
        if (context.getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tvCyber, true)) {
            isCyberTheme = (tvCyber.data != 0);
        }

        if (isCyberTheme) {
            try {
                Typeface tf = ResourcesCompat.getFont(context, R.font.share_tech_mono);
                if (tf != null) return tf;
            } catch (Exception ignored) {
            }
        }

        return Typeface.DEFAULT_BOLD;
    }

    private void initPaints() {
        Typeface tf = resolveThemeTypeface();

        // Translucent frosted glass backdrop disc behind main radar
        backdropPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backdropPaint.setStyle(Paint.Style.FILL);

        // Inner concentric circles (subtle, clean)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_circle_stroke_width, 1.2f));
        circlePaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 75));

        // Main radar outer border circle
        thickCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thickCirclePaint.setStyle(Paint.Style.STROKE);
        thickCirclePaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_thick_circle_stroke_width, 3f));
        thickCirclePaint.setColor(primaryColor);

        // Tick marks on main circle
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_main_tick_minor, 1.8f));
        tickPaint.setColor(primaryColor);

        // Crosshairs
        crosshairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_crosshair_stroke_width, 1.2f));
        crosshairPaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 110));

        // Outer scale ring track (concentric thin border)
        outerTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerTrackPaint.setStyle(Paint.Style.STROKE);
        outerTrackPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_outer_track_stroke_width, 1.2f));
        outerTrackPaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 130));

        // Outer scale major notches (calibrated notched ticks)
        outerScaleMajorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerScaleMajorPaint.setStyle(Paint.Style.STROKE);
        outerScaleMajorPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_circle_stroke_width, 1.8f));
        outerScaleMajorPaint.setColor(primaryColor);

        // Outer scale minor notches
        outerScaleMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerScaleMinorPaint.setStyle(Paint.Style.STROKE);
        outerScaleMinorPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_circle_stroke_width, 1.0f));
        outerScaleMinorPaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 170));

        // Sweeping radar fan (gradient shadow) with balanced transparency
        sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sweepPaint.setStyle(Paint.Style.FILL);

        // Leading edge radar stick/beam line
        sweepBeamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sweepBeamPaint.setStyle(Paint.Style.STROKE);
        sweepBeamPaint.setStrokeWidth(getDimen(R.dimen.xnet_radar_beam_stroke_width, 2f));
        sweepBeamPaint.setColor(ColorUtils.setAlphaComponent(lightShadeColor, 190));

        // Degree markings (000°, 030°, 060°, ..., 330°)
        degreeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        degreeTextPaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 175));
        degreeTextPaint.setTextSize(getDimen(R.dimen.xnet_radar_degree_text_size, sp(8.5f)));
        degreeTextPaint.setTextAlign(Paint.Align.CENTER);
        degreeTextPaint.setTypeface(tf);

        // Range rings text (dynamic partitions)
        rangeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rangeTextPaint.setColor(ColorUtils.setAlphaComponent(primaryColor, 130));
        rangeTextPaint.setTextSize(getDimen(R.dimen.xnet_radar_range_text_size, sp(7f)));
        rangeTextPaint.setTextAlign(Paint.Align.LEFT);
        rangeTextPaint.setTypeface(tf);
    }

    private float getDimen(int resId, float fallbackDp) {
        try {
            return getResources().getDimension(resId);
        } catch (Exception ignored) {
            return dp(fallbackDp);
        }
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    public void applyTheme() {
        resolveThemeColors();
        initPaints();
        updateGradient(getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyTheme();
        if (autoStart) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != VISIBLE) {
            stopAnimation();
        } else if (autoStart) {
            startAnimation();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradient(w, h);
    }

    private void updateGradient(int w, int h) {
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float maxRadius = Math.min(cx, cy);
        float outerScaleOffset = getDimen(R.dimen.xnet_radar_outer_scale_offset, 8f);
        float mainCircleOffset = getDimen(R.dimen.xnet_radar_main_circle_offset, 16f);
        float outerScaleRadius = Math.max(1f, maxRadius - outerScaleOffset);
        float mainRadius = Math.max(1f, outerScaleRadius - mainCircleOffset);

        // 1. Translucent frosted glass backdrop disc shader (Layer 0)
        int frostedTint = ColorUtils.blendARGB(backdropBaseColor, primaryColor, 0.08f);
        RadialGradient backdropShader = new RadialGradient(
                cx, cy, mainRadius,
                new int[]{
                        ColorUtils.setAlphaComponent(frostedTint, 205),
                        ColorUtils.setAlphaComponent(frostedTint, 222),
                        ColorUtils.setAlphaComponent(backdropBaseColor, 240)
                },
                new float[]{0.0f, 0.70f, 1.0f},
                Shader.TileMode.CLAMP
        );
        backdropPaint.setShader(backdropShader);

        // 2. Gradient for the radar sweep with elevated transparency and harmonic tone:
        SweepGradient gradient = new SweepGradient(cx, cy,
                new int[]{
                        transparentColor,
                        ColorUtils.setAlphaComponent(lightShadeColor, 8),
                        ColorUtils.setAlphaComponent(lightShadeColor, 35),
                        ColorUtils.setAlphaComponent(lightShadeColor, 85),
                        ColorUtils.setAlphaComponent(lightShadeColor, 140)
                },
                new float[]{0.0f, 0.78f, 0.93f, 0.99f, 1.0f});
        sweepPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        float maxRadius = Math.min(cx, cy);
        if (maxRadius <= 0) return;

        // Radius budget: outer scale dial layer + main radar circle
        float outerScaleOffset = getDimen(R.dimen.xnet_radar_outer_scale_offset, 8f);
        float mainCircleOffset = getDimen(R.dimen.xnet_radar_main_circle_offset, 16f);
        float outerScaleRadius = maxRadius - outerScaleOffset;
        float mainRadius = outerScaleRadius - mainCircleOffset;

        // ========================================================
        // LAYER 1: OUTER SCALE LAYER (খাস কাটা স্কেল)
        // Rotates subtly anti-clockwise <-> clockwise
        // ========================================================
        canvas.save();
        canvas.rotate(outerScaleAngle, cx, cy);

        // Outer circular track
        canvas.drawCircle(cx, cy, outerScaleRadius, outerTrackPaint);

        // Calibrated scale notches (120 notches around 360 degrees = every 3 degrees)
        float majorNotchLen = getDimen(R.dimen.xnet_radar_outer_scale_major_notch, 9f);
        float medNotchLen = getDimen(R.dimen.xnet_radar_outer_scale_medium_notch, 6f);
        float minorNotchLen = getDimen(R.dimen.xnet_radar_outer_scale_minor_notch, 3.5f);

        for (int i = 0; i < 120; i++) {
            boolean isMajor = (i % 10 == 0);   // Every 30 degrees
            boolean isMedium = (i % 5 == 0);  // Every 15 degrees
            float notchLen = isMajor ? majorNotchLen : (isMedium ? medNotchLen : minorNotchLen);
            Paint notchPaint = isMajor ? outerScaleMajorPaint : outerScaleMinorPaint;

            // Inward notch from outerScaleRadius towards main circle
            canvas.drawLine(cx, cy - outerScaleRadius, cx, cy - outerScaleRadius + notchLen, notchPaint);
            canvas.rotate(3f, cx, cy);
        }
        canvas.restore();

        // ========================================================
        // LAYER 0: TRANSLUCENT FROSTED GLASS BACKDROP DISC
        // ========================================================
        if (frostedBackdropEnabled) {
            canvas.drawCircle(cx, cy, mainRadius, backdropPaint);
        }

        // ========================================================
        // LAYER 2: MAIN RADAR CORE
        // Thick border circle, concentric rings, ticks, crosshairs
        // ========================================================

        // 2a. Outer thick circle of main radar
        canvas.drawCircle(cx, cy, mainRadius, thickCirclePaint);

        // 2b. Outer tick marks on main circle (72 ticks = every 5 degrees)
        float tickMajor = getDimen(R.dimen.xnet_radar_main_tick_major, 7f);
        float tickMinor = getDimen(R.dimen.xnet_radar_main_tick_minor, 3.5f);
        canvas.save();
        for (int i = 0; i < 72; i++) {
            float tickLength = (i % 6 == 0) ? tickMajor : tickMinor;
            canvas.drawLine(cx, cy - mainRadius, cx, cy - mainRadius + tickLength, tickPaint);
            canvas.rotate(5f, cx, cy);
        }
        canvas.restore();

        // 2c. Dynamic concentric rings & range indicators
        int effectiveRings = Math.max(1, ringCount);
        float rangeSpan = maxRange - minRange;
        float rangePad = getDimen(R.dimen.xnet_radar_range_padding, 5f);

        for (int i = 1; i <= effectiveRings; i++) {
            float ringFraction = (float) i / (float) effectiveRings;
            float r = mainRadius * ringFraction;

            // Concentric circle
            canvas.drawCircle(cx, cy, r, circlePaint);

            // Dynamic Range indicators on the inner concentric rings
            if (showRanges) {
                float rangeVal = minRange + (rangeSpan * ringFraction);
                String label = formatRangeValue(rangeVal) + rangeUnit;
                float ry = cy - r - dp(2);
                canvas.drawText(label, cx + rangePad, ry, rangeTextPaint);
            }
        }

        // 2d. Crosshairs
        canvas.drawLine(cx, cy - mainRadius, cx, cy + mainRadius, crosshairPaint);
        canvas.drawLine(cx - mainRadius, cy, cx + mainRadius, cy, crosshairPaint);

        // Minor tick marks on crosshairs corresponding to each ring partition
        float crossTick = getDimen(R.dimen.xnet_radar_cross_tick_size, 3.5f);
        for (int i = 1; i <= effectiveRings; i++) {
            float r = mainRadius * ((float) i / (float) effectiveRings);
            canvas.drawLine(cx - crossTick, cy - r, cx + crossTick, cy - r, crosshairPaint);
            canvas.drawLine(cx - crossTick, cy + r, cx + crossTick, cy + r, crosshairPaint);
            canvas.drawLine(cx - r, cy - crossTick, cx - r, cy + crossTick, crosshairPaint);
            canvas.drawLine(cx + r, cy - crossTick, cx + r, cy + crossTick, crosshairPaint);
        }

        // 2e. Degree markings around the main circle (12 positions: 000°, 030°, 060°, ..., 330°)
        if (showDegrees) {
            float degreeMargin = getDimen(R.dimen.xnet_radar_degree_margin, 14f);
            float degreeRadius = mainRadius - degreeMargin;
            Paint.FontMetrics fm = degreeTextPaint.getFontMetrics();
            float yCenterOffset = -(fm.ascent + fm.descent) / 2f;

            for (int deg = 0; deg < 360; deg += 30) {
                double rad = Math.toRadians(deg);
                float tx = cx + (float) (degreeRadius * Math.sin(rad));
                float ty = cy - (float) (degreeRadius * Math.cos(rad));
                String degStr = String.format(Locale.US, "%03d°", deg);
                canvas.drawText(degStr, tx, ty + yCenterOffset, degreeTextPaint);
            }
        }

        // ========================================================
        // LAYER 3: SWEEPING RADAR BEAM & GRADIENT FAN
        // Smooth continuous 360 clockwise rotation
        // ========================================================
        canvas.save();
        canvas.rotate(sweepAngle - 90, cx, cy);
        // Translucent gradient fan
        canvas.drawCircle(cx, cy, mainRadius, sweepPaint);
        // Crisp leading edge beam line
        canvas.drawLine(cx, cy, cx + mainRadius, cy, sweepBeamPaint);
        canvas.restore();
    }

    /**
     * Nicely formats distance numbers (e.g. 25, 50, 100 or 12.5) without ugly trailing zeroes.
     */
    private String formatRangeValue(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001f) {
            return String.valueOf(Math.round(value));
        } else {
            return String.format(Locale.US, "%.1f", value);
        }
    }

    // ========================================================
    // Public API Methods
    // ========================================================

    /**
     * Sets the distance range with automatic intermediate partitioning.
     *
     * @param min Minimum range boundary (e.g. 0)
     * @param max Maximum range boundary (e.g. 500)
     */
    public void setRange(float min, float max) {
        this.minRange = min;
        this.maxRange = max;
        invalidate();
    }

    /**
     * Sets full range configuration including divisions and measurement unit.
     *
     * @param min Minimum range (e.g. 0)
     * @param max Maximum range (e.g. 1000)
     * @param ringDivisions How many concentric rings / partitions to divide the space into
     * @param unit Distance unit suffix (e.g. "m", "km", "mi", "ft")
     */
    public void setRange(float min, float max, int ringDivisions, @NonNull String unit) {
        this.minRange = min;
        this.maxRange = max;
        this.ringCount = Math.max(1, ringDivisions);
        this.rangeUnit = unit != null ? unit : "";
        invalidate();
    }

    public void setMinRange(float min) {
        this.minRange = min;
        invalidate();
    }

    public void setMaxRange(float max) {
        this.maxRange = max;
        invalidate();
    }

    public void setRingCount(int ringCount) {
        this.ringCount = Math.max(1, ringCount);
        invalidate();
    }

    public void setRangeUnit(@NonNull String unit) {
        this.rangeUnit = unit != null ? unit : "";
        invalidate();
    }

    public float getMinRange() {
        return minRange;
    }

    public float getMaxRange() {
        return maxRange;
    }

    public int getRingCount() {
        return ringCount;
    }

    public String getRangeUnit() {
        return rangeUnit;
    }

    public void setShowDegrees(boolean showDegrees) {
        this.showDegrees = showDegrees;
        invalidate();
    }

    public void setShowRanges(boolean showRanges) {
        this.showRanges = showRanges;
        invalidate();
    }

    public void setFrostedBackdropEnabled(boolean enabled) {
        this.frostedBackdropEnabled = enabled;
        invalidate();
    }

    public void setAccentColor(@ColorInt int color) {
        this.customAccentColor = color;
        applyTheme();
    }

    public void setTypeface(@Nullable Typeface typeface) {
        this.customTypeface = typeface;
        initPaints();
        invalidate();
    }

    public void setSweepDuration(long durationMs) {
        this.sweepDurationMs = durationMs;
        if (sweepAnimator != null) {
            sweepAnimator.setDuration(sweepDurationMs);
        }
    }

    public void setOscillateDuration(long durationMs) {
        this.oscillateDurationMs = durationMs;
        if (scaleAnimator != null) {
            scaleAnimator.setDuration(oscillateDurationMs);
        }
    }

    public void setOscillateEnabled(boolean enabled) {
        this.oscillateEnabled = enabled;
        if (!enabled && scaleAnimator != null) {
            scaleAnimator.cancel();
            outerScaleAngle = 0f;
            invalidate();
        } else if (enabled && isAnimationRunning()) {
            startScaleAnimation();
        }
    }

    public boolean isAnimationRunning() {
        return (sweepAnimator != null && sweepAnimator.isRunning());
    }

    public void startAnimation() {
        // 1. Continuous 360 degree sweep animator
        if (sweepAnimator == null) {
            sweepAnimator = ValueAnimator.ofInt(0, 360);
            sweepAnimator.setDuration(sweepDurationMs);
            sweepAnimator.setInterpolator(new LinearInterpolator());
            sweepAnimator.setRepeatCount(ValueAnimator.INFINITE);
            sweepAnimator.addUpdateListener(animation -> {
                sweepAngle = (int) animation.getAnimatedValue();
                invalidate();
            });
        }
        if (!sweepAnimator.isRunning()) {
            sweepAnimator.start();
        }

        // 2. Gentle outer scale oscillation
        if (oscillateEnabled) {
            startScaleAnimation();
        }
    }

    private void startScaleAnimation() {
        if (scaleAnimator == null) {
            // Oscillates between -14 degrees (anti-clockwise) and +14 degrees (clockwise)
            scaleAnimator = ValueAnimator.ofFloat(-14f, 14f);
            scaleAnimator.setDuration(oscillateDurationMs);
            scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
            scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scaleAnimator.addUpdateListener(animation -> {
                outerScaleAngle = (float) animation.getAnimatedValue();
                invalidate();
            });
        }
        if (!scaleAnimator.isRunning()) {
            scaleAnimator.start();
        }
    }

    public void stopAnimation() {
        if (sweepAnimator != null && sweepAnimator.isRunning()) {
            sweepAnimator.cancel();
        }
        if (scaleAnimator != null && scaleAnimator.isRunning()) {
            scaleAnimator.cancel();
        }
    }
}
