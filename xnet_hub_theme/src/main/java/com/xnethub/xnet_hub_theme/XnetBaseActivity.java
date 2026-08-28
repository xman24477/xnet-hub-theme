package com.xnethub.xnet_hub_theme;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class XnetBaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        XnetThemeManager.applyNightMode(this);
        XnetThemeManager.applyThemeToActivity(this);
        super.onCreate(savedInstanceState);
        XnetEdgeToEdge.enable(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (isAnimatedBackdropEnabled()) {
            setupAnimatedLayout(getLayoutInflater().inflate(layoutResID, null));
        } else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if (isAnimatedBackdropEnabled()) {
            setupAnimatedLayout(view);
        } else {
            super.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (isAnimatedBackdropEnabled()) {
            setupAnimatedLayout(view, params);
        } else {
            super.setContentView(view, params);
        }
    }

    private boolean isAnimatedBackdropEnabled() {
        TypedValue typedValue = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.xnetEnableAnimatedBackdrop, typedValue, true)) {
            return typedValue.data != 0;
        }
        return false; // Default false
    }

    private void setupAnimatedLayout(View contentView) {
        setupAnimatedLayout(contentView, null);
    }

    private void setupAnimatedLayout(View contentView, @Nullable ViewGroup.LayoutParams contentParams) {
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Background animated view
        XnetAnimatedBackdropView backdropView = new XnetAnimatedBackdropView(this);
        backdropView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootLayout.addView(backdropView);

        // Actual content view
        if (contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
        if (contentParams != null) {
            rootLayout.addView(contentView, contentParams);
        } else {
            rootLayout.addView(contentView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        super.setContentView(rootLayout);
    }
}
