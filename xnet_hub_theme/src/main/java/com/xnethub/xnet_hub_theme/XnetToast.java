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
        card.setPadding(dp(context, 17), paddingPx, paddingPx, paddingPx);

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

        // Create 3-color transparent gradient based on accentColor and surfaceColor
        int color1 = ColorUtils.blendARGB(surfaceColor, accentColor, 0.15f);
        int color2 = ColorUtils.blendARGB(surfaceColor, accentColor, 0.40f);
        int color3 = ColorUtils.blendARGB(surfaceColor, accentColor, 0.70f);

        color1 = ColorUtils.setAlphaComponent(color1, 230); // 90% alpha
        color2 = ColorUtils.setAlphaComponent(color2, 180); // 70% alpha
        color3 = ColorUtils.setAlphaComponent(color3, 130); // 50% alpha

        int[] gradientColors = new int[] { color1, color2, color3 };
        int strokeColor = ColorUtils.setAlphaComponent(accentColor, 80);
        
        CyberToastBackgroundDrawable bgDrawable = new CyberToastBackgroundDrawable(
                gradientColors, dp(context, 12), strokeColor, dp(context, 1), accentColor, dp(context, 3)
        );
        card.setBackground(bgDrawable);

        // Left border is now drawn by CyberToastBackgroundDrawable

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

    private static class CyberToastBackgroundDrawable extends android.graphics.drawable.Drawable {
        private final android.graphics.Paint paint;
        private final android.graphics.Path path;
        private final int[] colors;
        private final float cutSizePx;
        private final android.graphics.Paint strokePaint;
        private final float strokeWidthPx;

        private final int leftBorderColor;
        private final float leftBorderWidthPx;

        public CyberToastBackgroundDrawable(int[] gradientColors, float cutSizePx, int strokeColor, float strokeWidthPx, int leftBorderColor, float leftBorderWidthPx) {
            this.colors = gradientColors;
            this.cutSizePx = cutSizePx;
            this.strokeWidthPx = strokeWidthPx;
            this.leftBorderColor = leftBorderColor;
            this.leftBorderWidthPx = leftBorderWidthPx;
            
            paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            path = new android.graphics.Path();
            
            strokePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            strokePaint.setStyle(android.graphics.Paint.Style.STROKE);
            strokePaint.setStrokeWidth(strokeWidthPx);
            strokePaint.setColor(strokeColor);
        }

        @Override
        protected void onBoundsChange(android.graphics.Rect bounds) {
            super.onBoundsChange(bounds);
            
            android.graphics.LinearGradient gradient = new android.graphics.LinearGradient(
                bounds.left, bounds.top, bounds.right, bounds.bottom,
                colors, null, android.graphics.Shader.TileMode.CLAMP
            );
            paint.setShader(gradient);
            
            float w = bounds.width();
            float h = bounds.height();
            
            path.reset();
            // Start top-left (normal)
            path.moveTo(0, 0);
            // Go to top-right minus cut
            path.lineTo(w - cutSizePx, 0);
            // Cut to top-right down
            path.lineTo(w, cutSizePx);
            // Go to bottom-right (normal)
            path.lineTo(w, h);
            // Go to bottom-left plus cut
            path.lineTo(cutSizePx, h);
            // Cut to bottom-left up
            path.lineTo(0, h - cutSizePx);
            // Close back to top-left
            path.close();
        }

        
        @Override
        public void draw(@androidx.annotation.NonNull android.graphics.Canvas canvas) {
            canvas.drawPath(path, paint);
            if (strokeWidthPx > 0) {
                canvas.drawPath(path, strokePaint);
            }
            if (leftBorderWidthPx > 0) {
                android.graphics.Paint borderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
                borderPaint.setColor(leftBorderColor);
                borderPaint.setStyle(android.graphics.Paint.Style.FILL);
                
                android.graphics.Path borderPath = new android.graphics.Path();
                borderPath.moveTo(0, 0);
                borderPath.lineTo(leftBorderWidthPx, 0);
                
                // Bottom is cut, so we calculate the intersection
                // The bottom-left cut goes from (0, h - cutSize) to (cutSize, h)
                // Equation of cut line: y - (h - cutSize) = (x - 0) * (cutSize) / (cutSize)
                // y = x + h - cutSize
                // So at x = leftBorderWidthPx, y = leftBorderWidthPx + h - cutSize
                float h = getBounds().height();
                
                borderPath.lineTo(leftBorderWidthPx, leftBorderWidthPx + h - cutSizePx);
                borderPath.lineTo(0, h - cutSizePx);
                borderPath.close();
                
                canvas.drawPath(borderPath, borderPaint);
            }
        }
@Override
        public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override
        public void setColorFilter(@androidx.annotation.Nullable android.graphics.ColorFilter colorFilter) { paint.setColorFilter(colorFilter); }
        @Override
        public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }
}
