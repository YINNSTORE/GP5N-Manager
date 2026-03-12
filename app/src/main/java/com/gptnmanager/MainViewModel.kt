package com.gptnmanager

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gptnmanager.data.ApiService
import com.gptnmanager.data.AppMessage
import com.gptnmanager.data.MessageType
import com.gptnmanager.data.ServerConfig
import com.gptnmanager.data.ServerStorage
import com.gptnmanager.data.UserItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = ServerStorage(application)
    private val api = ApiService()
    private var pingJob: Job? = null

    val servers = mutableStateListOf<ServerConfig>()

    var users by mutableStateOf<List<UserItem>>(emptyList())
        private set

    var systemInfo by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isDarkMode by mutableStateOf(true)
        private set

    var message by mutableStateOf<AppMessage?>(null)
        private set

    var pingMs by mutableLongStateOf(-1L)
        private set

    val activeServer: ServerConfig?
        get() = servers.firstOrNull { it.isActive } ?: servers.firstOrNull()

    init {
        isDarkMode = storage.loadDarkMode()
        servers.addAll(storage.loadServers())
        if (servers.isNotEmpty()) {
            refreshAll(showMessage = false)
            startPingLoop()
        }
    }

    fun dismissMessage() {
        message = null
    }

    fun updateDarkMode(value: Boolean) {
        isDarkMode = value
        storage.saveDarkMode(value)
    }

    fun addOrUpdateServer(existingId: String?, name: String, host: String, apiKey: String) {
        val safeName = name.ifBlank { "Server ${Random.nextInt(100, 999)}" }
        val list = servers.toMutableList()

        if (existingId == null) {
            val item = ServerConfig(
                id = System.currentTimeMillis().toString(),
                name = safeName,
                host = host.trim(),
                apiKey = apiKey.trim(),
                isActive = list.isEmpty(),
            )
            list.add(item)
        } else {
            val index = list.indexOfFirst { it.id == existingId }
            if (index >= 0) {
                val old = list[index]
                list[index] = old.copy(
                    name = safeName,
                    host = host.trim(),
                    apiKey = apiKey.trim()
                )
            }
        }

        replaceServers(list)
        message = AppMessage("Server berhasil disimpan", type = MessageType.SUCCESS)
        refreshAll(showMessage = false)
        startPingLoop()
    }

    fun deleteServer(id: String) {
        val list = servers.filterNot { it.id == id }.toMutableList()
        if (list.isNotEmpty() && list.none { it.isActive }) {
            list[0] = list[0].copy(isActive = true)
        }

        replaceServers(list)

        if (list.isNotEmpty()) {
            refreshAll(showMessage = false)
            startPingLoop()
        } else {
            users = emptyList()
            systemInfo = emptyMap()
            pingMs = -1L
            stopPingLoop()
        }

        message = AppMessage("Server dihapus", type = MessageType.SUCCESS)
    }

    fun setActiveServer(id: String) {
        replaceServers(servers.map { it.copy(isActive = it.id == id) })
        message = AppMessage("Server aktif diganti", type = MessageType.INFO)
        refreshAll(showMessage = false)
        startPingLoop()
    }

    fun testServer(host: String, apiKey: String) {
        val temp = ServerConfig(
            id = "tmp",
            name = "Temp",
            host = host,
            apiKey = apiKey,
            isActive = true
        )

        viewModelScope.launch {
            isLoading = true
            try {
                val started = System.currentTimeMillis()
                api.getInfo(temp)
                pingMs = System.currentTimeMillis() - started
                message = AppMessage("Koneksi sukses", type = MessageType.SUCCESS)
            } catch (t: Throwable) {
                pingMs = -1L
                message = AppMessage("Koneksi gagal: ${t.message}", true, MessageType.ERROR)
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshAll(showMessage: Boolean = true) {
        val server = activeServer ?: return

        viewModelScope.launch {
            isLoading = true
            try {
                val started = System.currentTimeMillis()
                systemInfo = api.getInfo(server)
                pingMs = System.currentTimeMillis() - started
                users = api.getUsers(server)

                if (showMessage) {
                    message = AppMessage("Data berhasil diperbarui", type = MessageType.INFO)
                }
            } catch (t: Throwable) {
                pingMs = -1L
                message = AppMessage("Gagal ambil data: ${t.message}", true, MessageType.ERROR)
            } finally {
                isLoading = false
            }
        }
    }

    fun createUser(username: String, days: Int) {
        val server = activeServer ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                message = AppMessage(
                    api.createUser(server, username, days = days),
                    type = MessageType.SUCCESS
                )
                refreshAll(showMessage = false)
            } catch (t: Throwable) {
                isLoading = false
                message = AppMessage("Create user gagal: ${t.message}", true, MessageType.ERROR)
            }
        }
    }

    fun createTrial(username: String, minutes: Int) {
        val server = activeServer ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                message = AppMessage(
                    api.createUser(server, username, minutes = minutes),
                    type = MessageType.SUCCESS
                )
                refreshAll(showMessage = false)
            } catch (t: Throwable) {
                isLoading = false
                message = AppMessage("Create trial gagal: ${t.message}", true, MessageType.ERROR)
            }
        }
    }

    fun renewUser(username: String, days: Int) {
        val server = activeServer ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                message = AppMessage(
                    api.renewUser(server, username, days),
                    type = MessageType.SUCCESS
                )
                refreshAll(showMessage = false)
            } catch (t: Throwable) {
                isLoading = false
                message = AppMessage("Renew gagal: ${t.message}", true, MessageType.ERROR)
            }
        }
    }

    fun deleteUser(username: String) {
        val server = activeServer ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                message = AppMessage(
                    api.deleteUser(server, username),
                    type = MessageType.SUCCESS
                )
                refreshAll(showMessage = false)
            } catch (t: Throwable) {
                isLoading = false
                message = AppMessage("Delete gagal: ${t.message}", true, MessageType.ERROR)
            }
        }
    }

    fun triggerExpire() {
        val server = activeServer ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                message = AppMessage(
                    api.triggerExpire(server),
                    type = MessageType.INFO
                )
                refreshAll(showMessage = false)
            } catch (t: Throwable) {
                isLoading = false
                message = AppMessage("Trigger expire gagal: ${t.message}", true, MessageType.ERROR)
            }
        }
    }

    private fun startPingLoop() {
        stopPingLoop()
        if (activeServer == null) return

        pingJob = viewModelScope.launch {
            while (true) {
                val server = activeServer
                if (server != null) {
                    try {
                        val started = System.currentTimeMillis()
                        api.getInfo(server)
                        pingMs = System.currentTimeMillis() - started
                    } catch (_: Throwable) {
                        pingMs = -1L
                    }
                } else {
                    pingMs = -1L
                }
                delay(5000)
            }
        }
    }

    private fun stopPingLoop() {
        pingJob?.cancel()
        pingJob = null
    }

    private fun replaceServers(list: List<ServerConfig>) {
        servers.clear()
        servers.addAll(list)
        storage.saveServers(list)
    }

    override fun onCleared() {
        super.onCleared()
        stopPingLoop()
    }
}