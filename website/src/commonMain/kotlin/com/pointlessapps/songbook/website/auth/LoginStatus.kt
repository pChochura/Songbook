package com.pointlessapps.songbook.website.auth

enum class LoginStatus {
    ANONYMOUS, LOGGED_IN, LOGGED_OUT;

    val isLoggedIn: Boolean
        get() = when (this) {
            LOGGED_IN, ANONYMOUS -> true
            else -> false
        }
}
