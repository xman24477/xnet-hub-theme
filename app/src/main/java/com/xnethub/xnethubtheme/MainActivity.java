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
import com.xnethub.xnet_hub_theme.XnetBadgeView;
import com.xnethub.xnet_hub_theme.XnetCalloutCard;
import com.xnethub.xnet_hub_theme.XnetDrawerHelper;
import com.xnethub.xnet_hub_theme.XnetProgressBar;
import com.xnethub.xnet_hub_theme.XnetSnackbar;
import com.xnethub.xnet_hub_theme.XnetThemeManager;
import com.xnethub.xnet_hub_theme.XnetToast;

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
        // titleText is now an XnetBrandTextView which auto-formats itself from XML!

        setupThemeSpinner();
        setupTopBar();
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
        setupWidgetShowcase();
        setupToastDemo();
    }

    private void setupToastDemo() {
        View btnShowToast = findViewById(R.id.btnShowToast);
        android.widget.EditText inputEditTextTest = findViewById(R.id.inputEditTextTest);
        com.xnethub.xnet_hub_theme.XnetSpinner spinnerToastType = findViewById(R.id.spinnerToastType);

        if (spinnerToastType != null) {
            java.util.List<String> types = new java.util.ArrayList<>();
            types.add("INFO");
            types.add("WARNING");
            types.add("ERROR");
            spinnerToastType.setItems(types);
            spinnerToastType.setSelection(0);
        }

        if (btnShowToast != null && inputEditTextTest != null) {
            btnShowToast.setOnClickListener(v -> {
                String text = inputEditTextTest.getText().toString();
                com.xnethub.xnet_hub_theme.XnetCalloutCard.CalloutType type = com.xnethub.xnet_hub_theme.XnetCalloutCard.CalloutType.INFO;
                
                if (spinnerToastType != null) {
                    int pos = spinnerToastType.getSelectedItemPosition();
                    if (pos == 1) type = com.xnethub.xnet_hub_theme.XnetCalloutCard.CalloutType.WARNING;
                    else if (pos == 2) type = com.xnethub.xnet_hub_theme.XnetCalloutCard.CalloutType.ERROR;
                }

                if (text.isEmpty()) {
                    text = "Xnet Toast! Please enter some text.";
                    com.xnethub.xnet_hub_theme.XnetToast.show(this, text, com.xnethub.xnet_hub_theme.XnetCalloutCard.CalloutType.WARNING);
                } else {
                    com.xnethub.xnet_hub_theme.XnetToast.show(this, text, type);
                }
            });
        }
    }

    private void setupWidgetShowcase() {
        // Progress bar — smooth live demo animation
        XnetProgressBar progressBar = findViewById(R.id.xnetProgressBar);
        if (progressBar != null) progressBar.startContinuousDemo();

        // Badge — 7 notifications demo (Standalone)
        XnetBadgeView badgeView = findViewById(R.id.xnetBadgeView);
        if (badgeView != null) badgeView.setCount(7);
        
        // Typing text demo
        com.xnethub.xnet_hub_theme.XnetTypingTextView typingText = findViewById(R.id.xnetTypingTextTest);
        if (typingText != null) {
            typingText.setTypingText("এটা টেস্ট বাংলা টাইপিং ইফেক্ট\nConnection established.\nWelcome to Xnet.", 65L);
        }
        
        // Test Image for XnetImageView
        com.xnethub.xnet_hub_theme.XnetImageView imageView = findViewById(R.id.xnetImageViewTest);
        if (imageView != null) {
            String testProfileUrl = "https://firebasestorage.googleapis.com/v0/b/xnet-hub.appspot.com"
                    + "/o/profile_images%2FOGP445ls6UYvfCkJtXDbhmuA5hp2.jpg"
                    + "?alt=media&token=f2f75ba3-3c8a-42bf-bb76-8429406675e7";
            com.xnethub.xnet_hub_theme.XnetNavIconHelper.loadFromUrl(this, testProfileUrl, 500, icon -> imageView.setImageDrawable(icon));
        }

        // Callout cards — one per type
        XnetCalloutCard calloutInfo = findViewById(R.id.calloutInfo);
        if (calloutInfo != null) calloutInfo.set(XnetCalloutCard.CalloutType.INFO, "System initialized. All modules are online.");

        XnetCalloutCard calloutWarning = findViewById(R.id.calloutWarning);
        if (calloutWarning != null) calloutWarning.set(XnetCalloutCard.CalloutType.WARNING, "Network latency detected. Check connection.");

        XnetCalloutCard calloutError = findViewById(R.id.calloutError);
        if (calloutError != null) calloutError.set(XnetCalloutCard.CalloutType.ERROR, "Authorization failed. Access denied.");

        // Terminal loading view — switch to STYLE_TERMINAL
        com.xnethub.xnet_hub_theme.XnetLoadingView terminalLoader =
            findViewById(R.id.loadingViewTerminal);
        if (terminalLoader != null) terminalLoader.setStyle(
            com.xnethub.xnet_hub_theme.XnetLoadingView.STYLE_TERMINAL);

        // Custom XnetSwitch — live state label demo
        com.xnethub.xnet_hub_theme.XnetSwitch xnetSwitch = findViewById(R.id.xnetSwitch);
        TextView switchLabel = findViewById(R.id.switchLabel);
        if (xnetSwitch != null) {
            xnetSwitch.setOnCheckedChangeListener((sw, isChecked) -> {
                if (switchLabel != null) {
                    switchLabel.setText(isChecked ? "ON" : "OFF");
                }
                View rootView = findViewById(android.R.id.content);
                XnetSnackbar.show(rootView, isChecked ? "Feature ENABLED." : "Feature DISABLED.");
            });
        }

        // Cyber XnetSearchBar demo
        com.xnethub.xnet_hub_theme.XnetSearchBar searchBar = findViewById(R.id.xnetSearchBarTest);
        if (searchBar != null) {
            searchBar.setOnSearchListener(new com.xnethub.xnet_hub_theme.XnetSearchBar.OnSearchListener() {
                @Override
                public void onSearch(String query) {
                    View rootView = findViewById(android.R.id.content);
                    XnetSnackbar.show(rootView, "Searching: \"" + query + "\"");
                }

                @Override
                public void onClear() {
                    View rootView = findViewById(android.R.id.content);
                    XnetSnackbar.show(rootView, "Search cleared.");
                }
            });
        }

        // Toolbar Search Bar & 3-Dot Hexagon Menu
        com.xnethub.xnet_hub_theme.XnetSearchBar toolbarSearchBar = findViewById(R.id.xnetSearchBarToolbar);
        if (toolbarSearchBar != null) {
            toolbarSearchBar.setOnSearchListener(new com.xnethub.xnet_hub_theme.XnetSearchBar.OnSearchListener() {
                @Override
                public void onSearch(String query) {
                    View rootView = findViewById(android.R.id.content);
                    XnetSnackbar.show(rootView, "Toolbar Search: \"" + query + "\"");
                }

                @Override
                public void onClear() {
                    View rootView = findViewById(android.R.id.content);
                    XnetSnackbar.show(rootView, "Toolbar search cleared.");
                }
            });
        }

        View btnOverflow = findViewById(R.id.btnOverflowMenu);
        if (btnOverflow != null) {
            btnOverflow.setOnClickListener(v -> {
                View rootView = findViewById(android.R.id.content);
                XnetSnackbar.show(rootView, "Cyber 3-Dot Overflow Menu clicked.");
            });
        }

        // Cyber XnetTabLayout demo
        com.xnethub.xnet_hub_theme.XnetTabLayout tabLayout = findViewById(R.id.xnetTabLayoutTest);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    View rootView = findViewById(android.R.id.content);
                    if (tab != null && tab.getText() != null) {
                        //XnetSnackbar.show(rootView, "Tab Selected: " + tab.getText());
                    }
                }

                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }

        // Cyber Bottom Navigation Dock Demo
        com.xnethub.xnet_hub_theme.XnetBottomNavigationView bottomNav = findViewById(R.id.xnetBottomNav);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                View root = findViewById(android.R.id.content);
                //XnetSnackbar.show(root, "Navigated: " + item.getTitle());
                return true;
            });

            // TEST: Load profile picture from URL with offline caching for nav_profile item
            String testUserId = "OGP445ls6UYvfCkJtXDbhmuA5hp2";
            String testProfileUrl = "https://firebasestorage.googleapis.com/v0/b/xnet-hub.appspot.com"
                    + "/o/profile_images%2FOGP445ls6UYvfCkJtXDbhmuA5hp2.jpg"
                    + "?alt=media&token=f2f75ba3-3c8a-42bf-bb76-8429406675e7";
            bottomNav.setNavItemPhotoUrl(R.id.nav_profile, testProfileUrl, testUserId);

            // TEST: Notification Badge
            bottomNav.setNotificationBadge(R.id.nav_notifications, 5);
        }

        // Cyber FAB Demo
        com.xnethub.xnet_hub_theme.XnetFab xnetFab = findViewById(R.id.xnetFabTest);
        if (xnetFab != null) {
            xnetFab.setOnClickListener(v -> {
                View root = findViewById(android.R.id.content);
                XnetSnackbar.show(root, "XnetFab Action Triggered.");
            });
        }

        // (Snackbar demo on buttons removed as buttons were removed)
    }

    private void setupTopBar() {
        View btnSearchIcon = findViewById(R.id.btnSearchIcon);
        View titleText = findViewById(R.id.titleText);
        View searchBar = findViewById(R.id.xnetSearchBarToolbar);

        if (btnSearchIcon != null && titleText != null && searchBar != null) {
            btnSearchIcon.setOnClickListener(v -> {
                titleText.setVisibility(View.GONE);
                btnSearchIcon.setVisibility(View.GONE);
                searchBar.setVisibility(View.VISIBLE);
                // Optionally request focus and show keyboard
            });
        }
    }

    @Override
    public void onBackPressed() {
        View titleText = findViewById(R.id.titleText);
        View searchBar = findViewById(R.id.xnetSearchBarToolbar);
        View btnSearchIcon = findViewById(R.id.btnSearchIcon);

        if (searchBar != null && searchBar.getVisibility() == View.VISIBLE) {
            // Revert search bar to title
            searchBar.setVisibility(View.GONE);
            if (titleText != null) titleText.setVisibility(View.VISIBLE);
            if (btnSearchIcon != null) btnSearchIcon.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isThemeSpinnerInitialLoad = true;

    private void setupThemeSpinner() {
        com.xnethub.xnet_hub_theme.XnetSpinner spinner = findViewById(R.id.spinnerThemes);
        if (spinner == null) return;

        List<String> themeDisplayNames = new ArrayList<>();
        List<String> themeValues = new ArrayList<>();

        // Add System Default first
        themeDisplayNames.add("System Default");
        themeValues.add(""); // Empty string means use system default based on day/night or let it fallback to default

        themeDisplayNames.add("X-Cyber Green");
        themeValues.add(XnetThemeManager.THEME_CYBER_GREEN);

        themeDisplayNames.add("X-Cyber Blue");
        themeValues.add(XnetThemeManager.THEME_CYBER_BLUE);

        themeDisplayNames.add("X-Cyber Black");
        themeValues.add(XnetThemeManager.THEME_CYBER_BLACK);

        themeDisplayNames.add("X-Cyber Orange");
        themeValues.add(XnetThemeManager.THEME_CYBER_ORANGE);

        themeDisplayNames.add("X-Cyber RGB");
        themeValues.add(XnetThemeManager.THEME_CYBER_RGB);

        themeDisplayNames.add("Classic Light");
        themeValues.add(XnetThemeManager.THEME_LIGHT);

        themeDisplayNames.add("Classic Dark");
        themeValues.add(XnetThemeManager.THEME_DARK);

        spinner.setItems(themeDisplayNames);

        String currentTheme = XnetThemeManager.getTheme(this);
        int pos = themeValues.indexOf(currentTheme);
        if (pos >= 0) {
            spinner.setSelection(pos);
        } else {
            spinner.setSelection(0); // Default if not found
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isThemeSpinnerInitialLoad) {
                    isThemeSpinnerInitialLoad = false;
                    return;
                }
                String selectedTheme = themeValues.get(position);
                if (position == 0) {
                    // System default selected: we can clear the preference to let the app use default,
                    // but since our library might not fully support an "empty" theme out of the box,
                    // we'll default to THEME_CYBER_GREEN or whatever the library's default is.
                    // For now, let's just pass empty string if the library handles it.
                    if (!currentTheme.equals(selectedTheme)) {
                        XnetThemeManager.setTheme(MainActivity.this, selectedTheme);
                        recreate();
                    }
                } else if (!selectedTheme.equals(currentTheme)) {
                    XnetThemeManager.setTheme(MainActivity.this, selectedTheme);
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
                if (btnPlayPause instanceof android.widget.ImageButton) {
                    ((android.widget.ImageButton) btnPlayPause).setImageResource(
                        isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                }
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
        com.xnethub.xnet_hub_theme.XnetSpinner spinner = findViewById(R.id.spinnerFonts);
        if (spinner == null) return;

        List<String> fontNames = new ArrayList<>();
        fontNames.add(XnetThemeManager.FONT_DEFAULT);

        Field[] fields = com.xnethub.xnet_hub_theme.R.font.class.getFields();
        for (Field field : fields) {
            String name = field.getName();
            if (!fontNames.contains(name)) {
                fontNames.add(name);
            }
        }

        spinner.setItems(fontNames);

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
            public void onNothingSelected(AdapterView<?> parent) {}
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
