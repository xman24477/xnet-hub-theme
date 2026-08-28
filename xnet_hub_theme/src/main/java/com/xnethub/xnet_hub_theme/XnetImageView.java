package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.Random;

/**
 * XnetImageView
 *
 * Official Xnet Hub Theme Library Custom ImageView.
 *
 * - X-Cyber Themes  → Clips image into a 6-sided regular hexagon with a 1dp outer stroke.
 * - Classic Themes  → Clips image into a circle with a 1dp outer stroke.
 *
 * The outer outline stroke is spaced 2dp outside the clipped image.
 */
public class XnetImageView extends AppCompatImageView {

    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path outerPath = new Path();
    private final Path innerPath = new Path();
    private boolean isCyber = false;
    private int strokeColor = Color.GRAY;

    public XnetImageView(@NonNull Context context) {
        this(context, null);
    }

    public XnetImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Resolve theme properties
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            isCyber = tv.data != 0;
        }

        // Set default stroke paint attributes
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.2f, getResources().getDisplayMetrics()));

        // Resolve stroke color
        if (isCyber) {
            if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
                strokeColor = tv.data;
            }
            // Cyber RGB theme support
            if (XnetThemeManager.THEME_CYBER_RGB.equals(XnetThemeManager.getTheme(getContext()))) {
                Random r = new Random(hashCode());
                strokeColor = XnetThemeManager.getRgbAccents(getContext())[r.nextInt(XnetThemeManager.getRgbAccents(getContext()).length)];
            }
        } else {
            if (getContext().getTheme().resolveAttribute(R.attr.xnetStroke, tv, true)) {
                strokeColor = tv.data;
            }
        }
        strokePaint.setColor(strokeColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePaths(w, h);
    }

    private void updatePaths(int w, int h) {
        outerPath.reset();
        innerPath.reset();

        float cx = w / 2f;
        float cy = h / 2f;
        float maxRadius = Math.min(w, h) / 2f;

        // Reserve 4dp margin to prevent view bounds from clipping the outer 1dp stroke
        float strokeMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());
        float outerRadius = maxRadius - strokeMargin;

        // Gap/Margin between outer outline and inner clipped image (2.5dp)
        float gap = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.5f, getResources().getDisplayMetrics());
        float innerRadius = outerRadius - gap;

        if (isCyber) {
            buildHexagonPath(outerPath, cx, cy, outerRadius);
            buildHexagonPath(innerPath, cx, cy, innerRadius);
        } else {
            outerPath.addCircle(cx, cy, outerRadius, Path.Direction.CW);
            innerPath.addCircle(cx, cy, innerRadius, Path.Direction.CW);
        }
    }

    private void buildHexagonPath(Path path, float cx, float cy, float radius) {
        // Pointy-topped regular hexagon (starts at -90 degrees)
        for (int i = 0; i < 6; i++) {
            double angleRad = Math.toRadians(i * 60 - 90);
            float x = (float) (cx + radius * Math.cos(angleRad));
            float y = (float) (cy + radius * Math.sin(angleRad));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 1. Clip canvas to the inner path (clipping the image itself)
        canvas.save();
        canvas.clipPath(innerPath);
        super.onDraw(canvas);
        canvas.restore();

        // 2. Draw the outer themed outline stroke
        canvas.drawPath(outerPath, strokePaint);
    }
}
