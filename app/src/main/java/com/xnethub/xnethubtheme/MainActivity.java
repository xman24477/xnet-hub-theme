package com.xnethub.xnethubtheme;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.xnethub.xnet_hub_theme.XnetBaseActivity;
import com.xnethub.xnet_hub_theme.XnetDrawerHelper;
import com.xnethub.xnet_hub_theme.XnetThemeManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends XnetBaseActivity {

    private boolean isSpinnerInitialLoad = true;
    private ObjectAnimator turntableAnimator;
    private boolean isPlaying = false;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView titleText = findViewById(R.id.titleText);
        com.xnethub.xnet_hub_theme.XnetTextFormatter.applyBrandName(titleText, "Xnet Hub UI Library Test");

        findViewById(R.id.btnThemeGreen).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_CYBER_GREEN));
        findViewById(R.id.btnThemeBlue).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_CYBER_BLUE));
        findViewById(R.id.btnThemeBlack).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_CYBER_BLACK));
        findViewById(R.id.btnThemeOrange).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_CYBER_ORANGE));
        findViewById(R.id.btnThemeRGB).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_CYBER_RGB));
        findViewById(R.id.btnThemeClassic).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_LIGHT));
        findViewById(R.id.btnThemeClassicDark).setOnClickListener(v -> changeTheme(XnetThemeManager.THEME_DARK));

        View topInsetSpacer = findViewById(R.id.topInsetSpacer);
        if (topInsetSpacer != null) {
            com.xnethub.xnet_hub_theme.XnetEdgeToEdge.applyTopSpacerWithAnimation(topInsetSpacer);
        }
        View bottomInsetSpacer = findViewById(R.id.bottomInsetSpacer);
        if (bottomInsetSpacer != null) {
            com.xnethub.xnet_hub_theme.XnetEdgeToEdge.applyBottomSpacerWithAnimation(bottomInsetSpacer);
        }
        View mainContent = findViewById(R.id.main);
        if (mainContent != null) {
            com.xnethub.xnet_hub_theme.XnetEdgeToEdge.applyContentInsets(mainContent, false, false);
        }

        setupFontSpinner();
        setupAudioPlayer();
        setupDrawer();
    }

    private void setupAudioPlayer() {
        com.xnethub.xnet_hub_theme.XnetAudioDiscView discView = findViewById(R.id.audioDiscTurntable);
        View btnPlayPause = findViewById(R.id.btnAudioPlayPause);

        if (discView != null && btnPlayPause != null) {
            // App icon and name are autoloaded by XnetAudioDiscView

            turntableAnimator = ObjectAnimator.ofFloat(discView, View.ROTATION, 0f, 360f);
            turntableAnimator.setDuration(4000);
            turntableAnimator.setRepeatCount(ValueAnimator.INFINITE);
            turntableAnimator.setInterpolator(new LinearInterpolator());

            btnPlayPause.setOnClickListener(v -> {
                isPlaying = !isPlaying;
                if (isPlaying) {
                    if (turntableAnimator.isPaused()) {
                        turntableAnimator.resume();
                    } else {
                        turntableAnimator.start();
                    }
                } else {
                    turntableAnimator.pause();
                }
            });
        }
    }

    private void setupFontSpinner() {
        Spinner spinner = findViewById(R.id.spinnerFonts);
        List<String> fontNames = new ArrayList<>();
        fontNames.add(XnetThemeManager.FONT_DEFAULT);

        // Dynamically get font names from R.font
        Field[] fields = com.xnethub.xnet_hub_theme.R.font.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (!fontNames.contains(name)) {
                fontNames.add(name);
            }
        }

        // Apply custom hex background to spinner popup dynamically
        com.xnethub.xnet_hub_theme.XnetThemeManager.applyHexPopupBackground(this, spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fontNames) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TypedValue tv = new TypedValue();
                    if (getContext().getTheme().resolveAttribute(com.xnethub.xnet_hub_theme.R.attr.xnetTextPrimary, tv, true)) {
                        ((TextView) view).setTextColor(tv.data);
                    }
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    TypedValue tv = new TypedValue();
                    if (getContext().getTheme().resolveAttribute(com.xnethub.xnet_hub_theme.R.attr.xnetTextSecondary, tv, true)) {
                        ((TextView) view).setTextColor(tv.data);
                    }
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String currentFont = XnetThemeManager.getFont(this);
        int pos = fontNames.indexOf(currentFont);
        if (pos >= 0) {
            spinner.setSelection(pos);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialLoad) {
                    isSpinnerInitialLoad = false;
                    return;
                }
                String selectedFont = fontNames.get(position);
                if (!selectedFont.equals(currentFont)) {
                    XnetThemeManager.setFont(MainActivity.this, selectedFont);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        MaterialCardView drawerPanel = findViewById(R.id.drawerPanel);
        if (drawerPanel == null) {
            drawerPanel = findViewById(com.xnethub.xnet_hub_theme.R.id.drawerPanel);
        }
        MaterialCardView profileCard = findViewById(com.xnethub.xnet_hub_theme.R.id.drawerProfileCard);
        View divider = findViewById(com.xnethub.xnet_hub_theme.R.id.drawerDivider);

        if (drawerLayout == null || drawerPanel == null) return;

        // Apply library-managed drawer styling (shape + stroke + scrim)
        XnetDrawerHelper.applyDrawerStyle(this, drawerLayout, drawerPanel);
        if (profileCard != null) {
            XnetDrawerHelper.applyProfileCardStyle(this, profileCard);
        }
        if (divider != null) {
            XnetDrawerHelper.applyDividerStyle(this, divider);
        }

        // Hook up open-drawer button in main content toolbar area
        View btnOpenDrawer = findViewById(R.id.btnOpenDrawer);
        final View targetPanel = drawerPanel;
        if (btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(v -> drawerLayout.openDrawer(targetPanel));
        }
    }

    private void changeTheme(String theme) {
        if (!XnetThemeManager.getTheme(this).equals(theme)) {
            XnetThemeManager.setTheme(this, theme);
            recreate();
        }
    }
}
