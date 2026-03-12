package com.gptnmanager.data

enum class MessageType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

data class ServerConfig(
    val id: String,
    val name: String,
    val host: String,
    val apiKey: String,
    val isActive: Boolean = false,
) {
    val baseUrl: String
        get() {
            val clean = host.trim().removeSuffix("/")
            val withProtocol = if (clean.startsWith("http://") || clean.startsWith("https://")) {
                clean
            } else {
                "http://$clean"
            }
            return if (withProtocol.endsWith(":8080")) withProtocol else "$withProtocol:8080"
        }
}

data class UserItem(
    val username: String,
    val expireAt: String? = null,
    val status: String? = null,
    val daysLeft: Int? = null,
)

data class AppMessage(
    val text: String,
    val isError: Boolean = false,
    val type: MessageType = if (isError) MessageType.ERROR else MessageType.SUCCESS,
    val id: Long = System.currentTimeMillis(),
)