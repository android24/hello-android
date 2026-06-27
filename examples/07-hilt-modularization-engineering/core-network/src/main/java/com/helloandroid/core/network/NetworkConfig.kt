package com.helloandroid.core.network

data class NetworkConfig(
    val baseUrl: String,
    val environmentName: String,
    val verboseLogEnabled: Boolean
) {
    val badge: String
        get() = if (verboseLogEnabled) {
            "$environmentName / verbose"
        } else {
            environmentName
        }
}
