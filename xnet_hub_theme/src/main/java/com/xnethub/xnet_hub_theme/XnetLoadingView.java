package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * XnetLoadingView
 *
 * Two loading styles in one widget, controlled by setStyle():
 *
 *   STYLE_SPINNER (default) — spinning hexagon (cyber) or arc (classic).
 *
 *   STYLE_TERMINAL — terminal-style loading where the spinner icon is drawn
 *     smaller on the left and a typing text animates to its right:
 *       "INITIALIZING|"  → types char-by-char → holds → clears → repeats.
 *
 * Attributes resolved:
 *   - xnetIsCyberTheme  → hexagon vs arc
 *   - xnetAccentPrimary → stroke / text color
 *
 * Rotation: 1 full rotation per 1200ms using ValueAnimator.
 */
public class XnetLoadingView extends View {

    public static final int STYLE_SPINNER  = 0;
    public static final int STYLE_TERMINAL = 1;

    private static final int   ANIMATION_DURATION_MS = 1200;
    private static final float STROKE_WIDTH_DP        = 2.5f;
    private static final float ARC_SWEEP_DEGREES      = 280f;

    // Terminal style config
    private static final String[] TERMINAL_TEXTS = {
        "INITIALIZING...",
        "CONNECTING...",
        "LOADING...",
        "PLEASE WAIT..."
    };
    private static final long CHAR_DELAY_MS  = 60L;
    private static final long HOLD_DELAY_MS  = 600L;
    private static final long CLEAR_DELAY_MS = 200L;

    private Paint   mPaint;
    private Paint   mTextPaint;
    private boolean mIsCyberTheme;
    private float   mRotationAngle = 0f;
    private ValueAnimator mAnimator;

    private int    mStyle = STYLE_SPINNER;
    private int    mCurrentTextIndex = 0;
    private int    mCurrentCharIndex = 0;
    private String mCurrentDisplay   = "";
    private boolean mCursorVisible   = true;
    private boolean mIsClearing      = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public XnetLoadingView(Context context) {
        super(context);
        init(context, null);
    }

    public XnetLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XnetLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Switch between STYLE_SPINNER and STYLE_TERMINAL. */
    public void setStyle(int style) {
        mStyle = style;
        if (style == STYLE_TERMINAL) {
            startTerminalAnimation();
        } else {
            mHandler.removeCallbacksAndMessages(null);
        }
        invalidate();
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    private void init(Context context, AttributeSet attrs) {
        int accentColor = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mIsCyberTheme   = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);

        float strokeWidth = dpToPx(STROKE_WIDTH_DP);

        // Spinner paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(accentColor);

        // Terminal text paint
        float textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 11f, context.getResources().getDisplayMetrics());
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(accentColor);
        mTextPaint.setTextSize(textSizePx);

        // Try to use Share Tech Mono font if available
        try {
            android.graphics.Typeface mono = context.getResources().getFont(R.font.share_tech_mono);
            if (mono != null) mTextPaint.setTypeface(mono);
        } catch (Exception ignored) {}

        startRotationAnimator();
    }

    // -----------------------------------------------------------------------
    // Spinner Animation
    // -----------------------------------------------------------------------

    private void startRotationAnimator() {
        mAnimator = ValueAnimator.ofFloat(0f, 360f);
        mAnimator.setDuration(ANIMATION_DURATION_MS);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            mRotationAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }

    // -----------------------------------------------------------------------
    // Terminal Typing Animation
    // -----------------------------------------------------------------------

    private void startTerminalAnimation() {
        mCurrentTextIndex = 0;
        mCurrentCharIndex = 0;
        mCurrentDisplay   = "";
        mIsClearing       = false;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mTypingRunnable);
        mHandler.post(mCursorBlinkRunnable);
    }

    private final Runnable mCursorBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            mCursorVisible = !mCursorVisible;
            invalidate();
            mHandler.postDelayed(this, 500L);
        }
    };

    private final Runnable mTypingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            String target = TERMINAL_TEXTS[mCurrentTextIndex];

            if (!mIsClearing) {
                // Typing phase
                if (mCurrentCharIndex <= target.length()) {
                    mCurrentDisplay = target.substring(0, mCurrentCharIndex);
                    mCurrentCharIndex++;
                    invalidate();

                    if (mCurrentCharIndex <= target.length()) {
                        mHandler.postDelayed(this, CHAR_DELAY_MS);
                    } else {
                        // Finished typing — hold, then clear
                        mHandler.postDelayed(() -> {
                            mIsClearing = true;
                            mHandler.post(mTypingRunnable);
                        }, HOLD_DELAY_MS);
                    }
                }
            } else {
                // Clearing phase — erase one char at a time
                if (mCurrentDisplay.length() > 0) {
                    mCurrentDisplay = mCurrentDisplay.substring(0, mCurrentDisplay.length() - 1);
                    invalidate();
                    mHandler.postDelayed(this, CLEAR_DELAY_MS);
                } else {
                    // Move to next text
                    mIsClearing = false;
                    mCurrentCharIndex = 0;
                    mCurrentTextIndex = (mCurrentTextIndex + 1) % TERMINAL_TEXTS.length;
                    mHandler.postDelayed(this, 200L);
                }
            }
        }
    };

    // -----------------------------------------------------------------------
    // Drawing
    // -----------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mStyle == STYLE_TERMINAL) {
            drawTerminal(canvas);
        } else {
            drawSpinner(canvas, getWidth() / 2f, getHeight() / 2f,
                Math.min(getWidth(), getHeight()) / 2f - mPaint.getStrokeWidth());
        }
    }

    private void drawSpinner(Canvas canvas, float cx, float cy, float radius) {
        canvas.save();
        canvas.rotate(mRotationAngle, cx, cy);
        if (mIsCyberTheme) {
            drawHexagonPath(canvas, cx, cy, radius);
        } else {
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(oval, 0f, ARC_SWEEP_DEGREES, false, mPaint);
        }
        canvas.restore();
    }

    private void drawTerminal(Canvas canvas) {
        int   w  = getWidth();
        int   h  = getHeight();

        // Small spinner on the left — ~30% of height as diameter
        float iconSize = h * 0.5f;
        float iconCx   = iconSize + dpToPx(4f);
        float iconCy   = h / 2f;
        float iconR    = iconSize / 2f - mPaint.getStrokeWidth();

        drawSpinner(canvas, iconCx, iconCy, iconR);

        // Typing text to the right of the spinner
        String cursor    = mCursorVisible ? "|" : " ";
        String displayed = mCurrentDisplay + cursor;

        float textX = iconCx + iconSize / 2f + dpToPx(8f);
        float textY = iconCy - (mTextPaint.descent() + mTextPaint.ascent()) / 2f;

        canvas.drawText(displayed, textX, textY, mTextPaint);
    }

    private void drawHexagonPath(Canvas canvas, float cx, float cy, float radius) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(-90 + i * 60);
            float  x     = cx + radius * (float) Math.cos(angle);
            float  y     = cy + radius * (float) Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else         path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, mPaint);
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAnimator != null && !mAnimator.isRunning()) mAnimator.start();
        if (mStyle == STYLE_TERMINAL) startTerminalAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) mAnimator.cancel();
        mHandler.removeCallbacksAndMessages(null);
    }

    // -----------------------------------------------------------------------
    // Utility helpers
    // -----------------------------------------------------------------------

    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    private static int resolveColor(Context context, int attr, int fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try { return ta.getColor(0, fallback); } finally { ta.recycle(); }
    }

    private static boolean resolveBoolean(Context context, int attr, boolean fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try { return ta.getBoolean(0, fallback); } finally { ta.recycle(); }
    }
}
