package com.gptnmanager.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ServerStorage(context: Context) {
    private val prefs = context.getSharedPreferences("gptn_manager_prefs", Context.MODE_PRIVATE)

    fun loadServers(): List<ServerConfig> {
        val raw = prefs.getString(KEY_SERVERS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        ServerConfig(
                            id = obj.optString("id"),
                            name = obj.optString("name"),
                            host = obj.optString("host"),
                            apiKey = obj.optString("apiKey"),
                            isActive = obj.optBoolean("isActive", false),
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveServers(servers: List<ServerConfig>) {
        val arr = JSONArray()
        servers.forEach { s ->
            arr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("host", s.host)
                    put("apiKey", s.apiKey)
                    put("isActive", s.isActive)
                }
            )
        }
        prefs.edit().putString(KEY_SERVERS, arr.toString()).apply()
    }

    fun loadDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)

    fun saveDarkMode(value: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    }

    companion object {
        private const val KEY_SERVERS = "servers"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}