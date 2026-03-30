package com.pointlessapps.songbook.core.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken

interface AuthRepository {
    suspend fun initialize()

    fun isSignedIn(): Boolean
    suspend fun signInWithGoogle(googleIdToken: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signInAnonymously()
}

internal class AuthRepositoryImpl(
    client: SupabaseClient,
) : AuthRepository {

    private val auth = client.auth

    override suspend fun initialize() {
        auth.awaitInitialization()
    }

    override fun isSignedIn(): Boolean = auth.currentUserOrNull() != null

    override suspend fun signInWithGoogle(googleIdToken: String) {
        auth.signInWith(IDToken) {
            idToken = googleIdToken
            provider = Google
        }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInAnonymously() {
        auth.signInAnonymously()
    }
}
