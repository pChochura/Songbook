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
    private val client: SupabaseClient,
) : AuthRepository {

    override suspend fun initialize() {
        client.auth.awaitInitialization()
    }

    override fun isSignedIn(): Boolean = client.auth.currentUserOrNull() != null

    override suspend fun signInWithGoogle(googleIdToken: String) {
        client.auth.signInWith(IDToken) {
            idToken = googleIdToken
            provider = Google
        }
    }

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signInAnonymously() {
        client.auth.signInAnonymously()
    }
}
