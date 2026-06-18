package com.ikoro.android.domain.wallet

import com.ikoro.android.data.model.DerivedAddresses
import wallet.core.jni.CoinType
import wallet.core.jni.HDWallet

class TrustWalletDerivation : WalletDerivation {
    override fun deriveAddresses(mnemonic: String): DerivedAddresses {
        val wallet = HDWallet(mnemonic, "")
        return DerivedAddresses(
            evmAddress = wallet.getAddressForCoin(CoinType.ETHEREUM),
            rootstockAddress = wallet.getAddressForCoin(CoinType.ROOTSTOCK),
            seed = wallet.seed()
        )
    }
}
