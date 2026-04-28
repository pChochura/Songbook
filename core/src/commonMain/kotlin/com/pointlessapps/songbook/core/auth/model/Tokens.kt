package com.pointlessapps.songbook.core.auth.model

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)
