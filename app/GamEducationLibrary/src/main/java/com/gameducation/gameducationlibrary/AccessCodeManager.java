package com.gameducation.gameducationlibrary;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;

public class AccessCodeManager {

    private static final String TAG = "AccessCodeManager";
    private static final String PREFERENCES_NAME = "AccessCodePreferences";
    private static final String ACCESS_CODE_KEY = "AccessCodeKey";

    public static void saveAccessCode(Context context, String accessCode) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            preferences.edit().putString(ACCESS_CODE_KEY, accessCode).apply();

            Log.d(TAG, "Saved access code: " + accessCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAccessCode(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            preferences.edit().remove(ACCESS_CODE_KEY).apply();

            Log.d(TAG, "Cleared access code");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAccessCode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(ACCESS_CODE_KEY, null);
    }
}
