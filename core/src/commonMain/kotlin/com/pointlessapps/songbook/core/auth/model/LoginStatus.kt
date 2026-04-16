package com.pointlessapps.songbook.core.auth.model

import com.pointlessapps.songbook.core.utils.Keep

@Keep
enum class LoginStatus {
    ANONYMOUS, LOGGED_IN, LOGGED_OUT;

    val isLoggedIn: Boolean
        get() = when (this) {
            LOGGED_IN, ANONYMOUS -> true
            else -> false
        }
}
