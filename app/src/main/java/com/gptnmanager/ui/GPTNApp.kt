package com.gptnmanager.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
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
import com.gptnmanager.data.*
import com.gptnmanager.ui.theme.*
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

    LaunchedEffect(viewModel.message) {
        val msg = viewModel.message ?: return@LaunchedEffect
        toastMessage = msg
        delay(2500)
        toastMessage = null
        if (viewModel.message == msg) {
            viewModel.dismissMessage()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(windowInsets = WindowInsets.navigationBars) {
                    TabItem.values().forEach { tab ->
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
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
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
                        contentAlignment = Alignment.Center
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
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut()
                    ) {
                        toastMessage?.let {
                            FancyToast(it) { toastMessage = null }
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
    onClose: () -> Unit
) {

    val (color, icon) = when (message.type) {
        MessageType.SUCCESS -> SuccessColor to Icons.Rounded.CheckCircle
        MessageType.ERROR -> ErrorColor to Icons.Rounded.Error
        MessageType.INFO -> InfoColor to Icons.Rounded.Info
        MessageType.WARNING -> WarningColor to Icons.Rounded.Warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 80.dp),
        shape = RoundedCornerShape(16.dp),
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
                message.text,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            TextButton(onClick = onClose) {
                Text("×")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DashboardScreen(viewModel: MainViewModel) {

    val pullRefreshState = rememberPullRefreshState(
        refreshing = viewModel.isLoading,
        onRefresh = { viewModel.refreshAll(false) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            "ZIVPN Manager by YINN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Dashboard server & akun")
                    }

                    FilledIconButton(
                        onClick = { viewModel.refreshAll(false) }
                    ) {
                        Icon(Icons.Rounded.Refresh, null)
                    }
                }
            }

            item {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    StatCard(
                        Modifier.weight(1f),
                        "Users",
                        viewModel.users.size.toString(),
                        Icons.Rounded.People
                    )

                    PingStatusCard(
                        Modifier.weight(1f),
                        viewModel.pingMs
                    )
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

@Composable
private fun PingStatusCard(modifier: Modifier, pingMs: Long) {

    val color =
        when {
            pingMs < 0 -> ErrorColor
            pingMs in 1..389 -> SuccessColor
            pingMs in 390..599 -> WarningColor
            else -> ErrorColor
        }

    val text =
        if (pingMs < 0) "Offline"
        else "$pingMs ms"

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text("Status")

            Text(
                text,
                color = color,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = color.copy(alpha = 0.9f),
                        offset = Offset.Zero,
                        blurRadius = 16f
                    )
                )
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Icon(icon, null)

            Text(title)

            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}