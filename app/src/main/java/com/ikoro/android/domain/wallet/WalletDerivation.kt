package com.ikoro.android.domain.wallet

import com.ikoro.android.data.model.DerivedAddresses

interface WalletDerivation {
    fun deriveAddresses(mnemonic: String): DerivedAddresses
}
