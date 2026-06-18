package com.ikoro.android.domain.identity

import com.ikoro.android.data.local.IdentityStorage
import com.ikoro.android.data.model.DerivedAddresses
import com.ikoro.android.data.model.Identity
import com.ikoro.android.domain.wallet.WalletDerivation
import java.security.MessageDigest

class StubIdentityStorage : IdentityStorage {
    private var stored: Identity? = null
    override var hasIdentity: Boolean = false

    override fun saveIdentity(identity: Identity) {
        stored = identity
        hasIdentity = true
    }

    override fun loadIdentity(): Identity? = stored

    override fun clearIdentity() {
        stored = null
        hasIdentity = false
    }
}

class StubWalletDerivation : WalletDerivation {
    override fun deriveAddresses(mnemonic: String): DerivedAddresses {
        return DerivedAddresses(
            evmAddress = "0x" + "00".repeat(20),
            rootstockAddress = "0x" + "00".repeat(20),
            seed = MessageDigest.getInstance("SHA-256").digest(mnemonic.toByteArray())
        )
    }
}
