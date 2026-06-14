package com.ikoro.android.domain.wallet

enum class EvmChain(
    val chainName: String,
    val chainId: Long,
    val rpcUrl: String,
    val currency: String
) {
    ROOTSTOCK("Rootstock", 30, "https://public-node.rsk.co", "RBTC"),
    ETHEREUM("Ethereum", 1, "https://eth.llamarpc.com", "ETH"),
    BASE("Base", 8453, "https://base.llamarpc.com", "ETH"),
    POLYGON("Polygon", 137, "https://polygon.llamarpc.com", "MATIC"),
    ARBITRUM("Arbitrum", 42161, "https://arb1.arbitrum.io/rpc", "ETH"),
    OPTIMISM("Optimism", 10, "https://mainnet.optimism.io", "ETH"),
    BSC("BSC", 56, "https://bsc-dataseed.binance.org", "BNB")
}
