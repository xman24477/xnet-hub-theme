package com.xnethub.xnet_hub_auth.auth;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AuthManager {

    private static final String TAG = "AuthManager";
    private static final String CLOUD_FUNCTION_URL = "https://getcustomtoken-hz65u25fna-uc.a.run.app";
    private static final int TIMEOUT_MS = 15000;

    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Call this method after the user successfully logs into Central Auth (Xnet Hub).
     * Fetches custom token and signs into local app instance.
     */
    public static void signInWithCentralAuth(Context context, AuthCallback callback) {
        FirebaseAuth centralAuth;
        try {
            centralAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralAuth = FirebaseAuth.getInstance();
        }
        
        FirebaseUser currentUser = centralAuth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("No user found in Central Auth.");
            return;
        }

        Log.d(TAG, "Getting ID Token from Central Auth user: " + currentUser.getUid());
        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String idToken = task.getResult().getToken();
                Log.d(TAG, "Successfully retrieved ID Token. Fetching Custom Token...");
                fetchCustomTokenAndLogin(idToken, callback);
            } else {
                Log.e(TAG, "Failed to get ID token", task.getException());
                String errorMsg = (task.getException() != null && task.getException().getMessage() != null)
                        ? task.getException().getMessage()
                        : "Unknown token error";
                callback.onError("Failed to get ID token: " + errorMsg);
            }
        });
    }

    private static void fetchCustomTokenAndLogin(String idToken, AuthCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(CLOUD_FUNCTION_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + idToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = "{\"targetProject\": \"xnetsmart\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Cloud Function HTTP Response Code: " + responseCode);
                if (responseCode == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    String response = scanner.useDelimiter("\\A").next();
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response);
                    String customToken = jsonResponse.getString("customToken");

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        FirebaseAuth.getInstance().signInWithCustomToken(customToken)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Successfully signed in via Custom Token!");
                                        callback.onSuccess();
                                    } else {
                                        Log.e(TAG, "Custom Token Login Failed", task.getException());
                                        String errorMsg = (task.getException() != null && task.getException().getMessage() != null)
                                                ? task.getException().getMessage()
                                                : "Unknown authentication error";
                                        callback.onError("Custom Token Login Failed: " + errorMsg);
                                    }
                                });
                    });

                } else {
                    Scanner scanner = (conn.getErrorStream() != null) ? new Scanner(conn.getErrorStream()) : null;
                    String response = (scanner != null && scanner.hasNext()) ? scanner.useDelimiter("\\A").next() : "Unknown error";
                    if (scanner != null) scanner.close();
                    Log.e(TAG, "Cloud Function error: " + responseCode + " - " + response);
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Server returned " + responseCode + ": " + response));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching custom token", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError("Exception: " + e.getMessage()));
            }
        }).start();
    }
}
