package com.gptnmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gptnmanager.MainViewModel
import com.gptnmanager.data.ServerConfig
import com.gptnmanager.data.UserItem

private enum class TabItem(val label: String) {
    Dashboard("Dashboard"), Users("Users"), Settings("Settings")
}

@Composable
fun GPTNApp(viewModel: MainViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(TabItem.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.message) {
        val msg = viewModel.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg.text)
        viewModel.dismissMessage()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(windowInsets = WindowInsets.navigationBars) {
                    listOf(TabItem.Dashboard, TabItem.Users, TabItem.Settings).forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        TabItem.Dashboard -> Icons.Rounded.Home
                                        TabItem.Users -> Icons.Rounded.People
                                        TabItem.Settings -> Icons.Rounded.Settings
                                    },
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentTab) {
                    TabItem.Dashboard -> DashboardScreen(viewModel)
                    TabItem.Users -> UsersScreen(viewModel)
                    TabItem.Settings -> SettingsScreen(viewModel)
                }
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(viewModel: MainViewModel) {
    var showServerSheet by remember { mutableStateOf(false) }
    var editServer by remember { mutableStateOf<ServerConfig?>(null) }
    var showActions by remember { mutableStateOf(false) }

    if (showServerSheet) {
        ServerSheet(
            initial = editServer,
            onDismiss = {
                showServerSheet = false
                editServer = null
            },
            onTest = { host, apiKey -> viewModel.testServer(host, apiKey) },
            onSave = { id, name, host, apiKey ->
                viewModel.addOrUpdateServer(id, name, host, apiKey)
                showServerSheet = false
                editServer = null
            }
        )
    }

    if (showActions) {
        QuickActionsDialog(
            onDismiss = { showActions = false },
            onCreateUser = { username, days ->
                viewModel.createUser(username, days)
                showActions = false
            },
            onCreateTrial = { username, minutes ->
                viewModel.createTrial(username, minutes)
                showActions = false
            },
            onExpire = {
                viewModel.triggerExpire()
                showActions = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("GPTN Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Dashboard server & akun")
                }
                Row {
                    FilledIconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { showServerSheet = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Tambah server")
                    }
                }
            }
        }

        val activeServer = viewModel.activeServer
        if (activeServer == null) {
            item {
                EmptyServerCard(onAdd = { showServerSheet = true })
            }
        } else {
            item {
                ServerCard(
                    server = activeServer,
                    totalServer = viewModel.servers.size,
                    onSwitch = { showActions = true },
                    onManage = { editServer = activeServer; showServerSheet = true },
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Users",
                        value = viewModel.users.size.toString(),
                        icon = Icons.Rounded.People,
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Status",
                        value = "Online",
                        icon = Icons.Rounded.Cloud,
                    )
                }
            }
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("System Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (viewModel.systemInfo.isEmpty()) {
                            Text("Belum ada data")
                        } else {
                            viewModel.systemInfo.entries.take(8).forEach { entry ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.key)
                                    Spacer(Modifier.width(8.dp))
                                    Text(entry.value, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(modifier = Modifier.weight(1f), title = "Create User", onClick = { showActions = true })
                    ActionCard(modifier = Modifier.weight(1f), title = "Create Trial", onClick = { showActions = true })
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(modifier = Modifier.weight(1f), title = "Expire Check", onClick = { viewModel.triggerExpire() })
                    ActionCard(modifier = Modifier.weight(1f), title = "Refresh", onClick = { viewModel.refreshAll() })
                }
            }
            item {
                Text("Saved Servers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(viewModel.servers, key = { it.id }) { server ->
                ServerListItem(
                    server = server,
                    onClick = { viewModel.setActiveServer(server.id) },
                    onEdit = { editServer = server; showServerSheet = true },
                    onDelete = { viewModel.deleteServer(server.id) },
                )
            }
        }
    }
}

@Composable
private fun UsersScreen(viewModel: MainViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    var renewTarget by remember { mutableStateOf<UserItem?>(null) }
    var deleteTarget by remember { mutableStateOf<UserItem?>(null) }
    val filtered = remember(viewModel.users, query) {
        if (query.isBlank()) viewModel.users else viewModel.users.filter {
            it.username.contains(query, ignoreCase = true)
        }
    }

    if (renewTarget != null) {
        RenewDialog(
            username = renewTarget!!.username,
            onDismiss = { renewTarget = null },
            onSubmit = {
                viewModel.renewUser(renewTarget!!.username, it)
                renewTarget = null
            }
        )
    }

    if (deleteTarget != null) {
        ConfirmDeleteDialog(
            username = deleteTarget!!.username,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                viewModel.deleteUser(deleteTarget!!.username)
                deleteTarget = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Users", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cari user") },
                    singleLine = true,
                )
                Text("Total: ${filtered.size} user")
            }
        }

        if (viewModel.activeServer == null) {
            item { Text("Belum ada server aktif") }
        } else if (filtered.isEmpty()) {
            item {
                Card(shape = RoundedCornerShape(22.dp)) {
                    Text("Belum ada data user", modifier = Modifier.padding(18.dp))
                }
            }
        } else {
            items(filtered, key = { it.username }) { item ->
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Row {
                                AssistChip(onClick = { renewTarget = item }, label = { Text("Renew") })
                                Spacer(Modifier.width(8.dp))
                                AssistChip(onClick = { deleteTarget = item }, label = { Text("Delete") })
                            }
                        }
                        item.status?.let { Text("Status: $it") }
                        item.expireAt?.let { Text("Expire: $it") }
                        item.daysLeft?.let { Text("Sisa hari: $it") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = viewModel.isDarkMode,
                            onClick = { viewModel.updateDarkMode(true) },
                            label = { Text("Dark") },
                            leadingIcon = { Icon(Icons.Rounded.DarkMode, null) },
                        )
                        FilterChip(
                            selected = !viewModel.isDarkMode,
                            onClick = { viewModel.updateDarkMode(false) },
                            label = { Text("Light") },
                            leadingIcon = { Icon(Icons.Rounded.LightMode, null) },
                        )
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("GPTN Manager v1.0.0")
                    Text("Native Android • Kotlin + Jetpack Compose")
                }
            }
        }
    }
}

@Composable
private fun EmptyServerCard(onAdd: () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.Dns, null, modifier = Modifier.size(52.dp))
            Text("Belum ada server", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Tambah domain/IP VPS dan API Key dulu biar dashboard jalan.")
            OutlinedButton(onClick = onAdd) { Text("Tambah Server") }
        }
    }
}

@Composable
private fun ServerCard(server: ServerConfig, totalServer: Int, onSwitch: () -> Unit, onManage: () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Dns, null)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(server.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(server.host)
                }
            }
            HorizontalDivider()
            Text("Base URL: ${server.baseUrl}")
            Text("Total saved server: $totalServer")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onSwitch) { Text("Quick Action") }
                OutlinedButton(onClick = onManage) { Text("Edit Server") }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null)
            Text(title)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionCard(modifier: Modifier, title: String, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ServerListItem(server: ServerConfig, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(server.name, fontWeight = FontWeight.Bold)
                    Text(server.host, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (server.isActive) {
                    AssistChip(onClick = {}, label = { Text("Active") })
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClick) { Text("Pakai") }
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, contentDescription = "Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerSheet(
    initial: ServerConfig?,
    onDismiss: () -> Unit,
    onTest: (String, String) -> Unit,
    onSave: (String?, String, String, String) -> Unit,
) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var host by rememberSaveable(initial?.id) { mutableStateOf(initial?.host.orEmpty()) }
    var apiKey by rememberSaveable(initial?.id) { mutableStateOf(initial?.apiKey.orEmpty()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (initial == null) "Tambah Server" else "Edit Server", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nama server") })
            OutlinedTextField(value = host, onValueChange = { host = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Domain / IP VPS") })
            OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, modifier = Modifier.fillMaxWidth(), label = { Text("API Key") })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { onTest(host, apiKey) }) { Text("Test") }
                OutlinedButton(modifier = Modifier.weight(1f), onClick = {
                    if (host.isNotBlank() && apiKey.isNotBlank()) onSave(initial?.id, name, host, apiKey)
                }) { Text("Save") }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickActionsDialog(
    onDismiss: () -> Unit,
    onCreateUser: (String, Int) -> Unit,
    onCreateTrial: (String, Int) -> Unit,
    onExpire: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var duration by rememberSaveable { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Actions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == 0, onClick = { mode = 0; duration = "30" }, label = { Text("User") })
                    FilterChip(selected = mode == 1, onClick = { mode = 1; duration = "60" }, label = { Text("Trial") })
                    FilterChip(selected = mode == 2, onClick = { mode = 2 }, label = { Text("Expire") })
                }
                if (mode != 2) {
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username / Password") }, singleLine = true)
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text(if (mode == 0) "Days" else "Minutes") }, singleLine = true)
                } else {
                    Text("Trigger pengecekan expired manual sekarang.")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (mode) {
                    0 -> onCreateUser(username.trim(), duration.toIntOrNull() ?: 30)
                    1 -> onCreateTrial(username.trim(), duration.toIntOrNull() ?: 60)
                    else -> onExpire()
                }
            }) { Text("Jalanin") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}

@Composable
private fun RenewDialog(username: String, onDismiss: () -> Unit, onSubmit: (Int) -> Unit) {
    var days by rememberSaveable { mutableStateOf("30") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(username)
                OutlinedTextField(value = days, onValueChange = { days = it }, label = { Text("Days") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(days.toIntOrNull() ?: 30) }) { Text("Renew") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun ConfirmDeleteDialog(username: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User") },
        text = { Text("Yakin mau hapus user \"$username\"?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}