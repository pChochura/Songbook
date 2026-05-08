package com.pointlessapps.songbook.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidGoogleAuthManager(
    private val context: Context,
    private val webClientId: String,
) : GoogleAuthManager {

    override suspend fun getGoogleTokens(): GoogleTokens? = withContext(Dispatchers.IO) {
        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

            GoogleTokens(
                idToken = googleIdTokenCredential.idToken,
                accessToken = null,
            )
        } catch (e: GetCredentialException) {
            e.printStackTrace()
            null
        }
    }
}
