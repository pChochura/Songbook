package com.pointlessapps.songbook.core.auth

import com.pointlessapps.songbook.core.auth.exceptions.AccountAlreadyLinkedException
import com.pointlessapps.songbook.core.auth.exceptions.RemoveAccountFailedException
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.auth.model.Tokens
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.functions.functions
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface AuthRepository {
    suspend fun initialize(tokens: Tokens? = null)

    val currentLoginStatusFlow: StateFlow<LoginStatus>

    fun isLoggedIn(): Boolean
    suspend fun signInWithGoogle(): Boolean
    suspend fun linkWithGoogle(): Boolean
    suspend fun signInAnonymously(): Boolean
    suspend fun logout()
    suspend fun removeAccount()

    suspend fun getTokens(): Tokens?
}

internal class AuthRepositoryImpl(
    client: SupabaseClient,
    private val googleAuthManager: GoogleAuthManager,
) : AuthRepository {

    private val functions = client.functions
    private val auth = client.auth

    private val _currentLoginStatusFlow = MutableStateFlow(LoginStatus.LOGGED_OUT)
    override val currentLoginStatusFlow: StateFlow<LoginStatus>
        get() = _currentLoginStatusFlow.asStateFlow()

    override suspend fun initialize(tokens: Tokens?) = withContext(Dispatchers.Default) {
        auth.awaitInitialization()

        if (tokens != null) {
            runCatching {
                auth.importAuthToken(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                )
                auth.retrieveUserForCurrentSession(updateSession = true)
            }
        }

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

    override suspend fun removeAccount() {
        withContext(Dispatchers.Default) {
            val statusCode = functions.invoke(
                function = REMOVE_ACCOUNT_EDGE_FUNCTION,
                headers = Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                },
            ).status

            if (!statusCode.isSuccess()) {
                throw RemoveAccountFailedException()
            }

            logout()
        }
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

    private companion object {
        const val REMOVE_ACCOUNT_EDGE_FUNCTION = "delete-current-user"
    }
}
