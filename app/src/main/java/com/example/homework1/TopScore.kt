package com.example.homework1

data class TopScore(
    val playerName: String,
    val score: Int,
    val distance: Int,
    val latitude: Double,
    val longitude: Double,
    val hasLocation: Boolean = false //Default location exist
)
