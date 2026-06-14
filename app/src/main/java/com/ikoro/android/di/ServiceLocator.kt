package com.ikoro.android.di

import android.content.Context
import com.ikoro.android.data.local.IdentityStore
import com.ikoro.android.domain.identity.IdentityManager

object ServiceLocator {

    @Volatile
    private var _context: Context? = null

    private val context: Context
        get() = _context ?: throw IllegalStateException("ServiceLocator not initialized")

    val identityStore: IdentityStore by lazy { IdentityStore(context) }
    val identityManager: IdentityManager by lazy { IdentityManager(identityStore) }

    fun init(context: Context) {
        _context = context.applicationContext
    }
}
