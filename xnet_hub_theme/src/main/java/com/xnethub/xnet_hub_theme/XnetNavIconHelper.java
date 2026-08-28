package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import androidx.core.content.res.ResourcesCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * XnetNavIconHelper
 *
 * Utility for converting any image (URL, Drawable resource, or raw Bitmap)
 * into a styled nav-bar icon Drawable that matches XnetImageView's appearance:
 *
 *   X-Cyber themes → hexagon clip with neon accent stroke
 *   Classic themes → circle clip with stroke
 *
 * This allows bottom nav icons to use the same visual style as XnetImageView
 * without replacing Material's internal ImageView hierarchy.
 *
 * Usage (in XnetBottomNavigationView or any Activity / Fragment):
 *
 *   // From URL (async):
 *   XnetNavIconHelper.loadFromUrl(context, url, size, menuItem::setIcon);
 *
 *   // From drawable resource (sync):
 *   Drawable d = XnetNavIconHelper.fromDrawable(context, R.drawable.ic_home, size);
 *   menuItem.setIcon(d);
 *
 *   // From Bitmap (sync):
 *   Drawable d = XnetNavIconHelper.fromBitmap(context, bitmap, size);
 *   menuItem.setIcon(d);
 */
public final class XnetNavIconHelper {

    /** Result callback for async loading. Called on the main thread. */
    public interface IconCallback {
        void onIconReady(Drawable drawable);
    }

    // -----------------------------------------------------------------------
    // Async URL loader
    // -----------------------------------------------------------------------

    /**
     * Downloads an image from {@code imageUrl} in a background thread,
     * applies the XnetImageView-style clip + stroke and delivers the result
     * to {@code callback} on the main thread.
     *
     * @param context    Activity or application context.
     * @param imageUrl   Full HTTP/HTTPS URL to the image (Firebase Storage, CDN, …).
     * @param sizePx     Desired icon size in pixels (e.g. 96px for a 48dp icon at 2× density).
     * @param callback   Called on the main thread with the ready {@link Drawable}.
     */
    public static void loadFromUrl(Context context, String imageUrl,
                                   int sizePx, IconCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Bitmap raw = downloadBitmap(imageUrl);
            if (raw == null) return;

            Drawable icon = fromBitmap(context, raw, sizePx);
            mainHandler.post(() -> callback.onIconReady(icon));
        });
        executor.shutdown();
    }

    // -----------------------------------------------------------------------
    // Sync helpers
    // -----------------------------------------------------------------------

    /**
     * Converts a vector / PNG drawable resource into a styled nav icon.
     * For non-photo icons (vectors, PNGs) this simply clips the drawable
     * into hexagon / circle with the themed stroke — without destroying the
     * original colours (no ColorFilter applied here).
     *
     * @param context  Context to resolve theme attributes.
     * @param drawableRes Drawable resource id (e.g. {@code R.drawable.ic_home}).
     * @param sizePx   Target icon size in pixels.
     * @return Styled {@link BitmapDrawable}, never null.
     */
    public static Drawable fromDrawableRes(Context context, int drawableRes, int sizePx) {
        Drawable src = ResourcesCompat.getDrawable(context.getResources(), drawableRes, context.getTheme());
        if (src == null) return new BitmapDrawable(context.getResources(),
                Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888));

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        src.setBounds(0, 0, sizePx, sizePx);
        src.draw(canvas);
        return fromBitmap(context, bitmap, sizePx);
    }

    /**
     * Converts any {@link Bitmap} into a styled nav icon that matches
     * {@link XnetImageView} — hexagon (Cyber) or circle (Classic),
     * with an outer accent stroke.
     *
     * @param context Context to resolve theme attributes.
     * @param src     Source bitmap (any size — will be scaled to {@code sizePx}).
     * @param sizePx  Target output size in pixels (square).
     * @return Styled {@link BitmapDrawable} ready for use as a menu item icon.
     */
    public static Drawable fromBitmap(Context context, Bitmap src, int sizePx) {
        boolean isCyber = resolveBoolean(context, R.attr.xnetIsCyberTheme);
        int accentColor = resolveColor(context, R.attr.xnetAccentPrimary, Color.GRAY);
        int strokeColor = isCyber ? accentColor
                : resolveColor(context, R.attr.xnetStroke, Color.GRAY);

        float density = context.getResources().getDisplayMetrics().density;
        float strokeWidth = 1.5f * density;

        // Geometry — mirrors XnetImageView
        float cx = sizePx / 2f;
        float cy = sizePx / 2f;
        float maxRadius  = sizePx / 2f;
        float strokeMargin = 3f * density;
        float outerRadius  = maxRadius - strokeMargin;
        float gap          = 2.5f * density;
        float innerRadius  = outerRadius - gap;

        // Build clip path (inner) and stroke path (outer)
        Path innerPath = new Path();
        Path outerPath = new Path();
        if (isCyber) {
            buildHexagon(innerPath, cx, cy, innerRadius);
            buildHexagon(outerPath, cx, cy, outerRadius);
        } else {
            innerPath.addCircle(cx, cy, innerRadius, Path.Direction.CW);
            outerPath.addCircle(cx, cy, outerRadius, Path.Direction.CW);
        }

        // Scale source bitmap to sizePx × sizePx
        Bitmap scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true);

        // Output bitmap
        Bitmap out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);

        // 1. Clip to inner shape and draw the photo — same as XnetImageView.onDraw()
        canvas.save();
        canvas.clipPath(innerPath);
        canvas.drawBitmap(scaled, 0f, 0f, null);
        canvas.restore();

        // 2. Draw the themed outer stroke on top
        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);
        strokePaint.setColor(strokeColor);
        canvas.drawPath(outerPath, strokePaint);

        if (scaled != src) scaled.recycle();

        return new BitmapDrawable(context.getResources(), out) {
            @Override
            public void setTintList(@androidx.annotation.Nullable android.content.res.ColorStateList tint) {
                // Ignore — keeps photo in original colours and prevents Material BottomNavigationView
                // from overwriting the photo with a solid tint list.
            }

            @Override
            public void setTint(int tintColor) {
                // Ignore
            }

            @Override
            public void setColorFilter(android.graphics.ColorFilter colorFilter) {
                // Ignore — prevents TintAwareDrawable wrappers from solid-colouring the profile image
                // when used in Classic Material BottomNavigationView mode.
            }
        };
    }

    // -----------------------------------------------------------------------
    // Private geometry helpers
    // -----------------------------------------------------------------------

    /** Pointy-topped regular hexagon path — mirrors XnetImageView.buildHexagonPath(). */
    private static void buildHexagon(Path path, float cx, float cy, float radius) {
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60 - 90);
            float x = (float) (cx + radius * Math.cos(angle));
            float y = (float) (cy + radius * Math.sin(angle));
            if (i == 0) path.moveTo(x, y);
            else        path.lineTo(x, y);
        }
        path.close();
    }

    // -----------------------------------------------------------------------
    // Private network + theme helpers
    // -----------------------------------------------------------------------

    /** Downloads a Bitmap from a URL. Returns {@code null} on failure. */
    static Bitmap downloadBitmap(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            try (InputStream is = conn.getInputStream()) {
                return BitmapFactory.decodeStream(is);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static boolean resolveBoolean(Context context, int attr) {
        TypedValue tv = new TypedValue();
        return context.getTheme().resolveAttribute(attr, tv, true) && tv.data != 0;
    }

    private static int resolveColor(Context context, int attr, int fallback) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, tv, true)) return tv.data;
        return fallback;
    }

    private XnetNavIconHelper() {} // static utility class — no instantiation
}
