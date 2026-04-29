package com.pointlessapps.songbook.website.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
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
    val currentLoginStatusFlow: StateFlow<LoginStatus>

    suspend fun initialize(url: String)
    suspend fun removeAccount()
}

internal class AuthRepositoryImpl(
    client: SupabaseClient,
) : AuthRepository {

    private val functions = client.functions
    private val auth = client.auth

    private val _currentLoginStatusFlow = MutableStateFlow(LoginStatus.LOGGED_OUT)
    override val currentLoginStatusFlow: StateFlow<LoginStatus>
        get() = _currentLoginStatusFlow.asStateFlow()

    override suspend fun initialize(url: String) = withContext(Dispatchers.Default) {
        auth.awaitInitialization()

        url.extractTokens()?.let { (accessToken, refreshToken) ->
            runCatching {
                auth.importAuthToken(accessToken, refreshToken)
                auth.retrieveUserForCurrentSession(updateSession = true)
            }
        }

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

    private suspend fun logout() = withContext(Dispatchers.Default) {
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

    private fun String.extractTokens() = Regex(
        "$ACCESS_TOKEN=([^&]+)&$REFRESH_TOKEN=([^&]+)",
    ).find(this)?.groups?.let {
        it[1]?.value.orEmpty() to it[2]?.value.orEmpty()
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val REMOVE_ACCOUNT_EDGE_FUNCTION = "delete-current-user"
    }
}
