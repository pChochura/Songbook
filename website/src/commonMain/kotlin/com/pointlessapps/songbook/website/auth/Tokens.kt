package com.pointlessapps.songbook.website.auth

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)
