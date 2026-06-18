package com.ikoro.android.data.local

import com.ikoro.android.data.model.Identity

interface IdentityStorage {
    var hasIdentity: Boolean
    fun saveIdentity(identity: Identity)
    fun loadIdentity(): Identity?
    fun clearIdentity()
}
