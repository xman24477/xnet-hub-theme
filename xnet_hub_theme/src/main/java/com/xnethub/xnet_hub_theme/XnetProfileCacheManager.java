package com.xnethub.xnet_hub_theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;

/**
 * XnetProfileCacheManager
 *
 * Lightweight, zero-permission offline cache manager for user profile pictures.
 * Uses Android's sandboxed internal storage (context.getFilesDir()).
 *
 * Key features:
 * - 100% permissionless: No READ/WRITE_EXTERNAL_STORAGE needed.
 * - Google Play Store compliant: Zero privacy policy hurdles.
 * - Namespaced by User ID (UID): Completely prevents cross-account photo leaks.
 * - Binary .dat extension: Prevents OS media scanners from indexing the cache.
 */
public final class XnetProfileCacheManager {

    private static final String FILE_PREFIX = "profile_";
    private static final String FILE_EXT = ".dat";

    private XnetProfileCacheManager() {}

    /**
     * Resolves the private cache file for a specific user ID.
     */
    public static File getProfileFile(Context context, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            userId = "default_user";
        }
        return new File(context.getFilesDir(), FILE_PREFIX + userId + FILE_EXT);
    }

    /**
     * Checks if a cached profile photo exists locally for this user.
     */
    public static boolean hasCachedProfile(Context context, String userId) {
        File file = getProfileFile(context, userId);
        return file.exists() && file.length() > 0;
    }

    /**
     * Loads the high-resolution cached profile Bitmap for a specific user.
     * Operates completely offline.
     *
     * @return Decoded {@link Bitmap}, or {@code null} if no cached file exists.
     */
    @Nullable
    public static Bitmap getCachedProfile(Context context, String userId) {
        File file = getProfileFile(context, userId);
        if (file.exists() && file.length() > 0) {
            try {
                return BitmapFactory.decodeFile(file.getAbsolutePath());
            } catch (OutOfMemoryError | Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Saves or overwrites a profile Bitmap in private internal storage.
     * Saves at high quality (90% JPEG) so it looks crisp both in full profile view
     * and scaled down in bottom navigation bars.
     *
     * @param context Activity or Application context.
     * @param userId  Unique user identifier (e.g. Firebase Auth UID).
     * @param bitmap  Source bitmap to cache.
     */
    public static void saveProfileToCache(Context context, String userId, Bitmap bitmap) {
        if (bitmap == null) return;
        File file = getProfileFile(context, userId);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
        } catch (Exception ignored) {
        }
    }

    /**
     * Deletes the user's cached profile picture on logout or user switch.
     */
    public static void clearProfileCache(Context context, String userId) {
        try {
            File file = getProfileFile(context, userId);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }
}
