package com.xnethub.xnet_hub_auth.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    /**
     * Call this when the user tries to log in.
     * Checks if there's an existing session. Shows prompt if there is one on a different device.
     */
    public static void handleLoginSession(Activity activity, FirebaseUser user, Runnable onSuccess) {
        if (user == null) {
            if (onSuccess != null) onSuccess.run();
            return;
        }
        
        String uid = user.getUid();
        FirebaseFirestore centralDb;
        try {
            centralDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralDb = FirebaseFirestore.getInstance();
        }

        DocumentReference userRef = centralDb.collection("users").document(uid);
        String appName = activity.getPackageName();
        
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Map<String, Object> data = task.getResult().getData();
                if (data != null && data.containsKey("active_sessions")) {
                    Map<String, Object> sessions = (Map<String, Object>) data.get("active_sessions");
                    String currentDeviceUuid = DeviceUtils.getDeviceUUID(activity);
                    String existingSessionUuid = (String) sessions.get(appName);
                    
                    if (existingSessionUuid != null && !existingSessionUuid.equals(currentDeviceUuid)) {
                        showSessionReplacePrompt(activity, userRef, currentDeviceUuid, existingSessionUuid, onSuccess);
                        return;
                    }
                }
            }
            
            updateSessionAndHistory(activity, userRef, DeviceUtils.getDeviceUUID(activity), null, "LOGIN", onSuccess);
        });
    }

    private static void showSessionReplacePrompt(Activity activity, DocumentReference userRef, String newUuid, String oldUuid, Runnable onSuccess) {
        new AlertDialog.Builder(activity)
                .setTitle("Active Session Detected")
                .setMessage("Your account is currently logged in on another device. Would you like to log out from the previous device and log in here?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    updateSessionAndHistory(activity, userRef, newUuid, oldUuid, "SESSION_REVOKED", onSuccess);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();
                    Toast.makeText(activity, "Login cancelled", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private static void updateSessionAndHistory(Context context, DocumentReference userRef, String newUuid, String oldUuid, String oldAction, Runnable onSuccess) {
        FirebaseFirestore centralDb;
        try {
            centralDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralDb = FirebaseFirestore.getInstance();
        }

        WriteBatch batch = centralDb.batch();
        String appName = context.getPackageName();
        
        Map<String, Object> sessionUpdate = new HashMap<>();
        sessionUpdate.put("active_sessions." + appName, newUuid);
        batch.update(userRef, sessionUpdate);
        
        if (oldUuid != null) {
            DocumentReference oldHistoryRef = userRef.collection("login_history").document();
            Map<String, Object> oldHistory = new HashMap<>();
            oldHistory.put("action", oldAction);
            oldHistory.put("app_name", appName);
            oldHistory.put("device_id", oldUuid);
            oldHistory.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
            batch.set(oldHistoryRef, oldHistory);
        }
        
        DocumentReference newHistoryRef = userRef.collection("login_history").document();
        Map<String, Object> newHistory = new HashMap<>();
        newHistory.put("action", "LOGIN");
        newHistory.put("app_name", appName);
        newHistory.put("device_model", DeviceUtils.getDeviceModel());
        newHistory.put("os_version", DeviceUtils.getOSVersion());
        newHistory.put("device_id", newUuid);
        newHistory.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        batch.set(newHistoryRef, newHistory);
        
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                Log.e("SessionManager", "Session update batch commit failed", task.getException());
                Toast.makeText(context, "Session update failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void handleManualLogout(Context context, FirebaseUser user, Runnable onSuccess) {
        if (user == null) {
            if (onSuccess != null) onSuccess.run();
            return;
        }
        
        String uid = user.getUid();
        FirebaseFirestore centralDb;
        try {
            centralDb = FirebaseFirestore.getInstance(FirebaseApp.getInstance("XnetHubAuth"));
        } catch (Exception e) {
            centralDb = FirebaseFirestore.getInstance();
        }

        DocumentReference userRef = centralDb.collection("users").document(uid);
        WriteBatch batch = centralDb.batch();
        String appName = context.getPackageName();
        
        Map<String, Object> sessionUpdate = new HashMap<>();
        sessionUpdate.put("active_sessions." + appName, com.google.firebase.firestore.FieldValue.delete());
        batch.update(userRef, sessionUpdate);
        
        DocumentReference historyRef = userRef.collection("login_history").document();
        Map<String, Object> history = new HashMap<>();
        history.put("action", "LOGOUT");
        history.put("app_name", appName);
        history.put("device_model", DeviceUtils.getDeviceModel());
        history.put("os_version", DeviceUtils.getOSVersion());
        history.put("device_id", DeviceUtils.getDeviceUUID(context));
        history.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        batch.set(historyRef, history);
        
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (onSuccess != null) onSuccess.run();
            } else {
                Log.e("SessionManager", "Logout session batch commit failed", task.getException());
                Toast.makeText(context, "Logout session update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
