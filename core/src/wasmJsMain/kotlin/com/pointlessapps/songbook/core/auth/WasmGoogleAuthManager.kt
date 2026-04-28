package com.pointlessapps.songbook.core.auth

internal class WasmGoogleAuthManager : GoogleAuthManager {
    override suspend fun getGoogleTokens() = null
}
