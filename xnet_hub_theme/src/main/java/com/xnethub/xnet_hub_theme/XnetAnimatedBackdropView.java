package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

public class XnetAnimatedBackdropView extends View {

    private static final int STAR_COUNT = 42;
    private static final int METEOR_COUNT = 6;

    // Cyber RGB Palette Colors (Neon Green, Cyan Blue, Amber Orange, Electric Magenta)
    private final int[] RGB_PALETTE;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridAccentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint electricPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint electricGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint meteorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Star[] stars = new Star[STAR_COUNT];
    private final Meteor[] meteors = new Meteor[METEOR_COUNT];
    private final Random random = new Random(41L);
    private final Path hexPath = new Path();
    private final Path electricPath = new Path();
    private final Path meteorPath = new Path();

    private long startTimeMs;

    public XnetAnimatedBackdropView(@NonNull Context context) {
        this(context, null);
    }

    public XnetAnimatedBackdropView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetAnimatedBackdropView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        // Fetching colors dynamically from Token Generated XMLs
        RGB_PALETTE = new int[] {
                androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_green_accent_primary),
                androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_blue_accent_primary),
                androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_orange_accent_primary),
                androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_rgb_accent_primary)
        };

        initializeParticles();
        startTimeMs = System.currentTimeMillis();
    }

    private void initializeParticles() {
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i] = new Star(random.nextFloat(), random.nextFloat(), 1.2f + random.nextFloat() * 2.8f, random.nextFloat() * 0.9f);
        }
        for (int i = 0; i < METEOR_COUNT; i++) {
            meteors[i] = new Meteor(
                    random.nextFloat(),
                    random.nextFloat() * 0.65f,
                    0.08f + random.nextFloat() * 0.18f,
                    random.nextFloat() * 5000f
            );
        }
    }

    private final int[] screenLocation = new int[2];

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // Resolve theme settings
        boolean enableAnimation = resolveThemeBoolean(R.attr.xnetEnableAnimatedBackdrop);
        int backgroundBase = resolveThemeColor(R.attr.xnetBackground);

        // If animation is disabled (Normal Dark / Light themes), draw solid background and STOP animation loop!
        if (!enableAnimation) {
            canvas.drawColor(backgroundBase);
            return;
        }

        boolean isCyberRGB = XnetThemeManager.THEME_CYBER_RGB.equals(XnetThemeManager.getTheme(getContext()));

        long elapsed = System.currentTimeMillis() - startTimeMs;
        int backgroundAlt = resolveThemeColor(R.attr.xnetBackgroundAlt);
        int backgroundDeep = resolveThemeColor(R.attr.xnetBackgroundDeep);
        int accentPrimary = resolveThemeColor(R.attr.xnetAccentPrimary);
        int accentHighlight = resolveThemeColor(R.attr.xnetAccentHighlight);
        int gridColor = resolveThemeColor(R.attr.xnetStroke);

        // Determine window screen coordinates
        getLocationOnScreen(screenLocation);
        int offsetX = screenLocation[0];
        int offsetY = screenLocation[1];

        // Resolve absolute display dimensions
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // Ensure we cover the full drawing space safely
        int drawWidth = Math.max(screenWidth, width + offsetX);
        int drawHeight = Math.max(screenHeight, height + offsetY);

        // Save canvas state and apply coordinate translation
        canvas.save();
        canvas.translate(-offsetX, -offsetY);

        fillPaint.setShader(new LinearGradient(
                0f,
                0f,
                0f,
                drawHeight,
                new int[]{backgroundBase, backgroundAlt, backgroundDeep},
                new float[]{0f, 0.46f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0f, 0f, drawWidth, drawHeight, fillPaint);

        drawNebula(canvas, drawWidth, drawHeight, accentPrimary, accentHighlight, elapsed, isCyberRGB);
        drawGrid(canvas, drawWidth, drawHeight, gridColor);
        drawElectricArcs(canvas, drawWidth, drawHeight, accentPrimary, accentHighlight, elapsed, isCyberRGB);
        drawStars(canvas, drawWidth, drawHeight, accentPrimary, accentHighlight, elapsed, isCyberRGB);
        drawMeteors(canvas, drawWidth, drawHeight, accentPrimary, accentHighlight, elapsed, isCyberRGB);
        drawScanLines(canvas, drawWidth, drawHeight);

        // Restore canvas state to prevent side effects
        canvas.restore();

        postInvalidateOnAnimation();
    }

    private boolean resolveThemeBoolean(int attrResId) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data != 0;
        }
        return false;
    }

    private void drawNebula(Canvas canvas, int width, int height, int accentPrimary, int accentHighlight, long elapsed, boolean isCyberRGB) {
        float pulse = 0.92f + (((elapsed % 4000L) / 4000f) * 0.18f);

        int leftOrbColor = isCyberRGB ? RGB_PALETTE[0] : accentPrimary;  // Neon Green in CyberRGB
        int rightOrbColor = isCyberRGB ? RGB_PALETTE[1] : accentHighlight; // Cyan Blue in CyberRGB
        int bottomOrbColor = isCyberRGB ? RGB_PALETTE[2] : accentPrimary; // Amber Orange in CyberRGB

        // Left Top Nebula Orb
        fillPaint.setShader(new RadialGradient(
                width * 0.18f,
                height * 0.08f,
                width * 0.48f * pulse,
                new int[]{withAlpha(leftOrbColor, 48), Color.TRANSPARENT},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0f, 0f, width, height, fillPaint);

        // Right Top Nebula Orb
        fillPaint.setShader(new RadialGradient(
                width * 0.82f,
                height * 0.12f,
                width * 0.42f,
                new int[]{withAlpha(rightOrbColor, 38), Color.TRANSPARENT},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0f, 0f, width, height, fillPaint);

        // Bottom Center Nebula Ambient Glow (Extra RGB layer in Cyber RGB)
        if (isCyberRGB) {
            fillPaint.setShader(new RadialGradient(
                    width * 0.5f,
                    height * 0.85f,
                    width * 0.55f,
                    new int[]{withAlpha(bottomOrbColor, 32), Color.TRANSPARENT},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0f, 0f, width, height, fillPaint);
        }
    }

    private void drawGrid(Canvas canvas, int width, int height, int gridColor) {
        float sideLength = dp(28f);
        float hexWidth = (float) (Math.sqrt(3d) * sideLength);
        float rowStep = sideLength * 1.5f;
        float halfWidth = hexWidth / 2f;

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPaint.setStrokeCap(Paint.Cap.ROUND);
        gridAccentPaint.setStyle(Paint.Style.STROKE);
        gridAccentPaint.setStrokeJoin(Paint.Join.ROUND);
        gridAccentPaint.setStrokeCap(Paint.Cap.ROUND);

        int rowIndex = 0;
        for (float centerY = -sideLength; centerY < height + (sideLength * 2f); centerY += rowStep) {
            float rowOffset = (rowIndex % 2 == 0) ? 0f : halfWidth;
            int columnIndex = 0;
            for (float centerX = -hexWidth; centerX < width + hexWidth; centerX += hexWidth) {
                float actualCenterX = centerX + rowOffset;
                boolean thickStroke = ((rowIndex + columnIndex) % 2) == 1;
                if (thickStroke) {
                    gridPaint.setColor(withAlpha(gridColor, 28));
                    gridPaint.setStrokeWidth(dp(1.55f));
                    drawHex(canvas, actualCenterX, centerY, sideLength, gridPaint);
                } else {
                    gridPaint.setColor(withAlpha(gridColor, 18));
                    gridPaint.setStrokeWidth(dp(0.8f));
                    drawHex(canvas, actualCenterX, centerY, sideLength, gridPaint);

                    gridAccentPaint.setColor(withAlpha(gridColor, 12));
                    gridAccentPaint.setStrokeWidth(dp(0.9f));
                    drawHex(canvas, actualCenterX, centerY, sideLength - dp(4.2f), gridAccentPaint);
                }
                columnIndex++;
            }
            rowIndex++;
        }
    }

    private void drawElectricArcs(
            Canvas canvas,
            int width,
            int height,
            int accentPrimary,
            int accentHighlight,
            long elapsed,
            boolean isCyberRGB
    ) {
        electricPaint.setStyle(Paint.Style.STROKE);
        electricPaint.setStrokeCap(Paint.Cap.ROUND);
        electricPaint.setStrokeJoin(Paint.Join.ROUND);
        electricGlowPaint.setStyle(Paint.Style.STROKE);
        electricGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        electricGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        for (int index = 0; index < 4; index++) {
            long durationMs = 3600L + (index * 700L);
            long cycle = (elapsed + (index * 1100L)) / durationMs;
            float phase = ((elapsed + (index * 1100L)) % durationMs) / (float) durationMs;
            float reveal = (phase - 0.1f) / 0.18f;
            if (reveal < 0f || reveal > 1f) {
                continue;
            }
            float intensity = 1f - Math.abs((reveal * 2f) - 1f);

            int electricColor;
            if (isCyberRGB) {
                electricColor = RGB_PALETTE[index % RGB_PALETTE.length]; // Dynamic Tri-color Cyber Arcs!
            } else {
                electricColor = index % 2 == 0 ? accentHighlight : accentPrimary;
            }

            float startX = width * (0.1f + (noise(cycle + (index * 17L)) * 0.8f));
            float startY = height * (0.08f + (noise(cycle + (index * 29L) + 3L) * 0.7f));
            float endX;
            float endY;
            int directionMode = (int) ((cycle + index) % 3L);
            if (directionMode == 0) {
                endX = startX + (width * (0.12f + (noise(cycle + 41L + index) * 0.2f)));
                endY = startY + (height * (0.14f + (noise(cycle + 59L + index) * 0.22f)));
            } else if (directionMode == 1) {
                endX = startX - (width * (0.08f + (noise(cycle + 73L + index) * 0.14f)));
                endY = startY + (height * (0.16f + (noise(cycle + 97L + index) * 0.26f)));
            } else {
                endX = startX + (width * (0.16f + (noise(cycle + 121L + index) * 0.18f)));
                endY = startY - (height * (0.1f + (noise(cycle + 149L + index) * 0.16f)));
            }
            endX = clamp(endX, dp(18f), width - dp(18f));
            endY = clamp(endY, dp(18f), height - dp(18f));

            electricPath.reset();
            electricPath.moveTo(startX, startY);
            int segments = 5 + index;
            float perpendicularX = endY - startY;
            float perpendicularY = -(endX - startX);
            float length = (float) Math.hypot(perpendicularX, perpendicularY);
            if (length > 0f) {
                perpendicularX /= length;
                perpendicularY /= length;
            }
            for (int step = 1; step < segments; step++) {
                float t = step / (float) segments;
                float baseX = lerp(startX, endX, t);
                float baseY = lerp(startY, endY, t);
                float variance = (0.5f - noise(cycle + (index * 211L) + step)) * dp(18f) * intensity;
                electricPath.lineTo(
                        baseX + (perpendicularX * variance),
                        baseY + (perpendicularY * variance)
                );
            }
            electricPath.lineTo(endX, endY);

            electricGlowPaint.setColor(withAlpha(electricColor, (int) (18 * intensity)));
            electricGlowPaint.setStrokeWidth(dp(3.4f));
            canvas.drawPath(electricPath, electricGlowPaint);

            electricPaint.setColor(withAlpha(electricColor, (int) (54 * intensity)));
            electricPaint.setStrokeWidth(dp(1.05f));
            canvas.drawPath(electricPath, electricPaint);
        }
    }

    private void drawHex(Canvas canvas, float centerX, float centerY, float sideLength, Paint paint) {
        if (sideLength <= 0f) {
            return;
        }
        float halfWidth = (float) (Math.sqrt(3d) * sideLength / 2d);
        hexPath.reset();
        hexPath.moveTo(centerX, centerY - sideLength);
        hexPath.lineTo(centerX + halfWidth, centerY - (sideLength / 2f));
        hexPath.lineTo(centerX + halfWidth, centerY + (sideLength / 2f));
        hexPath.lineTo(centerX, centerY + sideLength);
        hexPath.lineTo(centerX - halfWidth, centerY + (sideLength / 2f));
        hexPath.lineTo(centerX - halfWidth, centerY - (sideLength / 2f));
        hexPath.close();
        canvas.drawPath(hexPath, paint);
    }

    private void drawStars(Canvas canvas, int width, int height, int accentPrimary, int accentHighlight, long elapsed, boolean isCyberRGB) {
        for (int i = 0; i < stars.length; i++) {
            Star star = stars[i];
            float twinkle = 0.35f + (float) Math.abs(Math.sin((elapsed / 720f) + star.phase)) * 0.65f;
            
            int starColor;
            if (isCyberRGB) {
                starColor = RGB_PALETTE[i % RGB_PALETTE.length]; // Dynamic RGB particle stars!
            } else {
                starColor = i % 4 == 0 ? accentHighlight : accentPrimary;
            }

            starPaint.setColor(withAlpha(starColor, (int) (68 * twinkle)));
            canvas.drawCircle(star.xRatio * width, star.yRatio * height, dp(star.radiusDp) * twinkle, starPaint);
        }
    }

    private void drawMeteors(Canvas canvas, int width, int height, int accentPrimary, int accentHighlight, long elapsed, boolean isCyberRGB) {
        for (int i = 0; i < meteors.length; i++) {
            Meteor meteor = meteors[i];
            float durationMs = 3800f + (meteor.speedFactor * 1200f);
            float progress = ((elapsed + (long) meteor.offsetMs) % (long) durationMs) / durationMs;
            float startX = (meteor.startXRatio * width) - (progress * width * 0.25f);
            float startY = meteor.startYRatio * height;
            float travelX = width * 1.2f;
            float travelY = height * 0.85f;
            float endX = startX + (travelX * progress);
            float endY = startY + (travelY * progress);
            
            int meteorColor;
            if (isCyberRGB) {
                meteorColor = RGB_PALETTE[i % RGB_PALETTE.length]; // Dynamic RGB shooting meteors!
            } else {
                meteorColor = i % 2 == 0 ? accentPrimary : accentHighlight;
            }

            int alpha = (int) (130 * (1f - Math.abs((progress * 2f) - 1f)));
            if (alpha <= 6) {
                continue;
            }
            meteorPaint.setShader(new LinearGradient(
                    endX,
                    endY,
                    endX - dp(56f),
                    endY - dp(36f),
                    new int[]{withAlpha(meteorColor, alpha), Color.TRANSPARENT},
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            ));
            meteorPath.reset();
            meteorPath.moveTo(endX, endY);
            meteorPath.lineTo(endX - dp(58f), endY - dp(36f));
            meteorPath.lineTo(endX - dp(44f), endY - dp(22f));
            meteorPath.close();
            canvas.drawPath(meteorPath, meteorPaint);
            meteorPaint.setShader(null);
        }
    }

    private void drawScanLines(Canvas canvas, int width, int height) {
        for (int y = 0; y < height; y += dpInt(4f)) {
            scanPaint.setColor(Color.argb(12, 0, 0, 0));
            canvas.drawRect(0f, y, width, y + dp(2f), scanPaint);
        }
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(Math.max(0, Math.min(255, alpha)), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int resolveThemeColor(int attrResId) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        if (getContext().getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return Color.TRANSPARENT;
    }

    private float noise(long seed) {
        double value = Math.sin((seed * 12.9898d) + 78.233d) * 43758.5453d;
        return (float) (value - Math.floor(value));
    }

    private float lerp(float start, float end, float t) {
        return start + ((end - start) * t);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private int dpInt(float value) {
        return Math.round(dp(value));
    }

    private static class Star {
        final float xRatio;
        final float yRatio;
        final float radiusDp;
        final float phase;

        Star(float xRatio, float yRatio, float radiusDp, float phase) {
            this.xRatio = xRatio;
            this.yRatio = yRatio;
            this.radiusDp = radiusDp;
            this.phase = phase;
        }
    }

    private static class Meteor {
        final float startXRatio;
        final float startYRatio;
        final float speedFactor;
        final float offsetMs;

        Meteor(float startXRatio, float startYRatio, float speedFactor, float offsetMs) {
            this.startXRatio = startXRatio;
            this.startYRatio = startYRatio;
            this.speedFactor = speedFactor;
            this.offsetMs = offsetMs;
        }
    }
}
