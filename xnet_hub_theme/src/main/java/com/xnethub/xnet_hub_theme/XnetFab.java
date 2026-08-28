package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;

/**
 * XnetFab
 *
 * Official Xnet Hub Theme Library Custom Cyber Floating Action Button Widget.
 * Built on top of Material {@link FloatingActionButton}.
 *
 * Features:
 *   - Cyber Theme : 6-Sided Cut-Corner Hexagon FAB shape + Accent Stroke Border + Touch Press Pulse Animation
 *   - Classic / Normal / System Theme : Standard circular Material FAB shape
 */
public class XnetFab extends FloatingActionButton {

    private boolean mIsCyberTheme = false;
    private int mAccentColor = 0xFF00FFCC;
    private int mSurfaceAlt = 0xFF1A1A2E;
    private int mStrokeColor = 0xFF00FFCC;

    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mStrokePath = new Path();
    private float mStrokeWidth;

    public XnetFab(@NonNull Context context) {
        this(context, null);
    }

    public XnetFab(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetFab(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mIsCyberTheme = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);
        mAccentColor = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mSurfaceAlt = resolveColor(context, R.attr.xnetSurfaceAlt, 0xFF1A1A2E);
        mStrokeColor = resolveColor(context, R.attr.xnetStroke, mAccentColor);

        float density = context.getResources().getDisplayMetrics().density;

        if (mIsCyberTheme) {
            // Cyber 6-Sided Cut-Corner Shape (16dp cut on all corners creates a sharp 6-sided hexagon profile)
            float cutSize = 14f * density;
            ShapeAppearanceModel shape = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.CUT, cutSize)
                    .build();
            setShapeAppearanceModel(shape);

            // Tinting & Ripple
            setBackgroundTintList(ColorStateList.valueOf(mSurfaceAlt));
            setRippleColor(applyAlpha(mAccentColor, 0x66));
            setImageTintList(ColorStateList.valueOf(mAccentColor));

            // Setup stroke for Cyber theme
            mStrokeWidth = 1.5f * density;
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setStrokeWidth(mStrokeWidth);
            mStrokePaint.setColor(mStrokeColor);
        } else {
            // Classic Circular Material FAB
            ShapeAppearanceModel shape = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, 100f * density)
                    .build();
            setShapeAppearanceModel(shape);
            setBackgroundTintList(ColorStateList.valueOf(mAccentColor));
            setImageTintList(ColorStateList.valueOf(contrastColor(mAccentColor)));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mIsCyberTheme) {
            // Generate the exact cut-corner shape path for the stroke
            // We inset the rect by half the stroke width so the stroke isn't clipped by view bounds
            float inset = mStrokeWidth / 2f;
            RectF rect = new RectF(inset, inset, w - inset, h - inset);
            ShapeAppearancePathProvider.getInstance().calculatePath(getShapeAppearanceModel(), 1f, rect, mStrokePath);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsCyberTheme) {
            // Draw the themed outline stroke over the FAB
            canvas.drawPath(mStrokePath, mStrokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIsCyberTheme) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animate().scaleX(1.12f).scaleY(1.12f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    /** Returns black or white depending on the background luminance. */
    private static int contrastColor(int bgColor) {
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >>  8) & 0xFF;
        int b =  bgColor        & 0xFF;
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 130 ? 0xFF020A02 : 0xFFFFFFFF;
    }

    private static int applyAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
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
