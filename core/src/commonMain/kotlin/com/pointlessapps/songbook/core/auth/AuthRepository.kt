package com.pointlessapps.songbook.core.auth

import com.pointlessapps.songbook.core.auth.exceptions.AccountAlreadyLinkedException
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun initialize()

    val currentLoginStatusFlow: StateFlow<LoginStatus>

    fun isLoggedIn(): Boolean
    suspend fun signInWithGoogle()
    suspend fun linkWithGoogle()
    suspend fun signInAnonymously()
    suspend fun logout()
}

internal class AuthRepositoryImpl(
    client: SupabaseClient,
    private val googleAuthManager: GoogleAuthManager,
) : AuthRepository {

    private val auth = client.auth

    private val _currentLoginStatusFlow = MutableStateFlow(LoginStatus.LOGGED_OUT)
    override val currentLoginStatusFlow: StateFlow<LoginStatus>
        get() = _currentLoginStatusFlow.asStateFlow()

    override suspend fun initialize() = withContext(Dispatchers.Default) {
        auth.awaitInitialization()
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override suspend fun signInWithGoogle() = withContext(Dispatchers.Default) {
        val tokens = googleAuthManager.getGoogleTokens() ?: return@withContext
        val (idToken, accessToken) = tokens
        auth.signInWith(IDToken) {
            this.provider = Google
            this.idToken = idToken
            this.accessToken = accessToken
        }
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override suspend fun linkWithGoogle() = withContext(Dispatchers.Default) {
        val tokens = googleAuthManager.getGoogleTokens() ?: return@withContext
        val (idToken, accessToken) = tokens
        try {
            auth.linkIdentityWithIdToken(Google, idToken) {
                this.accessToken = accessToken
            }
        } catch (e: AuthRestException) {
            if (e.errorCode == AuthErrorCode.IdentityAlreadyExists) {
                throw AccountAlreadyLinkedException()
            } else {
                throw e
            }
        }
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override fun isLoggedIn() = getLoginStatus().isLoggedIn

    override suspend fun signInAnonymously() = withContext(Dispatchers.Default) {
        auth.signInAnonymously()
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override suspend fun logout() = withContext(Dispatchers.Default) {
        auth.signOut()
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    private fun getLoginStatus(): LoginStatus {
        val currentUser = auth.currentUserOrNull()

        return when {
            currentUser == null -> LoginStatus.LOGGED_OUT
            currentUser.isAnonymous == true -> LoginStatus.ANONYMOUS
            else -> LoginStatus.LOGGED_IN
        }
    }
}
