package com.xnethub.xnet_hub_theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.Random;

public class XnetThemeManager {
    
    private static final String TAG = "XnetThemeManager";
    private static final String PREF_NAME = "xnet_theme_prefs";
    private static final String KEY_THEME = "selected_theme";
    private static final String KEY_FONT = "selected_font";

    public static final String THEME_SYSTEM = "system_default";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_CYBER_GREEN = "cyber_green";
    public static final String THEME_CYBER_BLUE = "cyber_blue";
    public static final String THEME_CYBER_BLACK = "cyber_black";
    public static final String THEME_CYBER_ORANGE = "cyber_orange";
    public static final String THEME_CYBER_RGB = "cyber_rgb";

    @Deprecated
    public static final String THEME_CLASSIC_LIGHT = THEME_LIGHT;
    @Deprecated
    public static final String THEME_CLASSIC_DARK = THEME_DARK;
    
    public static final String FONT_DEFAULT = "default";
    public static final String FONT_RAJDHANI = "rajdhani";
    public static final String FONT_ORBITRON = "orbitron";
    public static final String FONT_SHARE_TECH_MONO = "share_tech_mono";

    // Cyber RGB Palette Colors (Neon Green, Cyan Blue, Amber Orange, Electric Magenta, Bright Yellow)
    public static int[] getRgbAccents(Context context) {
        return new int[] {
            androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_green_accent_primary), // Neon Green
            androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_blue_accent_primary), // Cyber Cyan Blue
            androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_orange_accent_primary), // Amber Orange
            androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_rgb_accent_primary), // Electric Magenta
            androidx.core.content.ContextCompat.getColor(context, R.color.xnet_color_cyber_orange_accent_highlight)  // Neon Yellow
        };
    }

    public static void setTheme(Context context, String themeName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, themeName).apply();
    }

    public static String getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, THEME_SYSTEM);
    }

    public static void setFont(Context context, String fontName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FONT, fontName).apply();
    }

    public static String getFont(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FONT, FONT_DEFAULT);
    }

    /**
     * Applies the selected theme and font configuration to the current Activity.
     * Must be called in onCreate() BEFORE setContentView() and super.onCreate().
     */
    public static void applyThemeToActivity(Context context) {
        String theme = getTheme(context);
        String font = getFont(context);

        int styleResId;

        switch (theme) {
            case THEME_LIGHT:
            case "classic_light":
                styleResId = R.style.Theme_XnetCore_Light;
                break;
            case THEME_DARK:
            case "classic_dark":
                styleResId = R.style.Theme_XnetCore_Dark;
                break;
            case THEME_CYBER_GREEN:
                styleResId = R.style.Theme_XnetCore_CyberGreen;
                break;
            case THEME_CYBER_BLUE:
                styleResId = R.style.Theme_XnetCore_CyberBlue;
                break;
            case THEME_CYBER_BLACK:
                styleResId = R.style.Theme_XnetCore_CyberBlack;
                break;
            case THEME_CYBER_ORANGE:
                styleResId = R.style.Theme_XnetCore_CyberOrange;
                break;
            case THEME_CYBER_RGB:
                styleResId = R.style.Theme_XnetCore_CyberRGB;
                break;
            case THEME_SYSTEM:
            default:
                int uiMode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                if (uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    styleResId = R.style.Theme_XnetCore_Dark;
                } else {
                    styleResId = R.style.Theme_XnetCore_Light;
                }
                break;
        }

        context.setTheme(styleResId);
        
        // Apply Dynamic Font Overlay
        if (font != null && !font.equals(FONT_DEFAULT)) {
            if (font.toLowerCase().contains("rajdhani")) {
                context.getTheme().applyStyle(R.style.Theme_XnetCore_Font_Rajdhani, true);
            } else if (font.toLowerCase().contains("orbitron")) {
                context.getTheme().applyStyle(R.style.Theme_XnetCore_Font_Orbitron, true);
            } else if (font.toLowerCase().contains("share_tech_mono")) {
                context.getTheme().applyStyle(R.style.Theme_XnetCore_Font_ShareTechMono, true);
            }
        }

        // Auto-trigger Hacker RGB Vibe inside the library if Activity is passed
        if (context instanceof Activity && THEME_CYBER_RGB.equals(theme)) {
            Activity activity = (Activity) context;
            activity.getWindow().getDecorView().post(() -> applyRGBHackerVibe(activity));
        }
    }

    /**
     * DYNAMIC HACKER RGB ENGINE:
     * When X-Cyber RGB theme is active, this method recursively traverses the layout
     * and randomizes button strokes, card borders, and chip tints across Neon Green, Cyber Blue,
     * Amber Orange, and Electric Magenta. Every button and card gets its own distinct Cyber RGB accent!
     */
    public static void applyRGBHackerVibe(Activity activity) {
        if (activity == null) return;
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            applyRGBHackerVibe(rootView);
        }
    }

    public static void applyRGBHackerVibe(View view) {
        if (view == null || view.getContext() == null) return;
        Context context = view.getContext();

        if (!THEME_CYBER_RGB.equals(getTheme(context))) {
            return; // Only execute dynamic randomization when X-Cyber RGB theme is active
        }

        Random random = new Random(view.hashCode()); // Deterministic random per view instance

        traverseAndRandomizeRGB(view, random);
    }

    private static void traverseAndRandomizeRGB(View view, Random random) {
        if (view == null) return;

        if (view instanceof MaterialButton) {
            MaterialButton button = (MaterialButton) view;
            int accentColor = getRgbAccents(view.getContext())[random.nextInt(getRgbAccents(view.getContext()).length)];
            int strokeWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, view.getResources().getDisplayMetrics());
            
            button.setStrokeColor(ColorStateList.valueOf(accentColor));
            button.setStrokeWidth(strokeWidthPx);
            button.setRippleColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 70)));
            if (button.getIcon() != null) {
                button.setIconTint(ColorStateList.valueOf(accentColor));
            }
        } else if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            int accentColor = getRgbAccents(view.getContext())[random.nextInt(getRgbAccents(view.getContext()).length)];
            int strokeWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, view.getResources().getDisplayMetrics());
            card.setStrokeColor(accentColor);
            card.setStrokeWidth(strokeWidthPx);
        } else if (view instanceof Spinner) {
            applyHexPopupBackground(view.getContext(), (Spinner) view);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                traverseAndRandomizeRGB(group.getChildAt(i), random);
            }
        }
    }
    
    /**
     * Optional: Helper to set AppCompatDelegate night mode
     */
    public static void applyNightMode(Context context) {
        String theme = getTheme(context);
        if (theme.equals(THEME_SYSTEM)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (theme.equals(THEME_DARK) || theme.contains("cyber") || theme.equals("classic_dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Applies a MaterialShapeDrawable to a Spinner's popup background.
     * Uses Hex cut corners for Cyber themes and standard rounded corners for Normal themes.
     */
    public static void applyHexPopupBackground(Context context, Spinner spinner) {
        try {
            TypedValue tvShape = new TypedValue();
            int shapeStyle = R.style.ShapeAppearanceOverlay_XnetCore_CutCard_Classic;
            if (context.getTheme().resolveAttribute(R.attr.xnetShapeCard, tvShape, true) && tvShape.resourceId != 0) {
                shapeStyle = tvShape.resourceId;
            }
            ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder(context, shapeStyle, 0).build();
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeModel);

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.xnetSurfaceRaised, typedValue, true);
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(typedValue.data));

            // If Cyber RGB, pick a random accent color for popup stroke
            int strokeColor;
            if (THEME_CYBER_RGB.equals(getTheme(context))) {
                Random r = new Random(spinner.hashCode());
                strokeColor = getRgbAccents(context)[r.nextInt(getRgbAccents(context).length)];
            } else {
                context.getTheme().resolveAttribute(R.attr.xnetStroke, typedValue, true);
                strokeColor = typedValue.data;
            }

            float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, context.getResources().getDisplayMetrics());
            materialShapeDrawable.setStroke(strokeWidth, strokeColor);

            if (spinner instanceof AppCompatSpinner) {
                ((AppCompatSpinner) spinner).setPopupBackgroundDrawable(materialShapeDrawable);
            } else {
                spinner.setPopupBackgroundDrawable(materialShapeDrawable);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to apply spinner popup background.", e);
        }
    }

    /**
     * Attaches the animated backdrop view as the background of any ViewGroup container (e.g. Activity or Fragment root).
     */
    public static void attachBackdropToContainer(ViewGroup container) {
        if (container == null) return;
        XnetAnimatedBackdropView backdropView = new XnetAnimatedBackdropView(container.getContext());
        backdropView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        container.addView(backdropView, 0);
    }
}
