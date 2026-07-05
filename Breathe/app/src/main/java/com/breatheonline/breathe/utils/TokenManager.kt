package com.breatheonline.breathe.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure JWT token storage backed by EncryptedSharedPreferences (AES-256).
 *
 * Injected as a singleton by Hilt — no manual instantiation needed.
 * All reads/writes are synchronous; call from a coroutine if needed on the main thread.
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy { buildEncryptedPrefs() }

    // ── Public API ────────────────────────────────────────────────────────────

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun buildEncryptedPrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (primary: Exception) {
            // Key mismatch happens after a backup/restore to a new device.
            // The Keystore master key is device-specific and cannot be transferred,
            // so the backed-up encrypted file is unreadable. Wipe it and start fresh
            // — the user will just need to log in again, which is the safe outcome.
            Log.w(TAG, "EncryptedSharedPreferences init failed; wiping and recreating (backup-restore scenario)", primary)
            context.deleteSharedPreferences(PREFS_FILE)
            try {
                createEncryptedPrefs()
            } catch (secondary: Exception) {
                // Extreme fallback: unencrypted prefs. Token will be null → forced login.
                Log.e(TAG, "EncryptedSharedPreferences recreation failed; falling back to plain prefs", secondary)
                context.getSharedPreferences("${PREFS_FILE}_fallback", Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    companion object {
        private const val PREFS_FILE = "secure_auth"
        private const val KEY_TOKEN  = "jwt_token"
        private const val TAG        = "TokenManager"
    }
}
