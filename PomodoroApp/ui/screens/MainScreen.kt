package com.musabber.pomofocus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musabber.pomofocus.data.SessionType
import com.musabber.pomofocus.ui.components.AppIcons
import com.musabber.pomofocus.ui.components.CircularTimer
import com.musabber.pomofocus.viewmodel.TimerViewModel

@Composable
fun MainScreen(viewModel: TimerViewModel) {
    val remainingMillis by viewModel.remainingMillis.collectAsState()
    val totalDurationMillis by viewModel.totalDurationMillis.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val sessionType by viewModel.sessionType.collectAsState()
    val selectedIcon by viewModel.selectedIcon.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val breakMessages = listOf(
        "Break na! Break na! Boka sagol!",
        "Pani kha, hishu kor, torkari na 🤣",
        "Ei break e to dekhi roj ekta mobile! 📱",
        "Ektu নাচ, lebu patay chaa kha 🍋",
        "Tor break shesh, ebar kaj kor, noyto Himel re bolbo 😈",
        "5 minute por abar dhuuka marbi? 🤨",
        "Ei je, FB scroll kora bondho kor 📵",
        "Arey dada, phone rekhe ektu chokh duto thanda korun! 👀",
        "Acha break mane ki shudhu phone gutaano? 🧐"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (sessionType) {
                SessionType.STUDY -> "STUDY TIME"
                SessionType.SHORT_BREAK -> "SHORT BREAK"
                SessionType.LONG_BREAK -> "LONG BREAK"
            },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularTimer(
            remainingMillis = remainingMillis,
            totalDurationMillis = totalDurationMillis
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.resetTimer() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.toggleTimer() },
                modifier = Modifier.size(72.dp),
                shape = CardDefaults.shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { viewModel.skipTimer() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Skip Session",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = AppIcons.getIcon(selectedIcon),
            contentDescription = "Custom Action Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(64.dp)
                .clickable {
                    if (sessionType == SessionType.STUDY) {
                        dialogMessage = "Oi ekhon o mobile e ki?"
                    } else {
                        dialogMessage = breakMessages.random()
                    }
                    showDialog = true
                }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Oops, sorry!")
                }
            },
            title = { Text("Oi! 🛑") },
            text = { Text(dialogMessage) }
        )
    }
}