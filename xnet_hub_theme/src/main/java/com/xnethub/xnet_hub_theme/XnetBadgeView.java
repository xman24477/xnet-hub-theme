package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * XnetBadgeView
 *
 * A small notification badge view with dynamic theme-aware contrast handling:
 *   - Resolves xnetAccentPrimary for badge fill color
 *   - Dynamically calculates background luminance to automatically pick high-contrast text color
 *     (dark text on bright/light themes, white text on dark themes)
 *   - Cyber Theme: Symmetrical 6-sided Hexagon badge shape
 *   - Classic Theme: Oval badge shape
 *
 * Minimum size: 22 × 22 dp.
 * Use {@link #setCount(int)} to update count text.
 */
public class XnetBadgeView extends AppCompatTextView {

    private static final int   MIN_SIZE_DP  = 22;
    private static final int   PADDING_DP   = 4;
    private static final float TEXT_SIZE_SP = 10f;

    private boolean mIsCyberTheme = false;
    private int     mFillColor    = 0xFF00FFCC;
    private int     mBorderColor  = 0x40FFFFFF;

    private Paint mPaintFill;
    private Paint mPaintBorder;

    private Paint mPaintSurface;

    public XnetBadgeView(Context context) {
        super(context);
        init(context, null);
    }

    public XnetBadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XnetBadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        android.util.TypedValue tv = new android.util.TypedValue();

        mIsCyberTheme = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);

        if (context.getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            mFillColor = tv.data; // We'll use this for the stroke and text color now
        }

        // Get the bottom navigation bar's background color (surface color) 
        // to act as a cutout layer against highlighted selected items.
        int surfaceColor = androidx.core.content.ContextCompat.getColor(getContext(), R.color.xnet_color_cyber_black_surface_alt); // Fallback dark
        if (context.getTheme().resolveAttribute(R.attr.xnetSurfaceRaised, tv, true)) {
            surfaceColor = tv.data;
        }

        float density = context.getResources().getDisplayMetrics().density;

        // Since there is no accent fill color, the text color will just match the accent color
        int textColor = mFillColor;

        // Text styling
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP);
        setTextColor(textColor);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setGravity(Gravity.CENTER);

        // Try Orbitron or Share Tech Mono for cyber text
        try {
            Typeface font = context.getResources().getFont(R.font.share_tech_mono);
            if (font != null) setTypeface(font);
        } catch (Exception ignored) {}

        // Padding (4dp all sides)
        int pad = (int) (PADDING_DP * density);
        setPadding(pad, pad, pad, pad);

        // Minimum size (22dp × 22dp)
        int minSize = (int) (MIN_SIZE_DP * density);
        setMinimumWidth(minSize);
        setMinimumHeight(minSize);

        if (mIsCyberTheme) {
            // Cyber theme: Use custom Canvas drawing for Hexagon badge shape (Solid cutout + Stroke)
            setWillNotDraw(false);

            mPaintSurface = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintSurface.setStyle(Paint.Style.FILL);
            mPaintSurface.setColor(surfaceColor); // Solid cutout background

            mPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintBorder.setStyle(Paint.Style.STROKE);
            mPaintBorder.setStrokeWidth(1.5f * density);
            mPaintBorder.setColor(mFillColor); // Border is the accent color
        } else {
            // Classic theme: Oval background with border (Solid cutout + Stroke)
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(surfaceColor); // Solid cutout background
            background.setStroke((int) (1.5f * density), mFillColor); // Border is the accent color
            setBackground(background);
        }
    }

    /**
     * Sets the badge count.
     *
     * @param count the number to display; values > 99 show as "99+"
     */
    public void setCount(int count) {
        if (count > 99) {
            setText("99+");
        } else {
            setText(String.valueOf(count));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsCyberTheme) {
            float w   = getWidth();
            float h   = getHeight();
            float cut = Math.min(w, h) * 0.25f;
            
            // Adjust bounds slightly to keep stroke fully inside the view
            float strokeOffset = mPaintBorder.getStrokeWidth() / 2f;

            // Draw 6-sided Hexagon badge outline
            Path hexPath = new Path();
            float midY = h / 2f;
            hexPath.moveTo(cut + strokeOffset, strokeOffset);
            hexPath.lineTo(w - cut - strokeOffset, strokeOffset);
            hexPath.lineTo(w - strokeOffset, midY);
            hexPath.lineTo(w - cut - strokeOffset, h - strokeOffset);
            hexPath.lineTo(cut + strokeOffset, h - strokeOffset);
            hexPath.lineTo(strokeOffset, midY);
            hexPath.close();

            // First draw the solid surface color to punch a hole through the highlighted item background
            canvas.drawPath(hexPath, mPaintSurface);
            // Then draw the accent color stroke on top
            canvas.drawPath(hexPath, mPaintBorder);
        }
        super.onDraw(canvas);
    }

    /**
     * Computes contrasting text color (Black/Dark or White) based on background color luminance.
     */
    private static int calculateContrastingTextColor(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 140 ? 0xFF121212 : Color.WHITE;
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
