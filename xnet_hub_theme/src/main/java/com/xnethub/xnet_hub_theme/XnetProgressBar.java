package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * XnetProgressBar
 *
 * A horizontal themed progress bar featuring:
 *   - Cyber theme: Symmetrical 6-sided HEXAGON bar shape (both left & right ends pointy)
 *   - Classic theme: Rounded rect bar shape
 *   - Clear track border & background so 100% target boundary is always visible
 *   - Dynamic Split-Color Dual-Pass Percentage Text Overlay:
 *     The text color automatically adapts pixel-by-pixel as the progress bar fills:
 *     - Text over unfilled track → White / Light color
 *     - Text over filled accent bar → Automatically calculated high-contrast color
 *       (e.g., Deep Black on bright Neon Green/Yellow/Cyan, White on dark fill)
 *   - Smooth animation support with animateProgressTo() or startContinuousDemo()
 */
public class XnetProgressBar extends View {

    private static final float MIN_HEIGHT_DP   = 18f;
    private static final float CORNER_RADIUS   = 4f;   // dp – classic theme
    private static final float CAP_WIDTH_DP    = 4f;   // bright end-cap width

    // Progress state
    private float mProgress = 0f;
    private int   mMax      = 100;
    private boolean mShowText = true;

    // Theme
    private boolean mIsCyberTheme;
    private int     mAccentColor;
    private int     mSurfaceAltColor;
    private int     mBorderColor;

    // Paints
    private Paint mTrackPaint;
    private Paint mBorderPaint;
    private Paint mFillPaint;
    private Paint mCapPaint;
    private Paint mTextPaint;
    private Paint mTextOutlinePaint;

    // Animator
    private ValueAnimator mAnimator;

    public XnetProgressBar(Context context) {
        this(context, null);
    }

    public XnetProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XnetProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mAccentColor     = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mSurfaceAltColor = resolveColor(context, R.attr.xnetSurfaceAlt,    0xFF1A1A2E);
        mIsCyberTheme    = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);

        // Unfilled track fill & border
        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackPaint.setStyle(Paint.Style.FILL);
        mTrackPaint.setColor(Color.argb(230, 16, 22, 18));

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(dpToPx(1f));
        mBorderColor = Color.argb(140, Color.red(mAccentColor), Color.green(mAccentColor), Color.blue(mAccentColor));
        mBorderPaint.setColor(mBorderColor);

        // Progress fill
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(mAccentColor);

        // Bright end-cap
        mCapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCapPaint.setStyle(Paint.Style.FILL);
        mCapPaint.setColor(blendColors(mAccentColor, Color.WHITE, 0.5f));

        // Percentage text overlay paint
        float textSizePx = dpToPx(11f);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(textSizePx);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTextOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextOutlinePaint.setStyle(Paint.Style.STROKE);
        mTextOutlinePaint.setStrokeWidth(dpToPx(1.5f));
        mTextOutlinePaint.setColor(Color.argb(180, 0, 0, 0));
        mTextOutlinePaint.setTextSize(textSizePx);
        mTextOutlinePaint.setTextAlign(Paint.Align.CENTER);

        // Try Share Tech Mono or Orbitron font for cyber look
        try {
            Typeface font = context.getResources().getFont(R.font.share_tech_mono);
            if (font != null) {
                mTextPaint.setTypeface(font);
                mTextOutlinePaint.setTypeface(font);
            }
        } catch (Exception ignored) {}

        setMinimumHeight((int) dpToPx(MIN_HEIGHT_DP));
    }

    public void setProgress(int progress, int max) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mMax      = Math.max(1, max);
        mProgress = Math.max(0, Math.min(progress, mMax));
        invalidate();
    }

    public void animateProgressTo(int targetProgress, int max) {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mMax = Math.max(1, max);
        float endVal = Math.max(0, Math.min(targetProgress, mMax));

        mAnimator = ValueAnimator.ofFloat(mProgress, endVal);
        mAnimator.setDuration(1200L);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(anim -> {
            mProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    public void startContinuousDemo() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mMax = 100;
        mAnimator = ValueAnimator.ofFloat(0f, 100f);
        mAnimator.setDuration(3800L);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addUpdateListener(anim -> {
            mProgress = (float) anim.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    public void setShowPercentageText(boolean show) {
        mShowText = show;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w   = getWidth();
        float h   = getHeight();
        float r   = dpToPx(CORNER_RADIUS);
        float cut = h * 0.4f; // 40% height for crisp hexagon points

        RectF trackRect = new RectF(0, 0, w, h);
        float fillRight = w * (mProgress / (float) mMax);

        if (mIsCyberTheme) {
            // --- Cyber: Symmetrical 6-sided Hexagon Bar Path ---
            Path hexagonPath = buildHexagonPath(w, h, cut);
            canvas.save();
            canvas.clipPath(hexagonPath);

            // Background track fill + stroke outline
            canvas.drawRect(trackRect, mTrackPaint);
            canvas.drawPath(hexagonPath, mBorderPaint);

            // Progress fill
            if (fillRight > 0) {
                canvas.drawRect(0, 0, fillRight, h, mFillPaint);

                // Bright end-cap
                float capW = dpToPx(CAP_WIDTH_DP);
                float capL = Math.max(0, fillRight - capW);
                canvas.drawRect(capL, 0, fillRight, h, mCapPaint);
            }

            canvas.restore();
        } else {
            // --- Classic: Rounded Rect ---
            canvas.drawRoundRect(trackRect, r, r, mTrackPaint);
            canvas.drawRoundRect(trackRect, r, r, mBorderPaint);

            // Progress fill
            if (fillRight > 0) {
                RectF fillRect = new RectF(0, 0, fillRight, h);
                canvas.drawRoundRect(fillRect, r, r, mFillPaint);

                // Bright end-cap
                float capW = dpToPx(CAP_WIDTH_DP);
                float capL = Math.max(0, fillRight - capW);
                RectF capRect = new RectF(capL, 0, fillRight, h);
                canvas.drawRoundRect(capRect, r, r, mCapPaint);
            }
        }

        // --- DYNAMIC SPLIT-COLOR PERCENTAGE TEXT OVERLAY ---
        if (mShowText && h >= dpToPx(10f)) {
            int percent = Math.round((mProgress / (float) mMax) * 100f);
            String label = percent + "%";

            float textX = w / 2f;
            float textY = h / 2f - (mTextPaint.descent() + mTextPaint.ascent()) / 2f;

            // Compute optimal contrasting text color over the filled accent bar
            int filledTextColor = calculateContrastingColor(mAccentColor);

            // --- Pass 1: Draw text over unfilled dark track (White text + Dark outline) ---
            canvas.save();
            if (fillRight > 0 && fillRight < w) {
                // Clip Pass 1 to the unfilled track region [fillRight → w]
                canvas.clipRect(fillRight, 0, w, h);
            }
            if (fillRight < w) {
                mTextOutlinePaint.setColor(Color.argb(200, 0, 0, 0));
                canvas.drawText(label, textX, textY, mTextOutlinePaint);

                mTextPaint.setColor(Color.WHITE);
                canvas.drawText(label, textX, textY, mTextPaint);
            }
            canvas.restore();

            // --- Pass 2: Draw text over filled progress bar (Dynamically contrasted text) ---
            if (fillRight > 0) {
                canvas.save();
                canvas.clipRect(0, 0, fillRight, h);

                // Outline color for pass 2
                int outlineColor = (filledTextColor == Color.WHITE) ? Color.argb(180, 0, 0, 0) : Color.argb(120, 255, 255, 255);
                mTextOutlinePaint.setColor(outlineColor);
                canvas.drawText(label, textX, textY, mTextOutlinePaint);

                mTextPaint.setColor(filledTextColor);
                canvas.drawText(label, textX, textY, mTextPaint);

                canvas.restore();
            }
        }
    }

    /**
     * Builds a symmetrical 6-sided Hexagon Bar path (pointy left and right ends).
     */
    private Path buildHexagonPath(float w, float h, float cut) {
        float midY = h / 2f;

        Path path = new Path();
        path.moveTo(cut, 0);             // Top-left cut start
        path.lineTo(w - cut, 0);         // Top edge → Top-right cut start
        path.lineTo(w, midY);            // Right hexagon center point
        path.lineTo(w - cut, h);         // Bottom-right cut
        path.lineTo(cut, h);             // Bottom edge → Bottom-left cut
        path.lineTo(0, midY);            // Left hexagon center point
        path.close();
        return path;
    }

    /**
     * Calculates contrasting text color (Deep Black or White) based on background color luminance.
     */
    private static int calculateContrastingColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 130 ? Color.parseColor("#020A02") : Color.WHITE;
    }

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
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

    private static boolean resolveBoolean(Context context, int attr, boolean fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return ta.getBoolean(0, fallback);
        } finally {
            ta.recycle();
        }
    }
}
