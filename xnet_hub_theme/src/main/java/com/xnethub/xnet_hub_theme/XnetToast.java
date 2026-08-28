package com.xnethub.xnet_hub_theme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

/**
 * XnetToast
 *
 * A custom styled floating notification that visually matches the XnetCalloutCard,
 * but features an XnetTypingTextView for the message.
 * Instead of disappearing after a fixed time (like standard Android Toasts),
 * this view waits for the typing animation to finish completely before auto-dismissing.
 */
public class XnetToast {

    private XnetToast() {}

    /**
     * Show a custom typing toast message in the given activity.
     */
    public static void show(@NonNull Activity activity, @NonNull String message, @NonNull XnetCalloutCard.CalloutType type) {
        final ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        final Context context = activity;

        // Build the layout identical to XnetCalloutCard but dynamic
        final LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        int paddingPx = dp(context, 12);
        card.setPadding(0, paddingPx, paddingPx, paddingPx);

        // Setup Callout Colors
        TypedValue tv = new TypedValue();
        int accentColor = Color.GREEN;
        String prefix = "> NOTE";

        if (type == XnetCalloutCard.CalloutType.WARNING) {
            accentColor = androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_orange_accent_primary);
            prefix = "> WARNING";
        } else if (type == XnetCalloutCard.CalloutType.ERROR) {
            accentColor = androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_rgb_accent_primary);
            prefix = "> ERROR";
        } else {
            if (context.getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
                accentColor = tv.data;
            }
        }

        // Resolve surface color to act as a dark solid base
        int surfaceColor = androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_classic_dark_surface_raised);
        if (context.getTheme().resolveAttribute(R.attr.xnetSurfaceRaised, tv, true)) {
            surfaceColor = tv.data;
        }

        // Blend 12% accent color into the surface color for a subtle themed tint
        int blendedBg = ColorUtils.blendARGB(surfaceColor, accentColor, 0.12f);
        // Apply 92% opacity (alpha 235 out of 255) so it's mostly solid but slightly see-through
        int finalBgColor = ColorUtils.setAlphaComponent(blendedBg, 235);

        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(finalBgColor);
        bgDrawable.setCornerRadius(dp(context, 6));
        // Add a very subtle stroke of the accent color to define the edges better against busy backgrounds
        bgDrawable.setStroke(dp(context, 1), ColorUtils.setAlphaComponent(accentColor, 80));
        card.setBackground(bgDrawable);

        // Left border strip
        View leftBorder = new View(context);
        LinearLayout.LayoutParams borderParams = new LinearLayout.LayoutParams(dp(context, 3), LinearLayout.LayoutParams.MATCH_PARENT);
        borderParams.setMarginEnd(dp(context, 14));
        leftBorder.setLayoutParams(borderParams);
        leftBorder.setBackgroundColor(accentColor);
        card.addView(leftBorder);

        // Text container
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(textContainer);

        // Prefix label
        TextView tvPrefix = new TextView(context);
        tvPrefix.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        tvPrefix.setLetterSpacing(0.18f);
        tvPrefix.setTextColor(accentColor);
        tvPrefix.setText(prefix);
        LinearLayout.LayoutParams prefixParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        prefixParams.bottomMargin = dp(context, 5);
        tvPrefix.setLayoutParams(prefixParams);
        textContainer.addView(tvPrefix);

        // Typing text
        XnetTypingTextView tvMessage = new XnetTypingTextView(context);
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tvMessage.setLineSpacing(0f, 1.6f);
        int textColor = Color.WHITE;
        if (context.getTheme().resolveAttribute(R.attr.xnetTextPrimary, tv, true)) {
            textColor = tv.data;
        }
        tvMessage.setTextColor(textColor);
        tvMessage.setLoop(false); // Stop typing after first pass
        textContainer.addView(tvMessage);

        // Container wrapper for padding/margins at bottom of screen
        final FrameLayout wrapper = new FrameLayout(context);
        FrameLayout.LayoutParams wrapParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        wrapParams.gravity = Gravity.BOTTOM;
        // Avoid bottom navigation bar
        wrapParams.setMargins(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 80)); 
        wrapper.setLayoutParams(wrapParams);
        wrapper.addView(card);

        root.addView(wrapper);

        // Animate In from bottom
        TranslateAnimation animateIn = new TranslateAnimation(0, 0, 300, 0);
        animateIn.setDuration(400);
        wrapper.startAnimation(animateIn);

        // When typing finishes, wait 1.2s then dismiss automatically
        tvMessage.setOnTypingCompleteListener(new XnetTypingTextView.OnTypingCompleteListener() {
            @Override
            public void onTypingComplete() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (wrapper.getParent() == null) return;
                        TranslateAnimation animateOut = new TranslateAnimation(0, 0, 0, wrapper.getHeight() + dp(context, 100));
                        animateOut.setDuration(300);
                        animateOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}
                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                root.removeView(wrapper);
                            }
                        });
                        wrapper.startAnimation(animateOut);
                    }
                }, 1200); // Wait time after typing completes
            }
        });

        // Start typing! (30ms per character for slightly faster speed)
        tvMessage.setTypingText(message, 30L);
    }

    private static int dp(Context ctx, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }
}
