package com.google.firebase.example.makeitso.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val signInMethod: String = "",
    val providers: List<String> = emptyList(),
    val createdAt: String = "",
    val lastSignInAt: String = ""
)
