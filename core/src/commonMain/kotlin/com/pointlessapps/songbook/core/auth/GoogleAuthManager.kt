package com.pointlessapps.songbook.core.auth

interface GoogleAuthManager {
    suspend fun getGoogleTokens(): GoogleTokens?
}

data class GoogleTokens(
    val idToken: String,
    val accessToken: String?,
)
