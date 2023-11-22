package com.gameducation.gameducationlibrary;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;

public class AccessCodeManager {

    private static final String PREFERENCES_NAME = "AccessCodePreferences";
    private static final String ACCESS_CODE_KEY_ALIAS = "AccessCodeKeyAlias";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    public static void saveAccessCode(Context context, String accessCode) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(accessCode.getBytes());
            String encryptedCode = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);

            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            preferences.edit().putString(ACCESS_CODE_KEY_ALIAS, encryptedCode).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAccessCode(Context context) {
        try {
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            String encryptedCode = preferences.getString(ACCESS_CODE_KEY_ALIAS, null);

            if (encryptedCode != null) {
                byte[] encryptedBytes = Base64.decode(encryptedCode, Base64.DEFAULT);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                return new String(decryptedBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(ACCESS_CODE_KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(
                        ACCESS_CODE_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
            }

            keyGenerator.generateKey();
        }

        return (SecretKey) keyStore.getKey(ACCESS_CODE_KEY_ALIAS, null);
    }
}
