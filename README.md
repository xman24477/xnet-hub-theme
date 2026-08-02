# Xnet Hub Core Libraries (Theme & Auth SDK)

Official multi-module Android library ecosystem for Xnet applications (`Xnet Hub`, `Xnet Smart`, `DriveX`, and new company apps).

This project contains two decoupled Android library modules:
1. **`:xnet_hub_theme`** — Pure UI Theme System & Design Kit (19 Cyber/Classic themes, Custom Fonts, Custom Animated Backdrops, Base Activities, and Cyber Buttons/Cards).
2. **`:xnet_hub_auth`** — Centralized Authentication SDK (Login, Phone Verification, OTP Countdown, Registration, Password Recovery, and Multi-Device Active Session Manager).

---

## 🛠️ Installation via JitPack

Add the JitPack repository to your root `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Option A: Include Only UI & Theme System
If your app only requires the Cyber/Classic design system and custom UI components without authentication:

```groovy
dependencies {
    implementation 'com.github.xman24477:Xnet-Hub-Theme:xnet_hub_theme:v1.0.0'
}
```

### Option B: Include Central Authentication (+ UI Theme System)
If your app requires Centralized Login, OTP Verification, and User Sessions:

```groovy
dependencies {
    implementation 'com.github.xman24477:Xnet-Hub-Theme:xnet_hub_auth:v1.0.0'
}
```
*Note: `:xnet_hub_auth` automatically includes `:xnet_hub_theme` as a transitive dependency.*

---

## 🚀 Usage Guide

### 1. Dynamic Theme Switching (`xnet_hub_theme`)

#### In `AndroidManifest.xml`:
```xml
<activity
    android:name=".MainActivity"
    android:theme="@style/Theme.XnetCore.CyberGreen" />
```

#### Programmatically switching themes:
```java
// Apply a theme dynamically
XnetThemeManager.applyTheme(activity, XnetThemeManager.XnetTheme.CYBER_BLUE);

// Listen to theme changes across activities
XnetThemeManager.registerThemeChangeListener(theme -> {
    // Theme updated
});
```

#### Using Cyber Animated Backdrop in XML:
```xml
<com.xnethub.xnet_hub_theme.XnetAnimatedBackdropView
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

---

### 2. Centralized Authentication (`xnet_hub_auth`)

Launch the pre-built, theme-driven Central Authentication flow in just 1 line of code:

```java
// Launch Central Auth Activity
Intent intent = new Intent(this, com.xnethub.xnet_hub_auth.auth.AuthActivity.class);
startActivityForResult(intent, AUTH_REQUEST_CODE);
```

#### Active Session & Multi-Device Logout Handling:
```java
// Check or register active session on user login
SessionManager.handleLoginSession(currentActivity, currentUser, () -> {
    // Session verified successfully
});

// Manual Logout
SessionManager.handleManualLogout(context, currentUser, () -> {
    // Successfully logged out from central registry
});
```

---

## 🎨 Available Themes (19 Variants)
- `Theme.XnetCore.Light` / `Theme.XnetCore.Dark`
- `Theme.XnetCore.CyberGreen` / `Theme.XnetCore.CyberGreen.System`
- `Theme.XnetCore.CyberBlue` / `Theme.XnetCore.CyberBlue.System`
- `Theme.XnetCore.CyberBlack` / `Theme.XnetCore.CyberBlack.System`
- `Theme.XnetCore.CyberOrange` / `Theme.XnetCore.CyberOrange.System`
- `Theme.XnetCore.CyberRGB` / `Theme.XnetCore.CyberRGB.System`
- Custom Font Overlays: Rajdhani, Orbitron, ShareTechMono.

---

## 📄 License
Copyright © 2026 Xnet Hub Corporation. All Rights Reserved.
