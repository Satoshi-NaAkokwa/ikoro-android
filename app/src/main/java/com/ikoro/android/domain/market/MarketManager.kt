package com.ikoro.android.domain.market

import timber.log.Timber

class MarketManager {

    data class MarketListing(
        val id: String,
        val title: String,
        val price: String,
        val seller: String,
        val assetType: String
    )

    fun listings(): List<MarketListing> {
        return emptyList()
    }

    fun createListing(title: String, price: String, assetType: String): Result<MarketListing> {
        Timber.i("Creating %s listing: %s at %s", assetType, title, price)
        return Result.success(MarketListing("1", title, price, "me", assetType))
    }

    fun buy(listingId: String): Result<String> {
        Timber.i("Buying listing %s", listingId)
        return Result.success("escrow_created")
    }
}
