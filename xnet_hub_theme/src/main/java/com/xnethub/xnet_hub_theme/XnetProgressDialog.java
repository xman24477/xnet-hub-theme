package com.xnethub.xnet_hub_theme;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;

public class XnetProgressDialog {
    private Dialog dialog;
    private XnetLoadingView loadingView;

    public XnetProgressDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        
        loadingView = new XnetLoadingView(context);
        loadingView.setStyle(XnetLoadingView.STYLE_TERMINAL);
        
        float density = context.getResources().getDisplayMetrics().density;
        int width = (int) (220 * density);
        int height = (int) (60 * density);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        layout.addView(loadingView, params);
        
        dialog.setContentView(layout);
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
            // Strip out sensitive internal backend texts if they are accidentally passed,
            // or just use generic LOADING... for anything mentioning Central Auth.
            if (message.contains("Central Auth")) {
                loadingView.setText("AUTHENTICATING...");
            } else {
                loadingView.setText(message);
            }
        }
    }
}