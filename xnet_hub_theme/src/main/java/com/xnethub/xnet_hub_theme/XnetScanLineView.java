package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

/**
 * XnetScanLineView
 *
 * A custom View that draws an animated horizontal scan line sweeping from top to bottom.
 * Perfectly matches the "Scan" animation defined in the Hacker Theme Design System.
 *
 * - X-Cyber Themes : Neon glow scan line (accent color).
 * - Classic Themes : Subtle translucent white scan line.
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetScanLineView
 *       android:layout_width="match_parent"
 *       android:layout_height="120dp" />
 */
public class XnetScanLineView extends View {

    private final Paint linePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float scanProgress = 0f; // 0.0 → 1.0 (top → bottom)
    private int accentColor = Color.argb(200, 0, 255, 65);
    private ValueAnimator animator;

    private static final long SCAN_DURATION_MS = 2400L;

    public XnetScanLineView(@NonNull Context context) {
        this(context, null);
    }

    public XnetScanLineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetScanLineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Resolve accent color
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            accentColor = tv.data;
        }

        // Scan line paint
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics()));
        linePaint.setColor(accentColor);

        // Glow paint (wide translucent line below scan)
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 18f, getResources().getDisplayMetrics()));
        glowPaint.setColor(Color.argb(28,
            Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor)));

        startAnimation();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(SCAN_DURATION_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            scanProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0 || getHeight() == 0) return;

        float y = scanProgress * getHeight();
        float fadeIn  = Math.min(1f, scanProgress * 5f);         // quick fade-in at top
        float fadeOut = Math.min(1f, (1f - scanProgress) * 5f);  // quick fade-out at bottom
        float alpha   = Math.min(fadeIn, fadeOut);

        // Update line opacity based on fade
        int r = Color.red(accentColor);
        int g = Color.green(accentColor);
        int b = Color.blue(accentColor);

        linePaint.setColor(Color.argb((int)(alpha * 230), r, g, b));
        glowPaint.setColor(Color.argb((int)(alpha * 35), r, g, b));

        // Draw glow trail below scan line
        canvas.drawLine(0, y + 8, getWidth(), y + 8, glowPaint);
        // Draw sharp scan line
        canvas.drawLine(0, y, getWidth(), y, linePaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null && !animator.isRunning()) animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }
}
