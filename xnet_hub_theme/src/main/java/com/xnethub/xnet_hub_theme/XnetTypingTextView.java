package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * XnetTypingTextView
 *
 * A custom TextView that types out text character by character with a
 * blinking terminal cursor at the end.
 *
 * Supports continuous looping (type → hold → erase → repeat) so the text
 * animation stays active continuously on screen.
 *
 * Usage in XML:
 *   <com.xnethub.xnet_hub_theme.XnetTypingTextView
 *       android:text="ACCESS GRANTED"
 *       android:textSize="16sp" />
 */
public class XnetTypingTextView extends AppCompatTextView {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private String fullText = "";
    private int charIndex = 0;
    private boolean cursorVisible = true;
    private boolean isErasing = false;
    private boolean isLooping = true; // default true so animation stays alive continuously

    private static final long DEFAULT_CHAR_DELAY_MS  = 65L;   // ms per character typed
    private static final long DEFAULT_ERASE_DELAY_MS = 30L;   // ms per character erased
    private static final long HOLD_DELAY_MS          = 2200L; // hold text before erasing
    private static final long CURSOR_BLINK_MS        = 500L;  // ms between cursor blinks
    private static final String CURSOR               = "|";

    private long charDelayMs = DEFAULT_CHAR_DELAY_MS;

    private final Runnable cursorBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;
            cursorVisible = !cursorVisible;
            refreshDisplay();
            handler.postDelayed(this, CURSOR_BLINK_MS);
        }
    };

    public interface OnTypingCompleteListener {
        void onTypingComplete();
    }
    private OnTypingCompleteListener mListener;

    private final Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAttachedToWindow()) return;

            if (!isErasing) {
                // --- Typing Phase ---
                if (charIndex <= fullText.length()) {
                    refreshDisplay();
                    charIndex++;
                    handler.postDelayed(this, charDelayMs);
                } else {
                    // Finished typing
                    if (mListener != null) {
                        mListener.onTypingComplete();
                    }
                    if (isLooping) {
                        // Hold for a moment, then start erasing
                        handler.postDelayed(() -> {
                            isErasing = true;
                            handler.post(typingRunnable);
                        }, HOLD_DELAY_MS);
                    }
                }
            } else {
                // --- Erasing Phase ---
                if (charIndex > 0) {
                    charIndex--;
                    refreshDisplay();
                    handler.postDelayed(this, DEFAULT_ERASE_DELAY_MS);
                } else {
                    // Finished erasing -> restart typing
                    isErasing = false;
                    charIndex = 0;
                    handler.postDelayed(this, 300L);
                }
            }
        }
    };

    public XnetTypingTextView(@NonNull Context context) {
        this(context, null);
    }

    public XnetTypingTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public XnetTypingTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Apply themed accent color
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
            setTextColor(tv.data);
        }

        // Capture initial text set from XML
        CharSequence xmlText = getText();
        if (xmlText != null && xmlText.length() > 0) {
            fullText = xmlText.toString();
        }
    }

    /** Set whether the animation should loop continuously. */
    public void setLoop(boolean loop) {
        this.isLooping = loop;
    }

    public void setOnTypingCompleteListener(OnTypingCompleteListener listener) {
        this.mListener = listener;
    }

    /** Set the text to type out and begin animation. */
    public void setTypingText(@NonNull String text, long charDelayMs) {
        handler.removeCallbacks(typingRunnable);
        this.fullText    = text;
        this.charDelayMs = charDelayMs;
        this.charIndex   = 0;
        this.isErasing   = false;
        handler.post(typingRunnable);
    }

    /** Restart typing of current text from the beginning. */
    public void restartTyping() {
        setTypingText(fullText, charDelayMs);
    }

    private void refreshDisplay() {
        String visible = fullText.substring(0, Math.min(Math.max(0, charIndex), fullText.length()));
        String cursor  = cursorVisible ? CURSOR : " ";
        setText(visible + cursor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacksAndMessages(null);

        // Start cursor blink
        handler.postDelayed(cursorBlinkRunnable, CURSOR_BLINK_MS);

        // Start continuous typing loop if text is present
        if (fullText.length() > 0) {
            charIndex = 0;
            isErasing = false;
            handler.postDelayed(typingRunnable, 400L);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}
