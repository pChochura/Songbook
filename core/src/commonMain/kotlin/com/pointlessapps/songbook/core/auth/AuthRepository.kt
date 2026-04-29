package com.pointlessapps.songbook.core.auth

import com.pointlessapps.songbook.core.auth.exceptions.AccountAlreadyLinkedException
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.auth.model.Tokens
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
    suspend fun signInWithGoogle(): Boolean
    suspend fun linkWithGoogle(): Boolean
    suspend fun signInAnonymously(): Boolean
    suspend fun logout()
    suspend fun clearSession()

    suspend fun getTokens(): Tokens?
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
        val tokens = googleAuthManager.getGoogleTokens() ?: return@withContext false
        val (idToken, accessToken) = tokens
        auth.signInWith(IDToken) {
            this.provider = Google
            this.idToken = idToken
            this.accessToken = accessToken
        }
        _currentLoginStatusFlow.value = getLoginStatus()

        return@withContext true
    }

    override suspend fun linkWithGoogle() = withContext(Dispatchers.Default) {
        val tokens = googleAuthManager.getGoogleTokens() ?: return@withContext false
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

        return@withContext true
    }

    override fun isLoggedIn() = getLoginStatus().isLoggedIn

    override suspend fun signInAnonymously() = withContext(Dispatchers.Default) {
        auth.signInAnonymously()
        _currentLoginStatusFlow.value = getLoginStatus()

        return@withContext true
    }

    override suspend fun logout() = withContext(Dispatchers.Default) {
        auth.signOut()
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override suspend fun clearSession() = withContext(Dispatchers.Default) {
        auth.clearSession()
        _currentLoginStatusFlow.value = getLoginStatus()
    }

    override suspend fun getTokens() = withContext(Dispatchers.Default) {
        runCatching {
            auth.refreshCurrentSession()
            auth.currentSessionOrNull()?.let {
                Tokens(
                    accessToken = it.accessToken,
                    refreshToken = it.refreshToken,
                )
            }
        }.getOrNull()
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
