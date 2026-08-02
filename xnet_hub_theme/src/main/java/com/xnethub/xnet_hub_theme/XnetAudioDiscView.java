package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * XnetAudioDiscView
 *
 * A complete turntable/disc widget matching DriveX's audio UI.
 * Automatically reads the host app's label and launcher icon so
 * any app embedding this library gets its own branding on the disc.
 *
 * Layer structure (bottom → top, exact DriveX z-order):
 *   1. Artwork card      — album art or placeholder gradient (220dp circle)
 *   2. BrandingView      — arc text: host app label (top) + "· XNET AUDIO ·" (bottom)
 *   3. Overlay gradient  — depth / glossy sheen over artwork
 *   4. Brand chip card   — 92dp card containing app launcher icon in middle
 *   5. Center hole       — 38dp view ON TOP of brand chip (dark ring + 14dp spindle hole)
 *   6. Outer stroke      — thin accent ring around full disc edge
 */
public final class XnetAudioDiscView extends FrameLayout {

    // Layer 1 – artwork
    private MaterialCardView artworkCard;
    private ImageView imgArtwork;

    // Layer 2 – branding arc text
    private XnetAudioDiscBrandingView brandingView;

    // Layer 3 – overlay gradient
    private android.view.View overlayView;

    // Layer 4 – brand icon chip card (92dp)
    private MaterialCardView brandChipCard;
    private ImageView imgBrandIcon;

    // Layer 5 – center hole (38dp, ON TOP of brand chip to punch spindle hole through icon)
    private android.view.View centerHoleView;

    // Layer 6 – outer stroke ring
    private android.view.View outerStrokeView;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    public XnetAudioDiscView(@NonNull Context context) {
        super(context);
        init();
    }

    public XnetAudioDiscView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XnetAudioDiscView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Init (Layer z-ordering strictly matches DriveX's activity_audio_viewer.xml)
    // ─────────────────────────────────────────────────────────────────────────

    private void init() {
        setClipChildren(false);

        ShapeAppearanceModel circleShape = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, 10000f)
                .build();

        // ── Layer 1: Artwork card (full circle) ───────────────────────────────
        artworkCard = new MaterialCardView(getContext());
        FrameLayout.LayoutParams fullParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        artworkCard.setLayoutParams(fullParams);
        artworkCard.setShapeAppearanceModel(circleShape);
        artworkCard.setCardElevation(0f);
        artworkCard.setStrokeWidth(0);
        artworkCard.setClipToOutline(true);

        imgArtwork = new ImageView(getContext());
        imgArtwork.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        imgArtwork.setScaleType(ImageView.ScaleType.CENTER_CROP);
        artworkCard.addView(imgArtwork);
        addView(artworkCard);

        // ── Layer 2: Branding arc text ────────────────────────────────────────
        brandingView = new XnetAudioDiscBrandingView(getContext());
        brandingView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        brandingView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(brandingView);

        // ── Layer 3: Overlay gradient ─────────────────────────────────────────
        overlayView = new android.view.View(getContext());
        overlayView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        overlayView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(overlayView);

        // ── Layer 4: Brand chip card (92dp) ──────────────────────────────────
        int chipSizePx = dpInt(92f);
        brandChipCard = new MaterialCardView(getContext());
        FrameLayout.LayoutParams chipParams = new FrameLayout.LayoutParams(chipSizePx, chipSizePx);
        chipParams.gravity = android.view.Gravity.CENTER;
        brandChipCard.setLayoutParams(chipParams);
        brandChipCard.setShapeAppearanceModel(circleShape);
        brandChipCard.setCardElevation(0f);
        brandChipCard.setStrokeWidth(0);
        brandChipCard.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
        brandChipCard.setClipToOutline(true);

        imgBrandIcon = new ImageView(getContext());
        imgBrandIcon.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        imgBrandIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imgBrandIcon.setPadding(dpInt(10f), dpInt(10f), dpInt(10f), dpInt(10f));
        brandChipCard.addView(imgBrandIcon);
        addView(brandChipCard);

        // ── Layer 5: Center hole (38dp) — PLACED ON TOP OF BRAND CHIP! ────────
        int holeSizePx = dpInt(38f);
        FrameLayout.LayoutParams holeParams = new FrameLayout.LayoutParams(holeSizePx, holeSizePx);
        holeParams.gravity = android.view.Gravity.CENTER;
        centerHoleView = new android.view.View(getContext());
        centerHoleView.setLayoutParams(holeParams);
        addView(centerHoleView);

        // ── Layer 6: Outer stroke ring ────────────────────────────────────────
        outerStrokeView = new android.view.View(getContext());
        outerStrokeView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        outerStrokeView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(outerStrokeView);

        // Auto-load the host app's launcher icon
        autoLoadAppIcon();

        refreshPalette();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshPalette();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /** Set or clear the album artwork. Pass null to show the placeholder gradient. */
    public void setArtworkBitmap(@Nullable Bitmap bitmap) {
        if (bitmap != null) {
            imgArtwork.setImageBitmap(bitmap);
            imgArtwork.setBackground(null);
        } else {
            imgArtwork.setImageDrawable(null);
            imgArtwork.setBackground(buildArtworkPlaceholderDrawable());
        }
    }

    /** Override the center chip icon (replaces the auto-loaded launcher icon). */
    public void setBrandIcon(@DrawableRes int drawableRes) {
        imgBrandIcon.setImageResource(drawableRes);
        imgBrandIcon.setImageTintList(null);
    }

    /** Override the top arc text (default = auto-resolved app name). */
    public void setDiscTopText(@Nullable String text) {
        if (brandingView != null) {
            brandingView.setTopText(text);
        }
    }

    /** Re-apply all theme colours after a theme switch. */
    public void refreshPalette() {
        int surfaceRaised = resolveColor(R.attr.xnetSurfaceRaised);
        artworkCard.setCardBackgroundColor(withAlpha(surfaceRaised, 214));

        overlayView.setBackground(buildOverlayDrawable());

        // Brand chip background
        imgBrandIcon.setBackground(buildBrandChipDrawable());
        imgBrandIcon.setImageTintList(null); // preserve original icon colours

        // Center hole — DriveX exact implementation (outer dark oval + inner solid hole)
        centerHoleView.setBackground(buildCenterHoleDrawable());

        // Outer stroke
        outerStrokeView.setBackground(buildOuterStrokeDrawable());

        if (brandingView != null) {
            brandingView.refreshPalette();
        }

        // Artwork placeholder if no bitmap set
        if (imgArtwork.getDrawable() == null) {
            imgArtwork.setBackground(buildArtworkPlaceholderDrawable());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auto launcher icon
    // ─────────────────────────────────────────────────────────────────────────

    private void autoLoadAppIcon() {
        try {
            PackageManager pm = getContext().getPackageManager();
            String pkgName = getContext().getPackageName();
            Drawable icon = pm.getApplicationIcon(pkgName);
            imgBrandIcon.setImageDrawable(icon);
        } catch (Exception e) {
            try {
                int iconRes = getContext().getApplicationInfo().icon;
                if (iconRes != 0) {
                    imgBrandIcon.setImageResource(iconRes);
                } else {
                    imgBrandIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } catch (Exception ignored) {
                imgBrandIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }
        imgBrandIcon.setImageTintList(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Drawable builders — exact DriveX AudioFileViewerActivity logic
    // ─────────────────────────────────────────────────────────────────────────

    @NonNull
    private Drawable buildArtworkPlaceholderDrawable() {
        int surfaceRaised   = resolveColor(R.attr.xnetSurfaceRaised);
        int backgroundBase  = resolveColor(R.attr.xnetBackground);
        int accentPrimary   = resolveColor(R.attr.xnetAccentPrimary);
        int accentHighlight = resolveColor(R.attr.xnetAccentHighlight);

        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.OVAL);
        base.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        base.setGradientRadius(dp(196f));
        base.setColors(new int[]{withAlpha(surfaceRaised, 216), withAlpha(backgroundBase, 244)});

        GradientDrawable inner = new GradientDrawable();
        inner.setShape(GradientDrawable.OVAL);
        inner.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        inner.setGradientRadius(dp(130f));
        inner.setColors(new int[]{withAlpha(backgroundBase, 154), withAlpha(backgroundBase, 0)});

        GradientDrawable accent = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{withAlpha(accentPrimary, 62), withAlpha(accentHighlight, 34), withAlpha(backgroundBase, 0)});
        accent.setShape(GradientDrawable.OVAL);

        LayerDrawable layers = new LayerDrawable(new Drawable[]{base, inner, accent});
        int inset = dpInt(8f); // Align perfectly with Outer Stroke #2 at 8dp inset
        layers.setLayerInset(2, inset, inset, inset, inset);
        return layers;
    }

    @NonNull
    private Drawable buildOverlayDrawable() {
        int textPrimary    = resolveColor(R.attr.xnetTextPrimary);
        int backgroundBase = resolveColor(R.attr.xnetBackground);
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{withAlpha(textPrimary, 40), withAlpha(backgroundBase, 22), withAlpha(backgroundBase, 72)});
        d.setShape(GradientDrawable.OVAL);
        return d;
    }

    @NonNull
    private Drawable buildBrandChipDrawable() {
        int surfaceRaised  = resolveColor(R.attr.xnetSurfaceRaised);
        int backgroundBase = resolveColor(R.attr.xnetBackground);
        int accentPositive = resolveColor(R.attr.xnetAccentPositive);

        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        d.setGradientRadius(dp(64f));
        d.setColors(new int[]{withAlpha(surfaceRaised, 232), withAlpha(backgroundBase, 244)});
        d.setStroke(dpInt(1f), withAlpha(accentPositive, 168));
        return d;
    }

    /**
     * CENTER HOLE — Exact DriveX buildAudioDiscCenterHoleDrawable() implementation:
     *   Outer Layer: Oval shape, color = backgroundDeep (alpha 208), stroke = 1dp stroke (alpha 176)
     *   Inner Layer: Oval shape, color = backgroundBase (alpha 255 - solid black spindle hole), inset = 12dp
     */
    @NonNull
    private Drawable buildCenterHoleDrawable() {
        int backgroundBase = resolveColor(R.attr.xnetBackground);
        int backgroundDeep = resolveColor(R.attr.xnetBackgroundDeep);
        int stroke         = resolveColor(R.attr.xnetStroke);

        // Outer dark ring overlay
        GradientDrawable outerLayer = new GradientDrawable();
        outerLayer.setShape(GradientDrawable.OVAL);
        outerLayer.setColor(withAlpha(backgroundDeep, 208));
        outerLayer.setStroke(dpInt(1f), withAlpha(stroke, 176));

        // Inner solid spindle hole (punches through the app icon label!)
        GradientDrawable innerLayer = new GradientDrawable();
        innerLayer.setShape(GradientDrawable.OVAL);
        innerLayer.setColor(withAlpha(backgroundBase, 255));

        LayerDrawable layers = new LayerDrawable(new Drawable[]{outerLayer, innerLayer});
        int inset = dpInt(12f);
        layers.setLayerInset(1, inset, inset, inset, inset);
        return layers;
    }

    @NonNull
    private Drawable buildOuterStrokeDrawable() {
        int stroke = resolveColor(R.attr.xnetStroke);
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(android.graphics.Color.TRANSPARENT);
        d.setStroke(dpInt(1f), withAlpha(stroke, 176));
        return d;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private int resolveColor(@AttrRes int attrRes) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attrRes, tv, true)) {
            return tv.data;
        }
        if (attrRes == R.attr.xnetBackgroundDeep) {
            return resolveColor(R.attr.xnetBackground);
        }
        return 0xFF808080;
    }

    private int withAlpha(int color, int alpha) {
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private int dpInt(float value) {
        return Math.round(dp(value));
    }
}
