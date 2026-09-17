package com.xnethub.xnet_hub_theme;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class XnetProgressDialog {
    private Dialog dialog;
    private XnetLoadingView loadingView;
    private XnetAnimatedBackdropView backdropView;

    private static class CutCornerFrameLayout extends FrameLayout {
        private final Path clipPath = new Path();
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float cutSize;

        public CutCornerFrameLayout(Context context) {
            super(context);
            cutSize = 16f * context.getResources().getDisplayMetrics().density;
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(1.5f * context.getResources().getDisplayMetrics().density);
            
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(R.attr.xnetAccentPrimary, tv, true)) {
                borderPaint.setColor(tv.data);
            } else {
                borderPaint.setColor(Color.GREEN);
            }
            setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            clipPath.reset();
            // Bottom-Left cut, Top-Right cut
            clipPath.moveTo(0, h - cutSize);
            clipPath.lineTo(cutSize, h);
            clipPath.lineTo(w, h);
            clipPath.lineTo(w, cutSize);
            clipPath.lineTo(w - cutSize, 0);
            clipPath.lineTo(0, 0);
            clipPath.close();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.clipPath(clipPath);
            super.draw(canvas);
            canvas.restore();
            canvas.drawPath(clipPath, borderPaint);
        }
    }

    public XnetProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.6f);
        }
        
        CutCornerFrameLayout container = new CutCornerFrameLayout(context);
        
        backdropView = new XnetAnimatedBackdropView(context);
        backdropView.setFrozen(true); // Stop animations for performance inside dialog
        container.addView(backdropView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        loadingView = new XnetLoadingView(context);
        loadingView.setStyle(XnetLoadingView.STYLE_TERMINAL);
        
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (260 * density);
        int height = (int) (80 * density);
        
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        container.addView(loadingView, params);
        
        dialog.setContentView(container, new ViewGroup.LayoutParams(width, height));
    }
    
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
    
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
    
    public void setCancelable(boolean cancelable) {
        if (dialog != null) {
            dialog.setCancelable(cancelable);
        }
    }
    
    public void setMessage(String message) {
        if (loadingView != null && message != null) {
            if (message.contains("Central Auth")) {
                loadingView.setText("AUTHENTICATING...");
            } else {
                loadingView.setText(message);
            }
        }
    }
}
