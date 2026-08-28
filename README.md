# Xnet Hub Theme

`Xnet Hub Theme` একটি reusable Android theme library। যেকোনো Android app বা অন্য কোনো Android library-তে dependency হিসেবে যোগ করে একই Xnet design system ব্যবহার করা যাবে। এতে আছে classic light/dark theme, system-default light/dark support, X-Cyber theme variants, themed button/card/text view, drawer UI, audio disc UI, edge-to-edge helper, animated backdrop, এবং Xnet brand text formatter।

Public GitHub repo:

```text
https://github.com/xman24477/xnet-hub-theme
```

JitPack dependency page:

```text
https://jitpack.io/#xman24477/xnet-hub-theme/1.1.5
```

## ইনস্টলেশন

প্রথমে app project-এর `settings.gradle` ফাইলে JitPack repository যোগ করতে হবে। এটা একবার করলেই হবে।

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

তারপর app-level `build.gradle` ফাইলে dependency যোগ করতে হবে।

```gradle
dependencies {
    implementation 'com.github.xman24477:xnet-hub-theme:1.1.5'
}
```

এরপর Android Studio থেকে Gradle Sync করলেই library ব্যবহার করা যাবে।

## Activity সেটআপ

সবচেয়ে সহজ উপায় হলো আপনার activity-কে `XnetBaseActivity` থেকে extend করা। এতে selected theme, night mode, edge-to-edge system bar, এবং cyber theme হলে activity background animation স্বয়ংক্রিয়ভাবে apply হবে।

```java
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.xnethub.xnet_hub_theme.XnetBaseActivity;

public class MainActivity extends XnetBaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

যদি কোনো কারণে `XnetBaseActivity` extend করা সম্ভব না হয়, তাহলে theme manually apply করতে হবে। `super.onCreate()`-এর আগে theme apply করা জরুরি।

```java
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.xnethub.xnet_hub_theme.XnetEdgeToEdge;
import com.xnethub.xnet_hub_theme.XnetThemeManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        XnetThemeManager.applyNightMode(this);
        XnetThemeManager.applyThemeToActivity(this);
        super.onCreate(savedInstanceState);
        XnetEdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
    }
}
```

## Theme নির্বাচন

Library-তে available theme constants:

```java
XnetThemeManager.THEME_SYSTEM
XnetThemeManager.THEME_LIGHT
XnetThemeManager.THEME_DARK
XnetThemeManager.THEME_CYBER_GREEN
XnetThemeManager.THEME_CYBER_BLUE
XnetThemeManager.THEME_CYBER_BLACK
XnetThemeManager.THEME_CYBER_ORANGE
XnetThemeManager.THEME_CYBER_RGB
```

Theme set করার পর current activity recreate করলে নতুন theme apply হবে।

```java
XnetThemeManager.setTheme(this, XnetThemeManager.THEME_CYBER_GREEN);
recreate();
```

Phone-এর system light/dark setting follow করতে চাইলে:

```java
XnetThemeManager.setTheme(this, XnetThemeManager.THEME_SYSTEM);
recreate();
```

Font set করার example:

```java
XnetThemeManager.setFont(this, XnetThemeManager.FONT_RAJDHANI);
XnetThemeManager.setFont(this, XnetThemeManager.FONT_ORBITRON);
XnetThemeManager.setFont(this, XnetThemeManager.FONT_SHARE_TECH_MONO);
XnetThemeManager.setFont(this, XnetThemeManager.FONT_DEFAULT);
recreate();
```

Button click দিয়ে theme switch করার example:

```java
findViewById(R.id.btnCyberGreen).setOnClickListener(v -> {
    XnetThemeManager.setTheme(this, XnetThemeManager.THEME_CYBER_GREEN);
    recreate();
});

findViewById(R.id.btnSystem).setOnClickListener(v -> {
    XnetThemeManager.setTheme(this, XnetThemeManager.THEME_SYSTEM);
    recreate();
});
```

## Theme Attribute ব্যবহার

নিজের XML layout বানানোর সময় hardcoded color না দিয়ে theme attribute ব্যবহার করা ভালো। তাহলে selected theme অনুযায়ী UI নিজে নিজে color বদলাবে।

```xml
android:background="?attr/xnetBackground"
android:textColor="?attr/xnetTextPrimary"
app:strokeColor="?attr/xnetStroke"
app:backgroundTint="?attr/xnetSurfaceRaised"
```

Common attributes:

```text
xnetBackground
xnetBackgroundAlt
xnetBackgroundDeep
xnetSurfaceBase
xnetSurfaceAlt
xnetSurfaceRaised
xnetSurfaceFloat
xnetStroke
xnetStrokeSoft
xnetTextPrimary
xnetTextSecondary
xnetTextMuted
xnetTextHint
xnetAccentPrimary
xnetAccentSecondary
xnetAccentPrimarySoft
xnetAccentHighlight
xnetAccentHighlightSoft
xnetAccentPositive
xnetGlass
xnetGlassSoft
xnetScrim
```

## TextView

`XnetTextView` ব্যবহার করলে text-এর মধ্যে `Xnet` বা `Xnet Hub` থাকলে brand formatting automatically apply হবে। active theme অনুযায়ী brand text-এর color বদলাবে।

```xml
<com.xnethub.xnet_hub_theme.XnetTextView
    android:id="@+id/title"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Welcome to Xnet Hub"
    android:textColor="?attr/xnetTextPrimary"
    android:textSize="24sp"
    android:textStyle="bold" />
```

Java থেকে text set করা:

```java
XnetTextView title = findViewById(R.id.title);
title.setText("Xnet Hub Dashboard");
```

Existing normal `TextView`-এ brand formatting apply করা:

```java
TextView title = findViewById(R.id.title);
XnetTextFormatter.applyBrandName(title, "Xnet Hub Login");
```

Dual-tone text তৈরি করা:

```java
TextView label = findViewById(R.id.label);
XnetTextFormatter.applyDualTone(label, "XNET", " HUB");
```

## Button

`XnetButton` হলো theme-aware Material button। এটা active theme-এর color, shape, stroke, font, এবং brand text formatting ব্যবহার করে।

```xml
<com.xnethub.xnet_hub_theme.XnetButton
    android:id="@+id/loginButton"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Login with Xnet Hub"
    android:textAllCaps="false" />
```

Click listener:

```java
XnetButton loginButton = findViewById(R.id.loginButton);
loginButton.setOnClickListener(v -> {
    // Login flow শুরু করুন।
});
```

আপনি চাইলে normal `MaterialButton`-এও Xnet style ব্যবহার করতে পারেন।

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/actionButton"
    style="@style/Widget.XnetCore.Button"
    android:layout_width="match_parent"
    android:layout_height="48dp"
    android:text="Continue"
    android:textAllCaps="false" />
```

## Card

`XnetCard` ব্যবহার করলে card active theme অনুযায়ী background, stroke, এবং shape পাবে।

```xml
<com.xnethub.xnet_hub_theme.XnetCard
    android:id="@+id/infoCard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:padding="16dp">

    <com.xnethub.xnet_hub_theme.XnetTextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Xnet Hub Account"
        android:textColor="?attr/xnetTextPrimary" />

</com.xnethub.xnet_hub_theme.XnetCard>
```

Normal `MaterialCardView`-এ style ব্যবহার:

```xml
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.XnetCore.Card"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

## Background Animation

`XnetAnimatedBackdropView` cyber animated background draw করে। Classic light/dark theme-এ এটি static background হিসেবে থাকবে, আর X-Cyber theme-এ animation চালু হবে।

XML layout-এ সরাসরি ব্যবহার:

```xml
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.xnethub.xnet_hub_theme.XnetAnimatedBackdropView
        android:id="@+id/backdrop"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="24dp">

        <!-- এখানে screen content থাকবে -->

    </LinearLayout>
</FrameLayout>
```

Existing container-এর ভিতরে Java থেকে backdrop যোগ করা:

```java
ViewGroup root = findViewById(R.id.rootContainer);
XnetThemeManager.attachBackdropToContainer(root);
```

## Activity Background Animation

`XnetBaseActivity` ব্যবহার করলে cyber theme selected থাকলে activity content-এর পেছনে animated background wrapper automatically যুক্ত হবে।

```java
public class DashboardActivity extends XnetBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }
}
```

Animated cyber theme চালু করার example:

```java
XnetThemeManager.setTheme(this, XnetThemeManager.THEME_CYBER_RGB);
recreate();
```

Classic light/dark theme-এ normal static background থাকবে। X-Cyber theme variants-এ animated background চালু হবে।

## Edge-To-Edge Layout

`XnetBaseActivity` নিজে থেকেই `XnetEdgeToEdge.enable(this)` call করে। Manual setup করলে নিজে call করতে হবে।

```java
XnetEdgeToEdge.enable(this);
```

Status bar-এর জন্য top spacer:

```xml
<View
    android:id="@+id/statusSpacer"
    android:layout_width="match_parent"
    android:layout_height="0dp" />
```

```java
XnetEdgeToEdge.applyTopSpacer(findViewById(R.id.statusSpacer));
```

Navigation bar-এর জন্য bottom spacer:

```xml
<View
    android:id="@+id/navSpacer"
    android:layout_width="match_parent"
    android:layout_height="0dp" />
```

```java
XnetEdgeToEdge.applyBottomSpacerWithAnimation(findViewById(R.id.navSpacer));
```

Content-এর padding-এ system bar inset যোগ করা:

```java
XnetEdgeToEdge.applyContentInsets(findViewById(R.id.content), true, true);
```

FAB বা bottom button-এর margin-এ bottom inset যোগ করা:

```java
XnetEdgeToEdge.applyBottomInsetMargin(findViewById(R.id.fab));
```

## Drawer

Library-তে ready drawer content layout আছে: `@layout/xnet_drawer_content`।

```xml
<include
    android:id="@+id/xnetDrawer"
    layout="@layout/xnet_drawer_content" />
```

Complete `DrawerLayout` example:

```xml
<androidx.drawerlayout.widget.DrawerLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/drawerLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <FrameLayout
        android:id="@+id/mainContent"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.xnethub.xnet_hub_theme.XnetAnimatedBackdropView
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <!-- Main screen content -->

    </FrameLayout>

    <include layout="@layout/xnet_drawer_content" />

</androidx.drawerlayout.widget.DrawerLayout>
```

Drawer style apply করা:

```java
DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
MaterialCardView drawerPanel = findViewById(R.id.drawerPanel);
MaterialCardView profileCard = findViewById(R.id.drawerProfileCard);
View divider = findViewById(R.id.drawerDivider);

XnetDrawerHelper.applyDrawerStyle(this, drawerLayout, drawerPanel);
XnetDrawerHelper.applyProfileCardStyle(this, profileCard);
XnetDrawerHelper.applyDividerStyle(this, divider);
```

Profile header hide করা:

```java
XnetDrawerHelper.setProfileHeaderVisible(findViewById(R.id.drawerProfileCard), false);
```

Drawer user text update করা:

```java
TextView userName = findViewById(R.id.drawerUserName);
TextView userEmail = findViewById(R.id.drawerUserEmail);

userName.setText("Xnet User");
userEmail.setText("user@example.com");
```

Drawer menu item dynamically add করা:

```java
LinearLayout menuContainer = findViewById(R.id.drawerMenuContainer);

XnetButton filesButton = new XnetButton(this);
filesButton.setText("Files");
filesButton.setAllCaps(false);
filesButton.setOnClickListener(v -> drawerLayout.closeDrawers());

menuContainer.addView(filesButton);
```

Drawer open/close:

```java
drawerLayout.openDrawer(GravityCompat.START);
drawerLayout.closeDrawer(GravityCompat.START);
```

## Cut Mask Layout

`XnetCutMaskLayout` child views-কে cyber cut-corner shape অনুযায়ী clip করে। Drawer panel বা custom shell-এর ভিতরে animated background থাকলে এটা useful।

```xml
<com.xnethub.xnet_hub_theme.XnetCutMaskLayout
    android:id="@+id/cutMask"
    android:layout_width="match_parent"
    android:layout_height="240dp">

    <com.xnethub.xnet_hub_theme.XnetAnimatedBackdropView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.xnethub.xnet_hub_theme.XnetCutMaskLayout>
```

Cut size বা enable state change:

```java
XnetCutMaskLayout cutMask = findViewById(R.id.cutMask);
cutMask.setCutSizeDp(22f);
cutMask.setCutCornersEnabled(true);
```

## Audio Disc UI

`XnetAudioDiscView` একটি reusable audio artwork/disc widget। এটি host app-এর label এবং launcher icon auto-load করে।

XML:

```xml
<com.xnethub.xnet_hub_theme.XnetAudioDiscView
    android:id="@+id/audioDisc"
    android:layout_width="260dp"
    android:layout_height="260dp"
    android:layout_gravity="center" />
```

Album artwork set করা:

```java
XnetAudioDiscView audioDisc = findViewById(R.id.audioDisc);
audioDisc.setArtworkBitmap(albumBitmap);
```

Artwork clear করে themed placeholder দেখানো:

```java
audioDisc.setArtworkBitmap(null);
```

Center brand icon override করা:

```java
audioDisc.setBrandIcon(R.drawable.ic_launcher_foreground);
```

Top arc text override করা:

```java
audioDisc.setDiscTopText("XNET MUSIC");
```

Theme change-এর পর view recreate না করে color refresh করা:

```java
audioDisc.refreshPalette();
```

## Spinner এবং Popup Styling

Spinner-এ Xnet style ব্যবহার:

```xml
<androidx.appcompat.widget.AppCompatSpinner
    android:id="@+id/themeSpinner"
    style="@style/Widget.XnetCore.Spinner"
    android:layout_width="match_parent"
    android:layout_height="48dp" />
```

Java থেকে popup background apply করা:

```java
Spinner spinner = findViewById(R.id.themeSpinner);
XnetThemeManager.applyHexPopupBackground(this, spinner);
```

## Dialog Styling

X-Cyber themes Material dialog overlay set করে। Material dialog example:

```java
new MaterialAlertDialogBuilder(this)
        .setTitle("Xnet Hub")
        .setMessage("Theme-aware dialog")
        .setPositiveButton("OK", null)
        .show();
```

## Complete Screen Example

`activity_main.xml`:

```xml
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawerLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:theme="@style/Theme.XnetCore.CyberGreen"
    android:background="@android:color/transparent">

<RelativeLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/transparent">

    <!-- Top status bar inset spacer container -->
    <FrameLayout
        android:id="@+id/topInsetSpacer"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_alignParentTop="true" />

    <!-- FIXED TOP CYBER TOOLBAR HEADER -->
    <LinearLayout
        android:id="@+id/toolbarContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/topInsetSpacer"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:paddingStart="16dp"
        android:paddingTop="8dp"
        android:paddingEnd="16dp"
        android:paddingBottom="8dp"
        android:background="@android:color/transparent">

        <!-- Left: XnetDrawerButton -->
        <com.xnethub.xnet_hub_theme.XnetDrawerButton
            android:id="@+id/btnOpenDrawer"
            android:layout_width="44dp"
            android:layout_height="44dp"
            android:padding="8dp" />

        <!-- Center: Title and hidden SearchBar -->
        <FrameLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="6dp"
            android:layout_marginEnd="6dp">

            <com.xnethub.xnet_hub_theme.XnetBrandTextView
                android:id="@+id/titleText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_gravity="center_vertical"
                android:textSize="20sp"
                android:textStyle="bold"
                android:textColor="?attr/xnetTextPrimary"
                android:text="Xnet Hub UI Library Test" />

            <com.xnethub.xnet_hub_theme.XnetSearchBar
                android:id="@+id/xnetSearchBarToolbar"
                android:layout_width="match_parent"
                android:layout_height="48dp"
                android:hint="Search..."
                android:visibility="gone"/>
        </FrameLayout>

        <!-- Search Icon -->
        <ImageButton
            android:id="@+id/btnSearchIcon"
            android:layout_width="44dp"
            android:layout_height="44dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_search"
            app:tint="?attr/xnetTextPrimary" />

        <!-- Right: XnetOverflowButton -->
        <com.xnethub.xnet_hub_theme.XnetOverflowButton
            android:id="@+id/btnOverflowMenu"
            android:layout_width="44dp"
            android:layout_height="44dp"
            android:padding="8dp" />
    </LinearLayout>

    <!-- CENTER SCROLLABLE CONTENT BODY (Bounded between toolbar and bottom nav) -->
    <ScrollView
        android:id="@+id/scrollView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/toolbarContainer"
        android:layout_above="@id/xnetBottomNav"
        android:background="@android:color/transparent"
        android:fillViewport="true">

        <LinearLayout
            android:id="@+id/main"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="20dp"
            android:gravity="center_horizontal">

            <!-- ROW 1: THEME & FONT SPINNERS -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:layout_marginBottom="24dp"
                android:weightSum="2"
                android:baselineAligned="false">

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical"
                    android:layout_marginEnd="8dp">
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Select Theme:"
                        android:textColor="?attr/xnetTextPrimary"
                        android:layout_marginBottom="8dp"/>
                    <com.xnethub.xnet_hub_theme.XnetSpinner
                        android:id="@+id/spinnerThemes"
                        android:layout_width="match_parent"
                        android:layout_height="54dp"/>
                </LinearLayout>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical"
                    android:layout_marginStart="8dp">
                    <TextView
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:text="Select Font:"
                        android:textColor="?attr/xnetTextPrimary"
                        android:layout_marginBottom="8dp"/>
                    <com.xnethub.xnet_hub_theme.XnetSpinner
                        android:id="@+id/spinnerFonts"
                        android:layout_width="match_parent"
                        android:layout_height="54dp"/>
                </LinearLayout>
            </LinearLayout>

            <!-- ROW 2: INPUT FIELD, SPINNER & BUTTON -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Themed Toast Test:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="24dp">
                
                <com.xnethub.xnet_hub_theme.XnetTextInputLayout
                    android:id="@+id/inputLayoutTest"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:hint="Toast Message"
                    app:shapeAppearanceOverlay="?attr/xnetShapeInput"
                    android:layout_marginEnd="8dp">

                    <com.xnethub.xnet_hub_theme.XnetEditText
                        android:id="@+id/inputEditTextTest"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:inputType="text" />
                </com.xnethub.xnet_hub_theme.XnetTextInputLayout>

                <com.xnethub.xnet_hub_theme.XnetSpinner
                    android:id="@+id/spinnerToastType"
                    android:layout_width="110dp"
                    android:layout_height="54dp"
                    android:layout_marginEnd="8dp"/>

                <com.xnethub.xnet_hub_theme.XnetButton
                    android:id="@+id/btnShowToast"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Show" />
            </LinearLayout>

            <!-- ROW 3: SWITCH & LOADERS -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Controls &amp; Loading Spinners:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:layout_marginBottom="24dp"
                android:weightSum="3">
                
                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:gravity="center_vertical"
                    android:orientation="horizontal">
                    <com.xnethub.xnet_hub_theme.XnetSwitch
                        android:id="@+id/xnetSwitch"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"/>
                    <TextView
                        android:id="@+id/switchLabel"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:text="OFF"
                        android:textColor="?attr/xnetTextSecondary"
                        android:textSize="12sp"/>
                </LinearLayout>

                <com.xnethub.xnet_hub_theme.XnetLoadingView
                    android:id="@+id/loadingViewRing"
                    android:layout_width="0dp"
                    android:layout_height="36dp"
                    android:layout_weight="1"/>

                <com.xnethub.xnet_hub_theme.XnetLoadingView
                    android:id="@+id/loadingViewTerminal"
                    android:layout_width="0dp"
                    android:layout_height="36dp"
                    android:layout_weight="1"/>
            </LinearLayout>

            <!-- ROW 4: CHIPS (Single & Multi Select equivalent via group) -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Themed Cyber Chips:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.google.android.material.chip.ChipGroup
                android:id="@+id/chipGroupTabs"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:singleSelection="true"
                app:selectionRequired="true"
                android:layout_marginBottom="24dp">

                <com.xnethub.xnet_hub_theme.XnetChip
                    android:id="@+id/chipSystem"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="SYSTEM"
                    android:checkable="true"
                    android:checked="true"
                    android:layout_marginEnd="8dp"/>

                <com.xnethub.xnet_hub_theme.XnetChip
                    android:id="@+id/chipNetwork"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="NETWORK"
                    android:checkable="true"
                    android:layout_marginEnd="8dp"/>

                <com.xnethub.xnet_hub_theme.XnetChip
                    android:id="@+id/chipSecure"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="SECURE"
                    android:checkable="true"/>
            </com.google.android.material.chip.ChipGroup>

            <!-- ROW 5: TAB LAYOUT -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Themed Cyber TabLayout:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetTabLayout
                android:id="@+id/xnetTabLayoutTest"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                app:tabMode="fixed"
                app:tabGravity="fill"
                android:layout_marginBottom="24dp">

                <com.google.android.material.tabs.TabItem
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="OVERVIEW" />

                <com.google.android.material.tabs.TabItem
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="ANALYTICS" />

                <com.google.android.material.tabs.TabItem
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="TERMINAL" />
            </com.xnethub.xnet_hub_theme.XnetTabLayout>

            <!-- ROW 6: AUDIO PLAYER -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Cyber Audio Turntable Player:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                app:cardCornerRadius="16dp"
                app:cardElevation="6dp"
                app:cardBackgroundColor="?attr/xnetSurfaceAlt"
                app:strokeColor="?attr/xnetStroke"
                app:strokeWidth="1.5dp"
                android:layout_marginBottom="24dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="16dp"
                    android:gravity="center">

                    <com.xnethub.xnet_hub_theme.XnetAudioDiscView
                        android:id="@+id/audioDiscTurntable"
                        android:layout_width="160dp"
                        android:layout_height="160dp"
                        android:layout_marginBottom="14dp" />

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:gravity="center"
                        android:orientation="horizontal">

                        <ImageButton
                            android:layout_width="40dp"
                            android:layout_height="40dp"
                            android:background="@drawable/bg_xnet_audio_control_button_ripple"
                            android:padding="8dp"
                            android:scaleType="centerInside"
                            app:srcCompat="@android:drawable/ic_media_previous"
                            app:tint="?attr/xnetTextPrimary" />

                        <ImageButton
                            android:id="@+id/btnAudioPlayPause"
                            android:layout_width="60dp"
                            android:layout_height="60dp"
                            android:layout_marginStart="12dp"
                            android:layout_marginEnd="12dp"
                            android:background="@drawable/bg_xnet_audio_play_hex_ripple"
                            android:padding="14dp"
                            android:scaleType="centerInside"
                            app:srcCompat="@android:drawable/ic_media_play" />

                        <ImageButton
                            android:layout_width="40dp"
                            android:layout_height="40dp"
                            android:background="@drawable/bg_xnet_audio_control_button_ripple"
                            android:padding="8dp"
                            android:scaleType="centerInside"
                            app:srcCompat="@android:drawable/ic_media_next"
                            app:tint="?attr/xnetTextPrimary" />
                    </LinearLayout>
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>

            <!-- ROW 7: PROGRESS BAR -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Progress Bar:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetProgressBar
                android:id="@+id/xnetProgressBar"
                android:layout_width="match_parent"
                android:layout_height="18dp"
                android:layout_marginBottom="24dp"/>

            <!-- ROW 8: NEON DIVIDER -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Neon Divider:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetDivider
                android:id="@+id/xnetDivider"
                android:layout_width="match_parent"
                android:layout_height="14dp"
                android:layout_marginBottom="24dp"/>

            <!-- BADGE VIEW & SEARCH BAR (Standalone tests) -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Badge View (Standalone):"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetBadgeView
                android:id="@+id/xnetBadgeView"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"/>

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Themed Cyber Search Bar Test:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetSearchBar
                android:id="@+id/xnetSearchBarTest"
                android:layout_width="match_parent"
                android:layout_height="52dp"
                android:hint="Search files, modules, logs..."
                android:layout_marginBottom="24dp"/>

            <!-- ROW 9: MISSING ANIMATION WIDGETS -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Animation Image Effects:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetGlitchTextView
                android:id="@+id/xnetGlitchText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="SYSTEM GLITCH"
                android:textSize="24sp"
                android:gravity="center"
                android:layout_marginBottom="16dp"/>

            <com.xnethub.xnet_hub_theme.XnetPulseGlowView
                android:id="@+id/xnetPulseGlow"
                android:layout_width="match_parent"
                android:layout_height="60dp"
                android:layout_marginBottom="16dp"/>

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="120dp"
                android:background="#20000000"
                android:layout_marginBottom="16dp">
                
                <com.xnethub.xnet_hub_theme.XnetScanLineView
                    android:id="@+id/xnetScanLine"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"/>
                    
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="Scan Line Effect"
                    android:textColor="?attr/xnetTextPrimary"/>
            </FrameLayout>

            <com.xnethub.xnet_hub_theme.XnetImageView
                android:id="@+id/xnetImageViewTest"
                android:layout_width="120dp"
                android:layout_height="120dp"
                android:layout_gravity="center"
                android:layout_marginBottom="16dp"
                android:scaleType="centerCrop"/>
                
            <com.xnethub.xnet_hub_theme.XnetTypingTextView
                android:id="@+id/xnetTypingTextTest"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"
                android:textSize="14sp"
                android:textColor="?attr/xnetTextPrimary"/>

            <!-- ROW 10: CALLOUT CARDS -->
            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Callout Cards:"
                android:textColor="?attr/xnetTextSecondary"
                android:layout_marginBottom="8dp"/>

            <com.xnethub.xnet_hub_theme.XnetCalloutCard
                android:id="@+id/calloutInfo"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="10dp"/>

            <com.xnethub.xnet_hub_theme.XnetCalloutCard
                android:id="@+id/calloutWarning"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="10dp"/>

            <com.xnethub.xnet_hub_theme.XnetCalloutCard
                android:id="@+id/calloutError"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"/>

        </LinearLayout>
    </ScrollView>

    <!-- FLOATING CYBER ACTION BUTTON (XnetFab) FIXED ABOVE BOTTOM NAV -->
    <com.xnethub.xnet_hub_theme.XnetFab
        android:id="@+id/xnetFabTest"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_above="@id/xnetBottomNav"
        android:layout_alignParentEnd="true"
        android:layout_marginBottom="12dp"
        android:layout_marginEnd="16dp"
        android:contentDescription="Cyber Action"
        android:src="@android:drawable/ic_input_add" />

    <!-- XNET DIVIDER — separates content from bottom nav bar -->
    <com.xnethub.xnet_hub_theme.XnetDivider
        android:id="@+id/bottomNavDivider"
        android:layout_width="match_parent"
        android:layout_height="2dp"
        android:layout_above="@id/xnetBottomNav" />

    <!-- BOTTOM NAVIGATION BAR — no dock shell, icons + labels only -->
    <!-- System nav bar inset applied as paddingBottom internally -->
    <com.xnethub.xnet_hub_theme.XnetBottomNavigationView
        android:id="@+id/xnetBottomNav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:menu="@menu/menu_bottom_nav" />

    <!-- Bottom inset spacer (reserved for future use / status-bar backdrop) -->
    <FrameLayout
        android:id="@+id/bottomInsetSpacer"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_alignParentBottom="true" />
</RelativeLayout>

    <!-- XNET DRAWER PANEL -->
    <include
        android:id="@+id/drawerPanel"
        layout="@layout/xnet_drawer_content" />

</androidx.drawerlayout.widget.DrawerLayout>

```

`MainActivity.java`:

```java
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

            // TEST: Load profile picture from URL into the nav_profile item
            String testProfileUrl = "https://firebasestorage.googleapis.com/v0/b/xnet-hub.appspot.com"
                    + "/o/profile_images%2FOGP445ls6UYvfCkJtXDbhmuA5hp2.jpg"
                    + "?alt=media&token=f2f75ba3-3c8a-42bf-bb76-8429406675e7";
            bottomNav.setNavItemPhotoUrl(R.id.nav_profile, testProfileUrl);

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

```

## গুরুত্বপূর্ণ নোট

- JitPack dependency ব্যবহার করতে হলে project repository settings-এ `maven { url 'https://jitpack.io' }` থাকতে হবে।
- `THEME_SYSTEM` phone-এর system light/dark mode follow করে।
- নিজের XML layout-এ `?attr/xnet...` color ব্যবহার করলে selected theme অনুযায়ী UI auto-update হবে।
- Theme বা font change করার পর current activity-তে `recreate()` call করতে হবে।
- নতুন app-এর জন্য `XnetBaseActivity` ব্যবহার করাই recommended।
