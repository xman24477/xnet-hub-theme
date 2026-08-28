package com.xnethub.xnet_hub_theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * XnetSwitch
 *
 * A fully custom toggle switch tailored for the Xnet Hub Theme System:
 *
 *   X-Cyber Themes  → Track is a symmetrical 6-sided HEXAGON capsule
 *                      (both left and right ends are cut into sharp hexagonal points).
 *                      Thumb is a sharp 6-sided flat-top HEXAGON sliding inside.
 *
 *   Classic Themes  → Standard pill-shaped track with circular thumb.
 *
 * Use setOnCheckedChangeListener() to receive toggle state callbacks.
 */
public class XnetSwitch extends View {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(XnetSwitch switchView, boolean isChecked);
    }

    // State
    private boolean mChecked   = false;
    private boolean mIsCyber   = false;
    private float   mThumbPos  = 0f; // 0.0=OFF → 1.0=ON

    private OnCheckedChangeListener mListener;

    // Colors
    private int mAccentColor   = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_green_accent_primary);
    private int mSurfaceColor  = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_green_surface_raised);
    private int mThumbOffColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_green_surface_alt);

    // Paints
    private final Paint mTrackPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Animator
    private ValueAnimator mThumbAnimator;

    // Dimensions
    private float mTrackWidth, mTrackHeight, mTrackRadius;
    private float mThumbRadius;
    private float mTrackLeft, mTrackTop;
    private float mCutSize;

    public XnetSwitch(@NonNull Context context) {
        this(context, null);
    }

    public XnetSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        TypedValue tv = new TypedValue();

        // Is cyber theme?
        if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            mIsCyber = tv.data != 0;
        }

        // Accent color
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            mAccentColor = tv.data;
        }

        // Surface color for track background
        if (getContext().getTheme().resolveAttribute(R.attr.xnetSurfaceAlt, tv, true)) {
            mSurfaceColor = tv.data;
        } else {
            mSurfaceColor = Color.argb(160,
                Color.red(mAccentColor) / 6,
                Color.green(mAccentColor) / 6,
                Color.blue(mAccentColor) / 6);
        }

        // Thumb OFF color
        mThumbOffColor = Color.argb(180, 90, 100, 90);

        mTrackPaint.setStyle(Paint.Style.FILL);
        mThumbPaint.setStyle(Paint.Style.FILL);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(dpToPx(1.2f));

        setOnClickListener(v -> toggle());

        int minW = (int) dpToPx(56f);
        int minH = (int) dpToPx(28f);
        setMinimumWidth(minW);
        setMinimumHeight(minH);
    }

    public boolean isChecked() { return mChecked; }

    public void setChecked(boolean checked) {
        if (mChecked == checked) return;
        mChecked = checked;
        animateThumb(checked ? 1f : 0f);
        if (mListener != null) mListener.onCheckedChanged(this, mChecked);
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredW = (int) dpToPx(58f);
        int desiredH = (int) dpToPx(28f);
        setMeasuredDimension(
            resolveSize(desiredW, widthMeasureSpec),
            resolveSize(desiredH, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float padding = dpToPx(2f);
        mTrackHeight  = h - padding * 2;
        mTrackWidth   = w - padding * 2;
        mTrackLeft    = padding;
        mTrackTop     = padding;
        mTrackRadius  = mTrackHeight / 2f;
        mThumbRadius  = mTrackHeight / 2f - dpToPx(3f);
        mCutSize      = mTrackHeight * 0.4f; // 40% height for crisp hexagon ends
    }

    private void animateThumb(float toValue) {
        if (mThumbAnimator != null) mThumbAnimator.cancel();
        mThumbAnimator = ValueAnimator.ofFloat(mThumbPos, toValue);
        mThumbAnimator.setDuration(220L);
        mThumbAnimator.setInterpolator(new DecelerateInterpolator());
        mThumbAnimator.addUpdateListener(anim -> {
            mThumbPos = (float) anim.getAnimatedValue();
            invalidate();
        });
        mThumbAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int trackColor = blendColors(mSurfaceColor,
            Color.argb(80,
                Color.red(mAccentColor),
                Color.green(mAccentColor),
                Color.blue(mAccentColor)),
            mThumbPos);

        int borderColor = blendColors(
            Color.argb(80, Color.red(mAccentColor), Color.green(mAccentColor), Color.blue(mAccentColor)),
            mAccentColor,
            mThumbPos);

        int thumbColor = blendColors(mThumbOffColor, mAccentColor, mThumbPos);

        mTrackPaint.setColor(trackColor);
        mBorderPaint.setColor(borderColor);
        mThumbPaint.setColor(thumbColor);

        float l = mTrackLeft;
        float t = mTrackTop;
        float r = l + mTrackWidth;
        float b = t + mTrackHeight;

        if (mIsCyber) {
            // Symmetrical 6-sided Hexagon track
            drawHexagonTrack(canvas, l, t, r, b, mCutSize);
        } else {
            // Classic rounded-rect track
            canvas.drawRoundRect(l, t, r, b, mTrackRadius, mTrackRadius, mTrackPaint);
            canvas.drawRoundRect(l, t, r, b, mTrackRadius, mTrackRadius, mBorderPaint);
        }

        // Thumb positioning
        float startX = l + mThumbRadius + dpToPx(3f);
        float endX   = r - mThumbRadius - dpToPx(3f);
        float thumbCenterX = startX + (endX - startX) * mThumbPos;
        float thumbCenterY = t + mTrackHeight / 2f;

        if (mIsCyber) {
            // Symmetrical Hexagon thumb
            drawHexagonThumb(canvas, thumbCenterX, thumbCenterY, mThumbRadius);
        } else {
            // Circle thumb
            canvas.drawCircle(thumbCenterX, thumbCenterY, mThumbRadius, mThumbPaint);
        }
    }

    /**
     * Draws a symmetrical 6-sided Hexagon Track Capsule
     * where BOTH left and right ends are sharp 3-segment hexagon points.
     */
    private void drawHexagonTrack(Canvas canvas, float l, float t, float r, float b, float cut) {
        float midY = t + (b - t) / 2f;

        Path path = new Path();
        path.moveTo(l + cut, t);          // Top-left cut
        path.lineTo(r - cut, t);          // Top edge → Top-right cut start
        path.lineTo(r, midY);             // Right hexagon center point
        path.lineTo(r - cut, b);          // Bottom-right cut
        path.lineTo(l + cut, b);          // Bottom edge → Bottom-left cut
        path.lineTo(l, midY);             // Left hexagon center point
        path.close();

        canvas.drawPath(path, mTrackPaint);
        canvas.drawPath(path, mBorderPaint);
    }

    /**
     * Draws a flat-top 6-sided Hexagon Thumb.
     */
    private void drawHexagonThumb(Canvas canvas, float cx, float cy, float radius) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            float x = cx + radius * (float) Math.cos(angle);
            float y = cy + radius * (float) Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else        path.lineTo(x, y);
        }
        path.close();

        canvas.drawPath(path, mThumbPaint);

        // Add subtle inner border for 3D cyber depth
        Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStrokeWidth(dpToPx(0.8f));
        innerPaint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawPath(path, innerPaint);
    }

    private static int blendColors(int from, int to, float ratio) {
        float inv = 1f - ratio;
        int a = (int)(Color.alpha(from) * inv + Color.alpha(to) * ratio);
        int r = (int)(Color.red(from)   * inv + Color.red(to)   * ratio);
        int g = (int)(Color.green(from) * inv + Color.green(to) * ratio);
        int b = (int)(Color.blue(from)  * inv + Color.blue(to)  * ratio);
        return Color.argb(a, r, g, b);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
