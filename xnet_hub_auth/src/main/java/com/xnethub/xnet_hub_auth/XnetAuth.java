package com.xnethub.xnet_hub_auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.xnethub.xnet_hub_auth.auth.AuthActivity;

public class XnetAuth {

    /**
     * Launches the Centralized AuthActivity from any client application.
     */
    public static void launchAuth(Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Launches the Centralized AuthActivity for result.
     */
    public static void launchAuth(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, AuthActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Creates an Intent to launch AuthActivity.
     */
    public static Intent createAuthIntent(Context context) {
        return new Intent(context, AuthActivity.class);
    }
}
