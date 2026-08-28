package com.xnethub.xnet_hub_theme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * XnetSplashAnimationView
 * A reusable component for splash screens that drops text letter-by-letter,
 * then converts it to brand-formatted text with glitch effects.
 */
public class XnetSplashAnimationView extends LinearLayout {

    private LinearLayout lettersContainer;
    private XnetBrandTextView finalBrandTextView;
    private XnetBrandTextView subtitleTextView;
    
    private String appName = "Xnet Smart";
    private String subtitle = "Xnet Hub Presents";

    public XnetSplashAnimationView(@NonNull Context context) {
        super(context);
        init();
    }

    public XnetSplashAnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XnetSplashAnimationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        // Container for dropping letters
        lettersContainer = new LinearLayout(getContext());
        lettersContainer.setOrientation(HORIZONTAL);
        lettersContainer.setGravity(Gravity.CENTER);
        addView(lettersContainer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Final Brand Text View (hidden initially)
        finalBrandTextView = new XnetBrandTextView(getContext());
        finalBrandTextView.setTextSize(32f);
        finalBrandTextView.setTypeface(null, Typeface.BOLD);
        finalBrandTextView.setVisibility(GONE);
        addView(finalBrandTextView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Subtitle Text View (hidden initially)
        subtitleTextView = new XnetBrandTextView(getContext());
        subtitleTextView.setTextSize(16f);
        subtitleTextView.setTypeface(null, Typeface.ITALIC);
        subtitleTextView.setAlpha(0f);
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = 16;
        addView(subtitleTextView, lp);
    }

    public void setTexts(String appName, String subtitle) {
        this.appName = appName;
        this.subtitle = subtitle;
    }

    public void startAnimation(Runnable onComplete) {
        lettersContainer.removeAllViews();
        lettersContainer.setVisibility(VISIBLE);
        finalBrandTextView.setVisibility(GONE);
        subtitleTextView.setAlpha(0f);
        
        finalBrandTextView.setText(appName);
        subtitleTextView.setText(subtitle);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;

        // Generate unformatted letters
        for (int i = 0; i < appName.length(); i++) {
            TextView tvLetter = new TextView(getContext());
            tvLetter.setText(String.valueOf(appName.charAt(i)));
            tvLetter.setTextSize(32f);
            tvLetter.setTypeface(null, Typeface.BOLD);
            android.util.TypedValue typedValue = new android.util.TypedValue(); getContext().getTheme().resolveAttribute(R.attr.xnetTextPrimary, typedValue, true); tvLetter.setTextColor(typedValue.data);
            
            // Start off-screen
            tvLetter.setTranslationY(-screenHeight);
            lettersContainer.addView(tvLetter);
            
            tvLetter.animate()
                    .translationY(0)
                    .setDuration(800)
                    .setStartDelay(i * 100L)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        // When the last letter drops, trigger swap and subtitle fade
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            lettersContainer.setVisibility(GONE);
            finalBrandTextView.setVisibility(VISIBLE);
            // Flash effect for the transition
            finalBrandTextView.setAlpha(0f);
            finalBrandTextView.animate().alpha(1f).setDuration(200).start();

            subtitleTextView.animate().alpha(1f).setDuration(800).start();
            
            // The XnetBrandTextView will auto-glitch based on its internal timer (every 2.8s)
            // Wait for 1-2 glitches (e.g. 3.5 seconds) then complete
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            }, 3500);

        }, (appName.length() * 100L) + 800L);
    }
}
