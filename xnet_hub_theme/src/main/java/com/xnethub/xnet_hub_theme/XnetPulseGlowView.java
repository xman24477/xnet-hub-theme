package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * XnetPulseGlowView
 *
 * Draws 3 concentric expanding rings that radiate from the center and fade out —
 * exactly like the pulse/glow branding effect on xnethub.xyz.
 *
 * No solid center dot. Only pure ripple rings.
 * Each ring is offset in time so they appear staggered.
 *
 * - X-Cyber Themes : Neon accent color rings.
 * - Classic Themes : Subtle white/grey rings.
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetPulseGlowView
 *       android:layout_width="80dp"
 *       android:layout_height="80dp" />
 */
public class XnetPulseGlowView extends View {

    private static final int   RING_COUNT       = 3;
    private static final long  PULSE_DURATION   = 2200L;
    private static final float RING_OFFSET      = 1f / RING_COUNT; // 0.33 stagger per ring

    private final Paint[] ringPaints = new Paint[RING_COUNT];
    private final float[] ringFractions = new float[RING_COUNT]; // 0..1 each

    private int accentColor = Color.argb(200, 0, 255, 65);
    private int r, g, b;

    private ValueAnimator pulseAnimator;

    public XnetPulseGlowView(@NonNull Context context) {
        this(context, null);
    }

    public XnetPulseGlowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetPulseGlowView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Resolve accent color
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            accentColor = tv.data;
        }
        r = Color.red(accentColor);
        g = Color.green(accentColor);
        b = Color.blue(accentColor);

        float strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1.2f, getResources().getDisplayMetrics());

        // Create one Paint per ring
        for (int i = 0; i < RING_COUNT; i++) {
            ringPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            ringPaints[i].setStyle(Paint.Style.STROKE);
            ringPaints[i].setStrokeWidth(strokeWidth);
            ringPaints[i].setColor(Color.argb(0, r, g, b));

            // Pre-set staggered starting fractions so rings are evenly spread
            ringFractions[i] = (i * RING_OFFSET) % 1f;
        }

        startPulseAnimation();
    }

    private void startPulseAnimation() {
        // Single global time counter 0→1, looping
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(PULSE_DURATION);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(anim -> {
            float globalFraction = (float) anim.getAnimatedValue();
            // Advance each ring with its own offset
            for (int i = 0; i < RING_COUNT; i++) {
                ringFractions[i] = (globalFraction + (i * RING_OFFSET)) % 1f;
            }
            invalidate();
        });
        pulseAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float maxRadius = Math.min(getWidth(), getHeight()) / 2f - 4f;

        for (int i = 0; i < RING_COUNT; i++) {
            float fraction = ringFractions[i];

            // Ring grows from 0 → maxRadius
            float radius = fraction * maxRadius;

            // Alpha: full at start → 0 at end (linear fade-out)
            // Also add a quick fade-in for the first 15% to avoid harsh pop
            float fadeIn  = Math.min(1f, fraction / 0.15f);
            float fadeOut = 1f - fraction;
            int alpha = (int) (Math.min(fadeIn, fadeOut) * 180);

            ringPaints[i].setColor(Color.argb(alpha, r, g, b));
            canvas.drawCircle(cx, cy, Math.max(1f, radius), ringPaints[i]);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (pulseAnimator != null && !pulseAnimator.isRunning()) pulseAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }
}
