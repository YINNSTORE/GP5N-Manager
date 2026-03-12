package com.gptnmanager.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gptnmanager.MainViewModel
import com.gptnmanager.data.AppMessage
import com.gptnmanager.data.MessageType
import com.gptnmanager.data.ServerConfig
import com.gptnmanager.data.UserItem
import com.gptnmanager.ui.theme.ErrorColor
import com.gptnmanager.ui.theme.InfoColor
import com.gptnmanager.ui.theme.SuccessColor
import com.gptnmanager.ui.theme.WarningColor
import kotlinx.coroutines.delay

private enum class TabItem(val label: String) {
    Dashboard("Dashboard"),
    Users("Users"),
    Servers("Servers"),
    Settings("Settings")
}

@Composable
fun GPTNApp(viewModel: MainViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(TabItem.Dashboard) }
    var toastMessage by remember { mutableStateOf<AppMessage?>(null) }

    LaunchedEffect(viewModel.message?.id) {
        val msg = viewModel.message ?: return@LaunchedEffect
        toastMessage = msg
        delay(2500)
        if (toastMessage?.id == msg.id) {
            toastMessage = null
        }
        if (viewModel.message?.id == msg.id) {
            viewModel.dismissMessage()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = WindowInsets.navigationBars) {
                    listOf(
                        TabItem.Dashboard,
                        TabItem.Users,
                        TabItem.Servers,
                        TabItem.Settings
                    ).forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        TabItem.Dashboard -> Icons.Rounded.Home
                                        TabItem.Users -> Icons.Rounded.People
                                        TabItem.Servers -> Icons.Rounded.Storage
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
                    TabItem.Servers -> ServersScreen(viewModel)
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    AnimatedVisibility(
                        visible = toastMessage != null,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    ) {
                        toastMessage?.let {
                            FancyToast(
                                message = it,
                                onClose = { toastMessage = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FancyToast(
    message: AppMessage,
    onClose: () -> Unit,
) {
    val (color, icon) = when (message.type) {
        MessageType.SUCCESS -> Pair(SuccessColor, Icons.Rounded.CheckCircle)
        MessageType.ERROR -> Pair(ErrorColor, Icons.Rounded.Error)
        MessageType.INFO -> Pair(InfoColor, Icons.Rounded.Info)
        MessageType.WARNING -> Pair(WarningColor, Icons.Rounded.Warning)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = message.text,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(6.dp))

            TextButton(onClick = onClose) {
                Text("×")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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

    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isLoading,
        onRefresh = { viewModel.refreshAll(showMessage = false) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "ZIVPN Manager by YINN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Dashboard server & akun")
                    }
                    Row {
                        FilledIconButton(onClick = { viewModel.refreshAll(showMessage = false) }) {
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
                        PingStatusCard(
                            modifier = Modifier.weight(1f),
                            pingMs = viewModel.pingMs
                        )
                    }
                }
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
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
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Create User",
                            onClick = { showActions = true }
                        )
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Create Trial",
                            onClick = { showActions = true }
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Expire Check",
                            onClick = { viewModel.triggerExpire() }
                        )
                        ActionCard(
                            modifier = Modifier.weight(1f),
                            title = "Refresh",
                            onClick = { viewModel.refreshAll(showMessage = false) }
                        )
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
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

    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isLoading,
        onRefresh = { viewModel.refreshAll(showMessage = false) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Users", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    RoundedInput(
                        value = query,
                        onValueChange = { query = it },
                        label = "Cari user",
                        singleLine = true
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
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.username, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row {
                                    MiniActionButton(
                                        text = "Renew",
                                        color = WarningColor,
                                        onClick = { renewTarget = item }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    MiniActionButton(
                                        text = "Delete",
                                        color = ErrorColor,
                                        onClick = { deleteTarget = item }
                                    )
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

        PullRefreshIndicator(
            refreshing = viewModel.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServersScreen(viewModel: MainViewModel) {
    var showServerSheet by remember { mutableStateOf(false) }
    var editServer by remember { mutableStateOf<ServerConfig?>(null) }

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Servers", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                FilledIconButton(onClick = {
                    editServer = null
                    showServerSheet = true
                }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Tambah server")
                }
            }
        }

        if (viewModel.servers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Text("Belum ada server tersimpan", modifier = Modifier.padding(18.dp))
                }
            }
        } else {
            items(viewModel.servers, key = { it.id }) { server ->
                ServerListItem(
                    server = server,
                    onClick = { viewModel.setActiveServer(server.id) },
                    onEdit = {
                        editServer = server
                        showServerSheet = true
                    },
                    onDelete = { viewModel.deleteServer(server.id) }
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
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
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("ZIVPN Manager by YINN v1.0.0")
                    Text("Native Android • Kotlin + Jetpack Compose")
                }
            }
        }
    }
}

@Composable
private fun EmptyServerCard(onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
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
            PrimaryActionButton(
                text = "Tambah Server",
                color = MaterialTheme.colorScheme.primary,
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun ServerCard(server: ServerConfig, totalServer: Int, onSwitch: () -> Unit, onManage: () -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
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
                PrimaryActionButton(
                    text = "Quick Action",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onSwitch
                )
                PrimaryActionButton(
                    text = "Edit Server",
                    color = InfoColor,
                    onClick = onManage
                )
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null)
            Text(title)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PingStatusCard(modifier: Modifier, pingMs: Long) {
    val pingColor = when {
        pingMs < 0 -> ErrorColor
        pingMs in 1..389 -> SuccessColor
        pingMs in 390..599 -> WarningColor
        else -> ErrorColor
    }

    val pingText = if (pingMs < 0) "Offline" else "${pingMs} ms"

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .shadow(12.dp, CircleShape, spotColor = pingColor, ambientColor = pingColor)
                        .background(pingColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text("Status")
            }

            Text(
                if (pingMs < 0) "Offline" else "Online",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                pingText,
                color = pingColor,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = pingColor.copy(alpha = 0.95f),
                        offset = Offset.Zero,
                        blurRadius = 16f
                    )
                )
            )
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
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                PrimaryActionButton(
                    text = "Pakai",
                    color = SuccessColor,
                    onClick = onClick
                )
                PrimaryActionButton(
                    text = "Edit",
                    color = InfoColor,
                    onClick = onEdit
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .background(ErrorColor.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, ErrorColor.copy(alpha = 0.25f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = ErrorColor)
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.14f),
            contentColor = color
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MiniActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.14f),
            contentColor = color
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RoundedInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors()
    )
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
            Text(
                if (initial == null) "Tambah Server" else "Edit Server",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            RoundedInput(value = name, onValueChange = { name = it }, label = "Nama server")
            RoundedInput(value = host, onValueChange = { host = it }, label = "Domain / IP VPS")
            RoundedInput(value = apiKey, onValueChange = { apiKey = it }, label = "API Key")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryActionButton(
                    text = "Test",
                    color = WarningColor,
                    onClick = { onTest(host, apiKey) }
                )
                PrimaryActionButton(
                    text = "Save",
                    color = SuccessColor,
                    onClick = {
                        if (host.isNotBlank() && apiKey.isNotBlank()) {
                            onSave(initial?.id, name, host, apiKey)
                        }
                    }
                )
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
    var duration by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Actions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = mode == 0,
                        onClick = {
                            mode = 0
                            duration = ""
                        },
                        label = { Text("User") }
                    )
                    FilterChip(
                        selected = mode == 1,
                        onClick = {
                            mode = 1
                            duration = ""
                        },
                        label = { Text("Trial") }
                    )
                    FilterChip(
                        selected = mode == 2,
                        onClick = { mode = 2 },
                        label = { Text("Expire") }
                    )
                }
                if (mode != 2) {
                    RoundedInput(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username / Password",
                        singleLine = true
                    )
                    RoundedInput(
                        value = duration,
                        onValueChange = { duration = it },
                        label = if (mode == 0) "Days" else "Minutes",
                        singleLine = true
                    )
                } else {
                    Text("Trigger pengecekan expired manual sekarang.")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (mode) {
                        0 -> {
                            val value = duration.toIntOrNull()
                            if (username.isNotBlank() && value != null) {
                                onCreateUser(username.trim(), value)
                            }
                        }
                        1 -> {
                            val value = duration.toIntOrNull()
                            if (username.isNotBlank() && value != null) {
                                onCreateTrial(username.trim(), value)
                            }
                        }
                        else -> onExpire()
                    }
                }
            ) { Text("Jalanin") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}

@Composable
private fun RenewDialog(username: String, onDismiss: () -> Unit, onSubmit: (Int) -> Unit) {
    var days by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renew User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(username)
                RoundedInput(
                    value = days,
                    onValueChange = { days = it },
                    label = "Days",
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = days.toIntOrNull()
                    if (value != null) onSubmit(value)
                }
            ) { Text("Renew") }
        },
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