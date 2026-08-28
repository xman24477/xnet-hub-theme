package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Random;

/**
 * XnetGlitchTextView
 *
 * A custom TextView that applies a periodic glitch/chromatic-aberration animation.
 * - X-Cyber Themes : Full RGB split + shake glitch effect.
 * - Classic Themes : Subtle opacity flicker only.
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetGlitchTextView
 *       android:text="XNET HUB"
 *       android:textSize="24sp" />
 */
public class XnetGlitchTextView extends AppCompatTextView {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private boolean isCyber = false;
    private boolean isGlitching = false;
    private int glitchOffsetX = 0;
    private int glitchAlpha = 255;

    // Ghost paint for RGB split channels
    private final Paint redChannelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cyanChannelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Glitch timing configuration
    private static final long GLITCH_INTERVAL_MS = 2800L;   // pause between glitch sequences
    private static final long GLITCH_FRAME_MS    = 60L;     // each glitch frame duration
    private static final int  GLITCH_FRAMES      = 4;       // number of rapid glitch frames

    private final Runnable glitchSequenceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            triggerGlitchSequence(0);
        }
    };

    public XnetGlitchTextView(@NonNull Context context) {
        this(context, null);
    }

    public XnetGlitchTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public XnetGlitchTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Detect cyber theme
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            isCyber = tv.data != 0;
        }

        // Resolve text color for ghost channels
        int textColor = getCurrentTextColor();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            textColor = tv.data;
        }
        setTextColor(textColor);

        // Red ghost channel
        redChannelPaint.setTextSize(getTextSize());
        redChannelPaint.setTypeface(getTypeface());
        redChannelPaint.setColor(Color.argb(120, 255, 0, 80));   // semi-transparent red
        redChannelPaint.setStyle(Paint.Style.FILL);

        // Cyan ghost channel
        cyanChannelPaint.setTextSize(getTextSize());
        cyanChannelPaint.setTypeface(getTypeface());
        cyanChannelPaint.setColor(Color.argb(120, 0, 210, 255)); // semi-transparent cyan
        cyanChannelPaint.setStyle(Paint.Style.FILL);

        // Start periodic glitch scheduling
        scheduleNextGlitch();
    }

    /** Schedule the next glitch sequence after an interval */
    private void scheduleNextGlitch() {
        long delay = GLITCH_INTERVAL_MS + (long) (random.nextInt(1500));
        handler.postDelayed(glitchSequenceRunnable, delay);
    }

    /** Run a rapid multi-frame glitch burst */
    private void triggerGlitchSequence(final int frameIndex) {
        if (frameIndex >= GLITCH_FRAMES) {
            // End of glitch: restore normal state
            isGlitching = false;
            glitchOffsetX = 0;
            glitchAlpha = 255;
            invalidate();
            scheduleNextGlitch();
            return;
        }

        isGlitching = true;
        glitchOffsetX = random.nextBoolean() ? random.nextInt(8) + 2 : -(random.nextInt(8) + 2);
        glitchAlpha   = 180 + random.nextInt(75);
        invalidate();

        handler.postDelayed(() -> triggerGlitchSequence(frameIndex + 1), GLITCH_FRAME_MS);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isCyber && isGlitching && getText() != null) {
            String text = getText().toString();
            float x = getPaddingLeft();
            float y = getBaseline();

            // Draw red ghost shifted left
            canvas.drawText(text, x - glitchOffsetX - 3, y, redChannelPaint);
            // Draw cyan ghost shifted right
            canvas.drawText(text, x + glitchOffsetX + 3, y, cyanChannelPaint);

            // Draw main text with slight alpha flicker
            setAlpha((float) glitchAlpha / 255f);
            super.onDraw(canvas);
            setAlpha(1f);
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scheduleNextGlitch();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
