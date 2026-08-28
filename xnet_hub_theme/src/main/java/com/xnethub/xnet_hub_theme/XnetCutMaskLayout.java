package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * XnetCutMaskLayout
 *
 * Custom FrameLayout container that clips child drawing (including animated backdrops)
 * to match the exact cut-corner geometry of X-Cyber themes (22dp top-right & bottom-right cuts),
 * eliminating any square corners poking out past cut stroke outlines.
 */
public class XnetCutMaskLayout extends FrameLayout {

    private final Path clipPath = new Path();
    private float cutSizePx;
    private boolean cutCornersEnabled = true;

    public XnetCutMaskLayout(@NonNull Context context) {
        this(context, null);
    }

    public XnetCutMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XnetCutMaskLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        cutSizePx = dp(22f);
        setWillNotDraw(false);
        checkCyberTheme();
    }

    private void checkCyberTheme() {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.xnetIsCyberTheme, tv, true)) {
            cutCornersEnabled = tv.data != 0;
        } else {
            cutCornersEnabled = false;
        }
    }

    public void setCutSizeDp(float cutSizeDp) {
        float newCutPx = dp(cutSizeDp);
        if (Math.abs(cutSizePx - newCutPx) < 0.5f) {
            return;
        }
        cutSizePx = newCutPx;
        rebuildClipPath(getWidth(), getHeight());
        invalidate();
    }

    public void setCutCornersEnabled(boolean enabled) {
        if (this.cutCornersEnabled == enabled) {
            return;
        }
        this.cutCornersEnabled = enabled;
        rebuildClipPath(getWidth(), getHeight());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        checkCyberTheme();
        rebuildClipPath(w, h);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (cutCornersEnabled && !clipPath.isEmpty()) {
            int saveCount = canvas.save();
            canvas.clipPath(clipPath);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    private void rebuildClipPath(int width, int height) {
        clipPath.reset();
        if (width <= 0 || height <= 0 || !cutCornersEnabled) {
            return;
        }
        float cut = Math.min(cutSizePx, Math.min(width, height) / 2f);
        // Top-left: straight (0, 0)
        clipPath.moveTo(0f, 0f);
        // Top-right: cut corner (width - cut, 0) -> (width, cut)
        clipPath.lineTo(width - cut, 0f);
        clipPath.lineTo(width, cut);
        // Bottom-right: cut corner (width, height - cut) -> (width - cut, height)
        clipPath.lineTo(width, height - cut);
        clipPath.lineTo(width - cut, height);
        // Bottom-left: straight (0, height)
        clipPath.lineTo(0f, height);
        clipPath.close();
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
