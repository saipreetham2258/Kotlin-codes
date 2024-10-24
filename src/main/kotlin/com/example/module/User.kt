package com.example.module
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class User(
    var name : String,
    var age : String)
