package com.xnethub.xnet_hub_theme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class XnetThemeTransitionHelper {
    
    private static Bitmap sSnapshot = null;
    
    /**
     * Takes a screenshot of the current activity, saves it, and restarts the activity with no animation.
     */
    public static void restartSmoothly(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        
        // Take screenshot
        try {
            sSnapshot = Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sSnapshot);
            decorView.draw(canvas);
        } catch (Exception e) {
            sSnapshot = null;
        }
        
        // Restart activity
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.finish();
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
    
    /**
     * Call this in onCreate() of your Activity after setContentView().
     * It will overlay the previous screenshot and fade it out smoothly.
     */
    public static void handleTransition(Activity activity) {
        if (sSnapshot != null) {
            final ImageView overlay = new ImageView(activity);
            overlay.setImageBitmap(sSnapshot);
            overlay.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.setScaleType(ImageView.ScaleType.FIT_XY);
            
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(overlay);
            
            // Fade out
            overlay.animate()
                   .alpha(0f)
                   .setDuration(400)
                   .withEndAction(() -> {
                       decorView.removeView(overlay);
                       sSnapshot.recycle();
                       sSnapshot = null;
                   })
                   .start();
        }
    }
}
