package com.example.tp1cm2021.api

import java.util.*

data class Report (
    val reportID: Int,
    val title: String,
    val description: String,
    val lat: Float,
    val lon: Float,
    val userID: Int,
    val tipo: String,
    val status: String,
    val lastModified: String,
    val lastModifiedDate: Date?,
    val username: String,
    val image : String?
)

data class LoginOutput (
    val userID: Int?,
    val username: String?,
    val error: String?
)

data class ReportNonSelectOutput (
    val error: String?,
    val result: String?
)

data class PutBody (
    val title: String,
    val description: String,
    val username: String
)