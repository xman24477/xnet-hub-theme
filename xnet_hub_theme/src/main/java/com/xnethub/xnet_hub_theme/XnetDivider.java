package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * XnetDivider
 *
 * A horizontal divider line featuring a sci-fi cyber neon glow effect
 * with a center-out expanding dual-light pulse animation:
 *
 * Animation:
 *   A bright neon spark originates at the exact CENTER of the line,
 *   splits into TWO light beads expanding outwards to the left and right,
 *   and smoothly fades out as they reach the outer edges.
 */
public class XnetDivider extends View {

    private static final float GLOW_SHADOW_WIDTH_DP = 18f;
    private static final float MIN_HEIGHT_DP        = 6f;
    private static final long  ANIM_DURATION_MS     = 2600L;

    private Paint mLinePaint;
    private Paint mGlowPaint;
    private Paint mPulsePaint;
    private Paint mBeadGlowPaint;

    private int mAccentColor;
    private int mStrokeColor;

    private float mAnimProgress = 0f;
    private boolean mIsAnimated  = true;
    private ValueAnimator mAnimator;

    public XnetDivider(Context context) {
        super(context);
        init(context, null);
    }

    public XnetDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XnetDivider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mAccentColor = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mStrokeColor = resolveColor(context, R.attr.xnetStroke,        0x33FFFFFF);

        // Base gradient line paint
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(dpToPx(1f));

        // Base wide glow shadow
        mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGlowPaint.setStyle(Paint.Style.STROKE);
        mGlowPaint.setStrokeWidth(dpToPx(GLOW_SHADOW_WIDTH_DP));
        mGlowPaint.setColor(applyAlpha(mAccentColor, 0x15));

        // Traveling light pulse line paint
        mPulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPulsePaint.setStyle(Paint.Style.STROKE);
        mPulsePaint.setStrokeWidth(dpToPx(1.8f));

        // Traveling light bead glow spot paint
        mBeadGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBeadGlowPaint.setStyle(Paint.Style.FILL);

        setMinimumHeight((int) dpToPx(MIN_HEIGHT_DP));
        startPulseAnimator();
    }

    private void startPulseAnimator() {
        if (mAnimator != null) mAnimator.cancel();
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(ANIM_DURATION_MS);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(anim -> {
            mAnimProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        if (mIsAnimated) {
            mAnimator.start();
        }
    }

    public void setAnimated(boolean animated) {
        this.mIsAnimated = animated;
        if (animated) {
            if (mAnimator != null && !mAnimator.isRunning()) mAnimator.start();
        } else {
            if (mAnimator != null) mAnimator.cancel();
            mAnimProgress = 0f;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        float cy = height / 2f;
        float cx = width / 2f;

        // 1. Build & draw base horizontal gradient line:
        // transparent → accent (at 25%) → accent (at 75%) → transparent
        LinearGradient baseShader = new LinearGradient(
                0f, cy, width, cy,
                new int[]{
                        Color.TRANSPARENT,
                        applyAlpha(mAccentColor, 0xAA),
                        applyAlpha(mAccentColor, 0xAA),
                        Color.TRANSPARENT
                },
                new float[]{0f, 0.25f, 0.75f, 1f},
                Shader.TileMode.CLAMP
        );
        mLinePaint.setShader(baseShader);

        // Faint wide glow shadow
        float shadowY = cy + dpToPx(1f);
        canvas.drawLine(0f, shadowY, width, shadowY, mGlowPaint);

        // Base line
        canvas.drawLine(0f, cy, width, cy, mLinePaint);

        // 2. Draw animated CENTER-OUT EXPANDING DUAL LIGHT PULSE BEADS
        if (mIsAnimated && mAnimator != null && mAnimator.isRunning()) {
            float maxDistance = cx;
            float leftBeadX  = cx - (mAnimProgress * maxDistance);
            float rightBeadX = cx + (mAnimProgress * maxDistance);

            // Fade in at center (0% to 15%) and fade out near outer edges (85% to 100%)
            float fadeIn  = Math.min(1f, mAnimProgress / 0.15f);
            float fadeOut = Math.min(1f, (1f - mAnimProgress) / 0.15f);
            float alphaFactor = Math.min(fadeIn, fadeOut);

            if (alphaFactor > 0.02f) {
                float beadWidth = Math.max(dpToPx(24f), width * 0.18f);
                int coreColor   = blendColors(mAccentColor, Color.WHITE, 0.85f);
                int sideColor   = applyAlpha(mAccentColor, (int)(180 * alphaFactor));
                int transparent = Color.TRANSPARENT;
                float glowRadius = dpToPx(12f);

                // --- Draw Left Bead (moving from center → left) ---
                drawBead(canvas, leftBeadX, cy, beadWidth, glowRadius, coreColor, sideColor, transparent, alphaFactor);

                // --- Draw Right Bead (moving from center → right) ---
                drawBead(canvas, rightBeadX, cy, beadWidth, glowRadius, coreColor, sideColor, transparent, alphaFactor);
            }
        }
    }

    private void drawBead(Canvas canvas, float beadX, float cy, float beadWidth, float glowRadius,
                          int coreColor, int sideColor, int transparent, float alphaFactor) {
        float startX = beadX - beadWidth / 2f;
        float endX   = beadX + beadWidth / 2f;

        LinearGradient pulseShader = new LinearGradient(
                startX, cy, endX, cy,
                new int[]{
                        transparent,
                        sideColor,
                        applyAlpha(coreColor, (int)(255 * alphaFactor)),
                        sideColor,
                        transparent
                },
                new float[]{0f, 0.35f, 0.5f, 0.65f, 1f},
                Shader.TileMode.CLAMP
        );
        mPulsePaint.setShader(pulseShader);
        canvas.drawLine(Math.max(0, startX), cy, Math.min(getWidth(), endX), cy, mPulsePaint);

        RadialGradient beadGlowShader = new RadialGradient(
                beadX, cy, glowRadius,
                new int[]{
                        applyAlpha(mAccentColor, (int)(120 * alphaFactor)),
                        Color.TRANSPARENT
                },
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        mBeadGlowPaint.setShader(beadGlowShader);
        canvas.drawCircle(beadX, cy, glowRadius, mBeadGlowPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mIsAnimated && mAnimator != null && !mAnimator.isRunning()) {
            mAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    private static int applyAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Math.min(255, Math.max(0, alpha)) << 24);
    }

    private static int blendColors(int c1, int c2, float ratio) {
        float inv = 1f - ratio;
        int a = (int) (Color.alpha(c1) * inv + Color.alpha(c2) * ratio);
        int r = (int) (Color.red(c1)   * inv + Color.red(c2)   * ratio);
        int g = (int) (Color.green(c1) * inv + Color.green(c2) * ratio);
        int b = (int) (Color.blue(c1)  * inv + Color.blue(c2)  * ratio);
        return Color.argb(a, r, g, b);
    }

    private static int resolveColor(Context context, int attr, int fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return ta.getColor(0, fallback);
        } finally {
            ta.recycle();
        }
    }
}
