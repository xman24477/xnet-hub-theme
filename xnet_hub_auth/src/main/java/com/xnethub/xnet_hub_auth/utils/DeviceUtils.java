package com.xnethub.xnet_hub_auth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.UUID;

public class DeviceUtils {

    private static final String PREF_NAME = "XnetDevicePrefs";
    private static final String KEY_DEVICE_UUID = "device_uuid";

    /**
     * Gets or generates a unique UUID for this app installation.
     */
    public static String getDeviceUUID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String uuid = prefs.getString(KEY_DEVICE_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply();
        }
        return uuid;
    }

    /**
     * Gets the device model (e.g., Samsung Galaxy S23).
     */
    public static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Gets the OS version (e.g., Android 13).
     */
    public static String getOSVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Normalizes a phone number input to international E.164 format (+880...).
     */
    public static String normalizePhoneNumber(String input) {
        if (input == null) return "";
        String cleaned = input.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        if (cleaned.startsWith("880")) {
            return "+" + cleaned;
        }
        if (cleaned.startsWith("0")) {
            return "+88" + cleaned;
        }
        return "+880" + cleaned;
    }
}
