package com.ikoro.android.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.ikoro.android.data.model.Identity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class IdentityStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasIdentity: Boolean
        get() = prefs.getBoolean(KEY_HAS_IDENTITY, false)
        set(value) = prefs.edit { putBoolean(KEY_HAS_IDENTITY, value) }

    var identityJson: String?
        get() = prefs.getString(KEY_IDENTITY, null)
        set(value) = prefs.edit { putString(KEY_IDENTITY, value) }

    fun saveIdentity(identity: Identity) {
        identityJson = Json.encodeToString(identity)
        hasIdentity = true
    }

    fun loadIdentity(): Identity? {
        val json = identityJson ?: return null
        return try {
            Json.decodeFromString<Identity>(json)
        } catch (e: Exception) {
            null
        }
    }

    fun clearIdentity() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "ikoro_identity_secure"
        private const val KEY_HAS_IDENTITY = "has_identity"
        private const val KEY_IDENTITY = "identity_json"

        fun sha256(input: String): String {
            return MessageDigest.getInstance("SHA-256")
                .digest(input.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }
    }
}
