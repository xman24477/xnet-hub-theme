package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Random;

/**
 * XnetBrandTextView
 *
 * A specialized TextView intended for paragraphs and labels that automatically detects 
 * "Xnet" or "Xnet Hub" and applies the signature brand formatting (colors, bold).
 * Additionally, if used in a Cyber Theme, it applies a glitch/glitter effect
 * specifically onto the brand words, without affecting the rest of the paragraph.
 */
public class XnetBrandTextView extends AppCompatTextView {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

        private boolean isGlitching = false;
    private int glitchOffsetX = 0;
    private int glitchAlpha = 255;

    // Ghost paints for the glitch effect
    private final Paint redChannelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cyanChannelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Glitch timing configuration
    private static final long GLITCH_INTERVAL_MS = 2800L;
    private static final long GLITCH_FRAME_MS    = 60L;
    private static final int  GLITCH_FRAMES      = 4;

    private boolean mIsFormattingInternal = false;

    private final Runnable glitchSequenceRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            triggerGlitchSequence(0);
        }
    };

    public XnetBrandTextView(@NonNull Context context) {
        this(context, null);
    }

    public XnetBrandTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public XnetBrandTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        

        // Setup Ghost channel paints
        redChannelPaint.setColor(Color.argb(120, 255, 0, 80));
        redChannelPaint.setStyle(Paint.Style.FILL);

        cyanChannelPaint.setColor(Color.argb(120, 0, 210, 255));
        cyanChannelPaint.setStyle(Paint.Style.FILL);

        // Apply formatting if text was already set from XML
        formatCurrentText();

        scheduleNextGlitch();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (!mIsFormattingInternal && text != null) {
            // Intercept text setting, apply brand formatting
            mIsFormattingInternal = true;
            CharSequence formatted = XnetTextFormatter.formatBrandText(getContext(), text);
            super.setText(formatted, BufferType.SPANNABLE);
            mIsFormattingInternal = false;
        } else {
            super.setText(text, type);
        }
    }

    private void formatCurrentText() {
        if (getText() != null && getText().length() > 0) {
            setText(getText()); // will trigger the intercepted setText above
        }
    }

    private void scheduleNextGlitch() {
        long delay = GLITCH_INTERVAL_MS + (long) (random.nextInt(1500));
        handler.postDelayed(glitchSequenceRunnable, delay);
    }

    private void triggerGlitchSequence(final int frameIndex) {
        if (frameIndex >= GLITCH_FRAMES) {
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
        if (isGlitching && getText() instanceof Spanned) {
            Spanned spanned = (Spanned) getText();
            Layout layout = getLayout();
            
            if (layout != null) {
                // Find all brand markers
                XnetTextFormatter.XnetBrandMarkerSpan[] markers = spanned.getSpans(
                        0, spanned.length(), XnetTextFormatter.XnetBrandMarkerSpan.class);

                if (markers.length > 0) {
                    // Update paint properties to match TextView
                    redChannelPaint.setTextSize(getTextSize());
                    redChannelPaint.setTypeface(getTypeface());
                    cyanChannelPaint.setTextSize(getTextSize());
                    cyanChannelPaint.setTypeface(getTypeface());

                    float paddingLeft = getTotalPaddingLeft();
                    float paddingTop = getTotalPaddingTop();

                    for (XnetTextFormatter.XnetBrandMarkerSpan marker : markers) {
                        int start = spanned.getSpanStart(marker);
                        int end = spanned.getSpanEnd(marker);

                        if (start < 0 || end < 0) continue;

                        int line = layout.getLineForOffset(start);
                        float startX = layout.getPrimaryHorizontal(start);
                        float baseline = layout.getLineBaseline(line);
                        
                        String brandWord = spanned.toString().substring(start, end);

                        // Draw ghosts manually at the exact coordinates of the brand word
                        canvas.drawText(brandWord, 
                                paddingLeft + startX - glitchOffsetX - 3, 
                                paddingTop + baseline, 
                                redChannelPaint);
                                
                        canvas.drawText(brandWord, 
                                paddingLeft + startX + glitchOffsetX + 3, 
                                paddingTop + baseline, 
                                cyanChannelPaint);
                    }
                }
            }
        }

        super.onDraw(canvas);
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
