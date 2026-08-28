package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * XnetBottomNavigationView
 *
 * Official Xnet Hub Theme Library custom bottom navigation view.
 * Built on top of Material {@link BottomNavigationView}.
 *
 * Design:
 *   - NO background shell / outline stroke — icons and text labels only.
 *   - Optional top divider line drawn via {@link XnetDivider}.
 *   - X-Cyber themes:
 *       Selected item  → 8dp cut-corner solid accent-fill capsule + high-contrast icon & label.
 *       Unselected     → white icon & label (always readable on dark surfaces).
 *       Icon tinting applied via ColorFilter — compatible with both Vector AND PNG drawables.
 *   - Classic / Dark / Light themes → standard Material BottomNavigationView behaviour.
 *   - System navigation bar inset handled internally via paddingBottom (edge-to-edge safe).
 */
public class XnetBottomNavigationView extends BottomNavigationView
        implements NavigationBarView.OnItemSelectedListener {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private boolean mIsCyberTheme = false;

    private int mAccentColor  = 0xFF00FFCC;
    private int mTextPrimary  = Color.WHITE;
    private int mTextSecondary = Color.GRAY;

    /** Menu item IDs whose icon is a photo — tinting must be skipped for these. */
    private final Set<Integer> mPhotoItemIds = new HashSet<>();

    private OnItemSelectedListener mUserListener;
    private Typeface mRajdhaniFont;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    public XnetBottomNavigationView(@NonNull Context context) {
        this(context, null);
    }

    public XnetBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetBottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------

    private void init(Context context) {
        mIsCyberTheme  = resolveBoolean(context, R.attr.xnetIsCyberTheme, false);
        mAccentColor   = resolveColor(context, R.attr.xnetAccentPrimary, 0xFF00FFCC);
        mTextPrimary   = resolveColor(context, R.attr.xnetTextPrimary, Color.WHITE);
        mTextSecondary = resolveColor(context, R.attr.xnetTextSecondary, Color.GRAY);

        final float density = context.getResources().getDisplayMetrics().density;

        if (mIsCyberTheme) {
            // Load Rajdhani font for cyber labels
            try {
                mRajdhaniFont = context.getResources().getFont(R.font.rajdhani);
            } catch (Exception ignored) {}

            // No background shell — transparent surface only
            setBackground(null);
            setItemActiveIndicatorEnabled(false);

            // Increase icon size — Material default is 24dp, we use 28dp
            int iconSizePx = (int) (26 * density);
            setItemIconSize(iconSizePx);

            // Disable Material's default Drawable tinting. We handle all icon tinting manually
            // in updateCyberItems() via ImageView.setColorFilter. This allows us to skip
            // tinting on specific items (like profile photos).
            setItemIconTintList(null);
            setItemTextColor(ColorStateList.valueOf(mTextPrimary));

            // Show icon + label on every tab
            setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

            // Intercept selection events so we can refresh per-item styling
            super.setOnItemSelectedListener(this);

        } else {
            // Classic / Dark / Light: standard Material behaviour
            setBackground(null);
            setItemIconSize((int) (26 * density));
            setItemIconTintList(createStateList(mTextSecondary, mAccentColor));
            setItemTextColor(createStateList(mTextSecondary, mAccentColor));
            setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        }

        // Edge-to-edge: push content above system navigation bar on both theme types
        ViewCompat.setOnApplyWindowInsetsListener(this, (view, insets) -> {
            androidx.core.graphics.Insets navInsets =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            int extraPad = (int) (8 * density);
            setPadding(getPaddingLeft(), getPaddingTop(),
                       getPaddingRight(), navInsets.bottom + extraPad);
            return insets;
        });

        setWillNotDraw(false);
    }

    // -----------------------------------------------------------------------
    // Item selection listener pass-through
    // -----------------------------------------------------------------------

    @Override
    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        if (mIsCyberTheme) {
            // Store caller's listener; we intercept at super level
            this.mUserListener = listener;
        } else {
            super.setOnItemSelectedListener(listener);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean handled = true;
        if (mUserListener != null) {
            handled = mUserListener.onNavigationItemSelected(item);
        } else if (!mIsCyberTheme) {
            // If we are not intercepting for cyber theme, pass to super if needed, 
            // but BottomNavigationView doesn't require calling super.onNavigationItemSelected
            // We just let the caller handle it.
        }
        
        // Refresh styling (Cyber theme handles its own, Classic theme needs photo tints cleared)
        post(() -> {
            if (mIsCyberTheme) {
                updateCyberItems();
            } else {
                for (Integer photoId : mPhotoItemIds) {
                    clearTintOnPhotoItem(photoId);
                }
            }
        });
        return handled;
    }

    // -----------------------------------------------------------------------
    // Layout hook — apply styling after children are measured/placed
    // -----------------------------------------------------------------------

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mIsCyberTheme) {
            updateCyberItems();
        } else {
            for (Integer photoId : mPhotoItemIds) {
                clearTintOnPhotoItem(photoId);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Per-item cyber styling
    // -----------------------------------------------------------------------

    private void updateCyberItems() {
        if (!mIsCyberTheme) return;

        float density = getResources().getDisplayMetrics().density;
        // For filled selected items, derive high-contrast foreground automatically
        int selectedOnFill = contrastColor(mAccentColor);

        ViewGroup menuView = (ViewGroup) getChildAt(0);
        if (menuView == null) return;

        int selectedId = getSelectedItemId();

        // Guarantee labels are always visible
        setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            boolean isSelected = (itemView.getId() == selectedId);
            boolean isPhotoItem = mPhotoItemIds.contains(itemView.getId());

            // ── Scale: selected item zooms to 1.15×, others reset to 1.0× ──────
            float targetScale = isSelected ? 1.15f : 1.0f;
            itemView.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .setDuration(180)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();

            // ── Background ──────────────────────────────────────────────────
            if (isSelected) {
                // 8dp cut-corner solid accent-fill capsule (same shape as XnetTabLayout)
                Drawable fillBg = XnetTabLayout.createCyberTabShape(mAccentColor, 8f, density);
                itemView.setBackground(fillBg);
            } else {
                itemView.setBackground(null);
            }

            // ── Foreground colour ────────────────────────────────────────────
            int color = isSelected ? selectedOnFill : mTextPrimary;

            // ── Apply to every child View inside the item ────────────────────
            // Photo items: style label text only, skip image tinting
            if (itemView instanceof ViewGroup) {
                if (isPhotoItem) {
                    stylePhotoItemLabel((ViewGroup) itemView, color, isSelected);
                    clearTintOnPhotoItem(itemView.getId());   // ensure no tint lingers
                } else {
                    applyColorToItemChildren((ViewGroup) itemView, color, isSelected);
                }
            }
        }
    }

    /**
     * Styles ONLY the label TextViews of a photo-icon item.
     * The ImageView (profile picture) is intentionally left untouched.
     */
    private void stylePhotoItemLabel(ViewGroup group, int labelColor, boolean isSelected) {
        int idSmall = com.google.android.material.R.id.navigation_bar_item_small_label_view;
        int idLarge = com.google.android.material.R.id.navigation_bar_item_large_label_view;
        View smallLabel = group.findViewById(idSmall);
        View largeLabel = group.findViewById(idLarge);
        if (smallLabel != null) smallLabel.setVisibility(View.GONE);
        if (largeLabel instanceof TextView) {
            TextView tv = (TextView) largeLabel;
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(labelColor);
            if (mRajdhaniFont != null) {
                tv.setTypeface(mRajdhaniFont, isSelected ? Typeface.BOLD : Typeface.NORMAL);
            }
        }
    }

    /**
     * Styles every icon and label inside a single nav item view.
     *
     * Material's BottomNavigationView places TWO TextViews per item:
     *   • navigation_bar_item_small_label_view  — shown (smaller) when unselected
     *   • navigation_bar_item_large_label_view  — shown (larger)  when selected
     *
     * When LABEL_VISIBILITY_LABELED is active, both are technically visible at the
     * same time (just scaled differently by Material). We take over full control:
     *   - Always HIDE the small label (prevents the double-text ghost effect).
     *   - Always SHOW the large label with our custom colour and Rajdhani typeface.
     *   - Tint every ImageView with SRC_IN (works for both Vector and PNG drawables).
     */
    private void applyColorToItemChildren(ViewGroup group, int color, boolean isSelected) {
        // Material's stable resource IDs for the two label TextViews
        int idSmall = com.google.android.material.R.id.navigation_bar_item_small_label_view;
        int idLarge = com.google.android.material.R.id.navigation_bar_item_large_label_view;

        // Try the named views first (Material-guaranteed path)
        View smallLabel = group.findViewById(idSmall);
        View largeLabel = group.findViewById(idLarge);

        if (smallLabel != null || largeLabel != null) {
            // Hide the small label unconditionally — we never want the double effect
            if (smallLabel != null) smallLabel.setVisibility(View.GONE);

            // Style and show the large label
            if (largeLabel instanceof TextView) {
                TextView tv = (TextView) largeLabel;
                tv.setVisibility(View.VISIBLE);
                tv.setTextColor(color);
                if (mRajdhaniFont != null) {
                    tv.setTypeface(mRajdhaniFont, isSelected ? Typeface.BOLD : Typeface.NORMAL);
                }
            }
        }

        // Tint every ImageView in the hierarchy (icon, badge background, etc.)
        // SRC_IN works for both VectorDrawable and PngDrawable — PNG-safe!
        tintImageViews(group, color);
    }

    /** Walks the view tree and applies a SRC_IN colour filter to every ImageView found.
     *  @param skipItemId menu item ID to skip tinting (e.g. photo profile icon) */
    private void tintImageViews(ViewGroup group, int color) {
        // Check if this group belongs to a photo-icon item (skip tinting)
        if (mPhotoItemIds.contains(group.getId())) return;

        for (int j = 0; j < group.getChildCount(); j++) {
            View child = group.getChildAt(j);
            if (child instanceof android.widget.ImageView) {
                ((android.widget.ImageView) child)
                        .setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            } else if (child instanceof ViewGroup) {
                tintImageViews((ViewGroup) child, color);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Public API — XnetImageView-styled icon loading
    // -----------------------------------------------------------------------

    /**
     * Loads a photo from {@code imageUrl} in a background thread, renders it
     * with the same hexagon-clip + neon stroke style as {@link XnetImageView}
     * (or circle-clip for Classic themes), then sets it as the nav item icon.
     *
     * The item is registered internally so its icon is NEVER colour-filtered,
     * preserving the original photo colours regardless of selection state.
     *
     * @param menuItemId  Menu item ID, e.g. {@code R.id.nav_profile}.
     * @param imageUrl    Full HTTP/HTTPS URL (Firebase Storage, CDN, …).
     */
    public void setNavItemPhotoUrl(int menuItemId, String imageUrl) {
        mPhotoItemIds.add(menuItemId);

        int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
        XnetNavIconHelper.loadFromUrl(getContext(), imageUrl, sizePx, icon -> {
            MenuItem item = getMenu().findItem(menuItemId);
            if (item != null) {
                item.setIcon(icon);
                clearTintOnPhotoItem(menuItemId);
                post(this::updateCyberItems);
            }
        });
    }

    /**
     * Sets a PNG or vector drawable resource as a nav item icon, rendered with
     * the XnetImageView clip + stroke style.
     *
     * Unlike {@link #setNavItemPhotoUrl}, this method IS synchronous and can be
     * called directly on the main thread.
     *
     * Tinting is NOT skipped for drawable icons — they will receive the
     * theme accent colour filter so they match other nav items visually.
     * Pass {@code useXnetStyle = false} to skip the clip and just use the raw drawable.
     *
     * @param menuItemId   Menu item ID.
     * @param drawableRes  Drawable resource id (PNG or VectorDrawable).
     * @param useXnetStyle If {@code true}, applies hexagon/circle clip + stroke.
     */
    public void setNavItemDrawable(int menuItemId, int drawableRes, boolean useXnetStyle) {
        MenuItem item = getMenu().findItem(menuItemId);
        if (item == null) return;
        if (useXnetStyle) {
            int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
            item.setIcon(XnetNavIconHelper.fromDrawableRes(getContext(), drawableRes, sizePx));
        } else {
            item.setIcon(drawableRes);
        }
        post(this::updateCyberItems);
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /** Clears the ColorFilter from the icon ImageView of a specific menu item. */
    private void clearTintOnPhotoItem(int menuItemId) {
        ViewGroup menuView = (ViewGroup) getChildAt(0);
        if (menuView == null) return;
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            if (itemView.getId() == menuItemId) {
                clearImageViewFilters(itemView);
                return;
            }
        }
    }

    private void clearImageViewFilters(View view) {
        if (view instanceof android.widget.ImageView) {
            android.widget.ImageView iv = (android.widget.ImageView) view;
            iv.clearColorFilter();
            androidx.core.widget.ImageViewCompat.setImageTintList(iv, null);
        } else if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                clearImageViewFilters(vg.getChildAt(i));
            }
        }
    }

    /** Creates a checked/unchecked ColorStateList (used for Classic themes). */
    private static ColorStateList createStateList(int normalColor, int selectedColor) {
        return new ColorStateList(
            new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
            },
            new int[]{selectedColor, normalColor}
        );
    }

    /**
     * Returns black {@code #020A02} for light/neon backgrounds and
     * {@code Color.WHITE} for dark backgrounds, so icon + text always
     * contrast against the accent fill.
     */
    private static int contrastColor(int bgColor) {
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >>  8) & 0xFF;
        int b =  bgColor        & 0xFF;
        double luminance = 0.299 * r + 0.587 * g + 0.114 * b;
        return luminance > 130 ? 0xFF121212 : Color.WHITE;
    }

    private static int resolveColor(Context context, int attr, int fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try { return ta.getColor(0, fallback); } finally { ta.recycle(); }
    }

    private static boolean resolveBoolean(Context context, int attr, boolean fallback) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try { return ta.getBoolean(0, fallback); } finally { ta.recycle(); }
    }

    // -----------------------------------------------------------------------
    // Public API — Notification Badges
    // -----------------------------------------------------------------------

    /**
     * Sets a notification badge on a specific menu item.
     * Uses Material Badge for Classic themes, and XnetBadgeView for Cyber themes.
     */
    public void setNotificationBadge(int menuItemId, int count) {
        if (!mIsCyberTheme) {
            // Classic: use Material Badge
            com.google.android.material.badge.BadgeDrawable badge = getOrCreateBadge(menuItemId);
            if (count > 0) {
                badge.setNumber(count);
                badge.setVisible(true);
            } else {
                badge.setVisible(false);
                removeBadge(menuItemId);
            }
            // Remove Cyber Badge if it exists from previous theme
            removeXnetBadgeFromItem(menuItemId);
        } else {
            // Cyber: add XnetBadgeView inside the BottomNavigationItemView
            ViewGroup menuView = (ViewGroup) getChildAt(0);
            if (menuView == null) return;
            for (int i = 0; i < menuView.getChildCount(); i++) {
                View itemView = menuView.getChildAt(i);
                if (itemView.getId() == menuItemId && itemView instanceof ViewGroup) {
                    addXnetBadgeToItem((ViewGroup) itemView, count);
                    break;
                }
            }
            // Remove Material Badge if it exists from previous theme
            removeBadge(menuItemId);
        }
    }

    private void removeXnetBadgeFromItem(int menuItemId) {
        ViewGroup menuView = (ViewGroup) getChildAt(0);
        if (menuView == null) return;
        for (int i = 0; i < menuView.getChildCount(); i++) {
            View itemView = menuView.getChildAt(i);
            if (itemView.getId() == menuItemId && itemView instanceof ViewGroup) {
                View badgeView = itemView.findViewWithTag("xnet_badge");
                if (badgeView != null) {
                    ((ViewGroup) itemView).removeView(badgeView);
                }
                break;
            }
        }
    }

    private void addXnetBadgeToItem(ViewGroup itemView, int count) {
        XnetBadgeView badgeView = itemView.findViewWithTag("xnet_badge");
        if (count <= 0) {
            if (badgeView != null) itemView.removeView(badgeView);
            return;
        }

        if (badgeView == null) {
            badgeView = new XnetBadgeView(getContext());
            badgeView.setTag("xnet_badge");
            // Set elevation so it appears above the item layout (like background pills)
            ViewCompat.setElevation(badgeView, 10f);
            
            // BottomNavigationItemView is a FrameLayout.
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
            
            // Add some margin so it sits exactly on the top right of the icon
            float density = getResources().getDisplayMetrics().density;
            params.topMargin = (int) (6 * density);
            params.leftMargin = (int) (20 * density); // Push to the right of center
            
            itemView.addView(badgeView, params);
        }
        badgeView.setCount(count);
    }
}
