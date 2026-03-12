package com.gptnmanager.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ApiService {
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun getInfo(server: ServerConfig): Map<String, String> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("${server.baseUrl}/api/info")
            .header("X-API-Key", server.apiKey)
            .get()
            .build()

        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) error(body.ifBlank { "HTTP ${res.code}" })
            parseInfo(body)
        }
    }

    suspend fun getUsers(server: ServerConfig): List<UserItem> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("${server.baseUrl}/api/users")
            .header("X-API-Key", server.apiKey)
            .get()
            .build()

        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) error(body.ifBlank { "HTTP ${res.code}" })
            parseUsers(body)
        }
    }

    suspend fun createUser(server: ServerConfig, password: String, days: Int? = null, minutes: Int? = null): String =
        post(server, "/api/user/create", JSONObject().apply {
            put("password", password)
            if (days != null) put("days", days)
            if (minutes != null) put("minutes", minutes)
        })

    suspend fun renewUser(server: ServerConfig, password: String, days: Int): String =
        post(server, "/api/user/renew", JSONObject().apply {
            put("password", password)
            put("days", days)
        })

    suspend fun deleteUser(server: ServerConfig, password: String): String =
        post(server, "/api/user/delete", JSONObject().apply {
            put("password", password)
        })

    suspend fun triggerExpire(server: ServerConfig): String = post(server, "/api/cron/expire", JSONObject())

    private suspend fun post(server: ServerConfig, path: String, payload: JSONObject): String = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("${server.baseUrl}$path")
            .header("X-API-Key", server.apiKey)
            .post(payload.toString().toRequestBody(jsonType))
            .build()

        client.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) error(body.ifBlank { "HTTP ${res.code}" })
            parseMessage(body)
        }
    }

    private fun parseInfo(body: String): Map<String, String> {
        return try {
            val obj = JSONObject(body)
            val source: JSONObject = when {
                obj.has("data") && obj.opt("data") is JSONObject -> obj.getJSONObject("data")
                else -> obj
            }
            source.keys().asSequence().associateWith { key ->
                source.opt(key)?.toString().orEmpty()
            }
        } catch (_: Throwable) {
            mapOf("raw" to body)
        }
    }

    private fun parseUsers(body: String): List<UserItem> {
        return try {
            when {
                body.trim().startsWith("[") -> parseUserArray(JSONArray(body))
                else -> {
                    val obj = JSONObject(body)
                    when {
                        obj.opt("data") is JSONArray -> parseUserArray(obj.getJSONArray("data"))
                        obj.opt("users") is JSONArray -> parseUserArray(obj.getJSONArray("users"))
                        obj.opt("result") is JSONArray -> parseUserArray(obj.getJSONArray("result"))
                        else -> emptyList()
                    }
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun parseUserArray(arr: JSONArray): List<UserItem> = buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            add(
                UserItem(
                    username = obj.optString("username")
                        .ifBlank { obj.optString("user") }
                        .ifBlank { obj.optString("password") }
                        .ifBlank { obj.optString("name") },
                    expireAt = obj.optString("expire_at").ifBlank {
                        obj.optString("expired_at").ifBlank { obj.optString("expires_at") }
                    }.ifBlank { null },
                    status = obj.optString("status").ifBlank { null },
                    daysLeft = obj.optInt("days_left").takeIf { it != 0 || obj.has("days_left") },
                )
            )
        }
    }

    private fun parseMessage(body: String): String {
        return try {
            val obj = JSONObject(body)
            obj.optString("message").ifBlank {
                if (obj.optBoolean("status", false)) "Success" else body
            }
        } catch (_: Throwable) {
            body.ifBlank { "Success" }
        }
    }
}