package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

/**
 * XnetCard
 *
 * Official Xnet Hub Theme Library Custom Card Widget.
 * Automatically adapts to all 7 Xnet Core themes, applying cut corners,
 * stroke borders, and glass surface overlays.
 */
public class XnetCard extends MaterialCardView {

    public XnetCard(@NonNull Context context) {
        this(context, null);
    }

    public XnetCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialCardViewStyle);
    }

    public XnetCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
