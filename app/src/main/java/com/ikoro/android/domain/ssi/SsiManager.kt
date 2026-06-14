package com.ikoro.android.domain.ssi

import com.ikoro.android.data.model.Identity
import timber.log.Timber

class SsiManager {

    data class Credential(
        val id: String,
        val type: String,
        val issuerDid: String,
        val subjectDid: String,
        val claims: Map<String, String>,
        val signature: String
    )

    private val store = mutableListOf<Credential>()

    fun issueSelfCredential(identity: Identity): Credential {
        val cred = Credential(
            id = "cred-${System.currentTimeMillis()}",
            type = "IkoroSelfCredential",
            issuerDid = identity.did,
            subjectDid = identity.did,
            claims = mapOf(
                "npub" to (identity.nostrNpub ?: ""),
                "evm" to (identity.evmAddress ?: ""),
                "created" to System.currentTimeMillis().toString()
            ),
            signature = "self-signed-placeholder"
        )
        store.add(cred)
        Timber.i("Issued self-credential %s", cred.id)
        return cred
    }

    fun credentials(): List<Credential> = store.toList()

    fun issueCommunityToken(identity: Identity, community: String, amount: Long): Result<String> {
        Timber.i("Issuing %d community tokens for %s by %s", amount, community, identity.did)
        return Result.success("token_mint_tx_placeholder")
    }
}
