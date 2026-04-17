package com.pointlessapps.songbook.core.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class IosGoogleAuthManager : GoogleAuthManager {
    override suspend fun getGoogleTokens(): GoogleTokens? = withContext(Dispatchers.Main) {
        // On iOS, Google Sign-In requires the GoogleSignIn SDK which is not yet integrated.
        // Once integrated, use GIDSignIn.sharedInstance.signIn(withPresenting: ...)
        // to retrieve the idToken and accessToken.
        null
    }
}
