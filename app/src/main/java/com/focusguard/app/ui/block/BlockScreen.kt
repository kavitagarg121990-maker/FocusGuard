package com.focusguard.app.ui.block

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguard.app.data.local.entity.ShortContentRuleEntity
import com.focusguard.app.data.model.InstalledAppInfo
import com.focusguard.app.service.FocusDeviceAdminReceiver
import com.focusguard.app.ui.components.ProBadgePill
import com.focusguard.app.ui.theme.AmoledBlack
import com.focusguard.app.ui.theme.CardBorderDark
import com.focusguard.app.ui.theme.CardDark
import com.focusguard.app.ui.theme.GuardGreen
import com.focusguard.app.ui.theme.GuardRed
import com.focusguard.app.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlockScreen(
    onOpenHelp: () -> Unit,
    viewModel: BlockViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAppPickerSheet by remember { mutableStateOf(false) }
    var showWebsiteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val limitedOrBlockedApps = remember(uiState.installedApps) {
        uiState.installedApps.filter { it.isBlocked || it.dailyLimitMinutes != null }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocks",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp
                    ),
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.clickable { onOpenHelp() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Help",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        // App Limits Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Limits",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showAppPickerSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = GuardGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add App",
                        color = GuardGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (limitedOrBlockedApps.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAppPickerSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1A3316),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = GuardGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No app limits active",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Tap + Add App to block apps or set daily limits.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else {
            items(limitedOrBlockedApps, key = { it.packageName }) { appInfo ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { viewModel.selectAppForDetail(appInfo) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (appInfo.icon != null) {
                            Image(
                                bitmap = appInfo.icon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = appInfo.appName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF262A27),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = appInfo.appName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appInfo.appName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    appInfo.isBlocked -> "Blocked 0ms"
                                    appInfo.dailyLimitMinutes != null -> "${appInfo.dailyLimitMinutes}m Limit"
                                    else -> "Active"
                                },
                                color = if (appInfo.isBlocked) GuardRed else TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF242825),
                            modifier = Modifier.clickable { viewModel.selectAppForDetail(appInfo) }
                        ) {
                            Text(
                                text = "Edit",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Block Shorts PRO Section
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Block Shorts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                ProBadgePill()
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(uiState.shortContentRules, key = { it.id }) { rule ->
            ShortRuleCard(
                rule = rule,
                onToggle = { viewModel.toggleShortContentRule(rule.id, !rule.isEnabled) }
            )
        }

        // Other blocks PRO Section
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Other blocks",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                ProBadgePill()
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWebsiteSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF232724),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFFB7BEBA)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block Websites",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${uiState.blockedWebsites.size} domains guarded",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF242825)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Strict Mode Protections PRO Section
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Strict mode protections",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                ProBadgePill()
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF232724),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = GuardGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Block FocusGuard uninstall",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Prevents tampering or disabling shield during active blocks",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = uiState.isStrictModeEnabled,
                        onCheckedChange = { enable ->
                            viewModel.toggleStrictMode(enable) {}
                            if (enable) {
                                try {
                                    val comp = ComponentName(context, FocusDeviceAdminReceiver::class.java)
                                    val dpm = context.getSystemService(DevicePolicyManager::class.java)
                                    if (dpm != null && !dpm.isAdminActive(comp)) {
                                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp)
                                            putExtra(
                                                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                                "Prevents uninstalling FocusGuard while Strict Mode is active."
                                            )
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GuardGreen,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF282D2A)
                        )
                    )
                }
            }
        }
    }

    // Add / Manage Apps Picker Sheet
    if (showAppPickerSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showAppPickerSheet = false },
            sheetState = sheetState,
            containerColor = CardDark,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(540.dp)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Select Apps to Guard",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search installed apps...", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GuardGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.toggleShowSystemApps() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (uiState.showSystemApps) Color(0xFF1F3B1A) else Color(0xFF202421))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "System Apps",
                            tint = if (uiState.showSystemApps) GuardGreen else TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoadingApps) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GuardGreen)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.filteredApps, key = { it.packageName }) { appInfo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAppPickerSheet = false
                                        viewModel.selectAppForDetail(appInfo)
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (appInfo.icon != null) {
                                    Image(
                                        bitmap = appInfo.icon.toBitmap(84, 84).asImageBitmap(),
                                        contentDescription = appInfo.appName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = appInfo.appName,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when {
                                            appInfo.isBlocked -> "Blocked 0ms"
                                            appInfo.dailyLimitMinutes != null -> "${appInfo.dailyLimitMinutes}m / day"
                                            else -> "Tap to configure"
                                        },
                                        color = if (appInfo.isBlocked) GuardRed else TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                                Switch(
                                    checked = appInfo.isBlocked,
                                    onCheckedChange = { viewModel.toggleAppBlock(appInfo, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GuardGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Website Blocker Sheet
    if (showWebsiteSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var newDomain by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showWebsiteSheet = false },
            sheetState = sheetState,
            containerColor = CardDark,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Blocked Websites",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newDomain,
                        onValueChange = { newDomain = it },
                        placeholder = { Text("e.g. youtube.com", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GuardGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newDomain.isNotBlank()) {
                                viewModel.addBlockedWebsite(newDomain)
                                newDomain = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GuardGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("instagram.com", "youtube.com", "tiktok.com", "reddit.com", "x.com").forEach { preset ->
                        val active = uiState.blockedWebsites.contains(preset)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) Color(0xFF1B3516) else Color(0xFF202421),
                            border = BorderStroke(1.dp, if (active) GuardGreen else CardBorderDark),
                            modifier = Modifier.clickable {
                                if (active) viewModel.removeBlockedWebsite(preset)
                                else viewModel.addBlockedWebsite(preset)
                            }
                        ) {
                            Text(
                                text = if (active) "✓ $preset" else "+ $preset",
                                color = if (active) GuardGreen else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                uiState.blockedWebsites.forEach { domain ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = domain, color = Color.White, fontSize = 14.sp)
                        IconButton(
                            onClick = { viewModel.removeBlockedWebsite(domain) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Existing App Detail Bottom Sheet
    uiState.selectedAppForDetail?.let { appInfo ->
        AppDetailBottomSheet(
            appInfo = appInfo,
            onDismiss = { viewModel.selectAppForDetail(null) },
            onSave = { isBlocked, dailyLimit ->
                viewModel.toggleAppBlock(appInfo, isBlocked)
                viewModel.setAppDailyLimit(appInfo, dailyLimit)
            }
        )
    }
}

@Composable
private fun ShortRuleCard(
    rule: ShortContentRuleEntity,
    onToggle: () -> Unit
) {
    val badgeColor = when {
        rule.id.contains("youtube") -> Color(0xFFFF1744)
        rule.id.contains("instagram") -> Color(0xFFE1306C)
        rule.id.contains("facebook") -> Color(0xFF1877F2)
        rule.id.contains("snapchat") -> Color(0xFFFFD600)
        else -> Color(0xFF69C9D0)
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardDark,
        border = BorderStroke(1.dp, CardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = badgeColor.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = rule.displayName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(50),
                color = if (rule.isEnabled) Color(0xFF1D3B16) else Color(0xFF242825),
                border = if (rule.isEnabled) BorderStroke(1.dp, GuardGreen) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (rule.isEnabled) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = if (rule.isEnabled) GuardGreen else Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (rule.isEnabled) "Active" else "Add",
                        color = if (rule.isEnabled) GuardGreen else Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
