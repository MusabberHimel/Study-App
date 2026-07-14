package com.musabber.pomofocus.ui.screens

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musabber.pomofocus.ui.components.AppIcons
import com.musabber.pomofocus.viewmodel.SettingsViewModel
import android.widget.Toast
import com.musabber.pomofocus.util.BackupHelper
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    val studyDuration by viewModel.studyDuration.collectAsState()
    val shortBreakDuration by viewModel.shortBreakDuration.collectAsState()
    val longBreakDuration by viewModel.longBreakDuration.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val alarmSoundName by viewModel.alarmSoundName.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val vibrationPattern by viewModel.vibrationPattern.collectAsState()
    val vibrationCustomPattern by viewModel.vibrationCustomPattern.collectAsState()
    val vibrationDurationMs by viewModel.vibrationDurationMs.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val selectedIcon by viewModel.selectedIcon.collectAsState()

    var showThemeMenu by remember { mutableStateOf(false) }
    var showVibeMenu by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                val ringtone = RingtoneManager.getRingtone(context, it)
                val name = ringtone?.getTitle(context) ?: "Unknown Sound"
                viewModel.setAlarmSound(it.toString(), name)
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var name = "Custom File"
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex)
                    }
                }
            }
            viewModel.setAlarmSound(it.toString(), name)
        }
    }

    val dndPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            viewModel.setDndEnabled(true)
        } else {
            viewModel.setDndEnabled(false)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            SettingsSectionHeader("Timer Customization")
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DurationInputField(
                    label = "Study Duration (5-90 min)",
                    value = studyDuration,
                    onValueChange = { viewModel.setStudyDuration(it) },
                    range = 5..90
                )
                DurationInputField(
                    label = "Short Break (1-30 min)",
                    value = shortBreakDuration,
                    onValueChange = { viewModel.setShortBreakDuration(it) },
                    range = 1..30
                )
                DurationInputField(
                    label = "Long Break (5-60 min)",
                    value = longBreakDuration,
                    onValueChange = { viewModel.setLongBreakDuration(it) },
                    range = 5..60
                )
            }
        }

        item {
            SettingsSectionHeader("Do Not Disturb")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable DND during study sessions", fontSize = 16.sp)
                    Text("Silences notifications during work", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = dndEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (notificationManager.isNotificationPolicyAccessGranted) {
                                viewModel.setDndEnabled(true)
                            } else {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                dndPermissionLauncher.launch(intent)
                            }
                        } else {
                            viewModel.setDndEnabled(false)
                        }
                    }
                )
            }
        }

        item {
            SettingsSectionHeader("Alarm Sound")
            Spacer(modifier = Modifier.height(12.dp))
            Text("Selected: $alarmSoundName", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_NOTIFICATION)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        }
                        ringtonePickerLauncher.launch(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ringtones", fontSize = 12.sp)
                }
                Button(
                    onClick = { filePickerLauncher.launch("audio/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Custom File", fontSize = 12.sp)
                }
            }
        }

        item {
            SettingsSectionHeader("Vibration")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Vibration", fontSize = 16.sp)
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }

            if (vibrationEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    OutlinedButton(onClick = { showVibeMenu = true }) {
                        Text("Pattern: $vibrationPattern")
                    }
                    DropdownMenu(
                        expanded = showVibeMenu,
                        onDismissRequest = { showVibeMenu = false }
                    ) {
                        val patterns = listOf("Single pulse", "Double pulse", "Triple pulse", "Long pulse", "Custom")
                        patterns.forEach { pattern ->
                            DropdownMenuItem(
                                text = { Text(pattern) },
                                onClick = {
                                    viewModel.setVibrationPattern(pattern)
                                    showVibeMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (vibrationPattern == "Custom") {
                    OutlinedTextField(
                        value = vibrationCustomPattern,
                        onValueChange = { viewModel.setVibrationCustomPattern(it) },
                        label = { Text("Custom Pattern (e.g. 0,500,200,500)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Pulse Duration: ${vibrationDurationMs}ms", fontSize = 14.sp)
                    Slider(
                        value = vibrationDurationMs.toFloat(),
                        onValueChange = { viewModel.setVibrationDuration(it.toInt()) },
                        valueRange = 100f..2000f,
                        steps = 19
                    )
                }
            }
        }

        item {
            SettingsSectionHeader("Appearance / Theme")
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                OutlinedButton(onClick = { showThemeMenu = true }) {
                    Text("Theme: $selectedTheme")
                }
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    val themes = listOf("Dark", "Classic", "Cyberpunk", "Retro", "Royal Gold")
                    themes.forEach { theme ->
                        DropdownMenuItem(
                            text = { Text(theme) },
                            onClick = {
                                viewModel.setTheme(theme)
                                showThemeMenu = false
                            }
                        )
                    }
                }
            }
        }

        item {
            SettingsSectionHeader("Choose Display Icon")
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppIcons.iconMap.keys.toList()) { iconName ->
                    val isSelected = iconName == selectedIcon
                    val icon = AppIcons.getIcon(iconName)
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { viewModel.setIcon(iconName) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = iconName,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "About Developer",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun DurationInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newVal ->
            textValue = newVal
            val parsed = newVal.toIntOrNull()
            if (parsed != null && parsed in range) {
                onValueChange(parsed)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
