package com.ikoro.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ikoro.android.data.model.Identity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class IdentityStore(context: Context) : IdentityStorage {

    private val prefs: SharedPreferences

    init {
        prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "EncryptedSharedPreferences failed; falling back to plain SharedPreferences (device key store issue)")
            context.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }

    override var hasIdentity: Boolean
        get() = prefs.getBoolean(KEY_HAS_IDENTITY, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_IDENTITY, value).apply()

    private var identityJson: String?
        get() = prefs.getString(KEY_IDENTITY, null)
        set(value) = prefs.edit().putString(KEY_IDENTITY, value).apply()

    override fun saveIdentity(identity: Identity) {
        identityJson = Json.encodeToString(identity)
        hasIdentity = true
    }

    override fun loadIdentity(): Identity? {
        val json = identityJson ?: return null
        return try {
            Json.decodeFromString<Identity>(json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode identity")
            null
        }
    }

    override fun clearIdentity() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "ikoro_identity_secure"
        private const val KEY_HAS_IDENTITY = "has_identity"
        private const val KEY_IDENTITY = "identity_json"
    }
}
