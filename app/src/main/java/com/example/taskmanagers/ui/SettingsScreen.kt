package com.example.taskmanagers.ui
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.example.taskmanagers.R
import com.example.taskmanagers.data.local.TaskPriority
import com.example.taskmanagers.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel) {
    val darkmode by viewModel.isDarkMode.collectAsState()
    val notifications by viewModel.notificationsEnabled.collectAsState()
    val defaultPriority by viewModel.defaultPriority.collectAsState()
    val defaultNotifyBefore by viewModel.defaultNotifyBefore.collectAsState()
    Scaffold { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.language), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val appLocales = LocaleListCompat.forLanguageTags("pl")
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocales)
                    },
                    colors = if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags() == "pl")
                        ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(stringResource(R.string.lang_pl))
                }
                Button(
                    onClick = {
                        val appLocales = LocaleListCompat.forLanguageTags("en")
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocales)
                    },
                    colors = if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags().startsWith("en") ||
                        androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().isEmpty)
                        ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(stringResource(R.string.lang_en))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(stringResource(R.string.dark_mode), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = darkmode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(stringResource(R.string.notifications), style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = notifications,
                    onCheckedChange = { viewModel.setNotifications(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.default_priority), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { priority ->
                    val priorityText = when(priority) {
                        TaskPriority.LOW -> stringResource(R.string.priority_low)
                        TaskPriority.MEDIUM -> stringResource(R.string.priority_medium)
                        TaskPriority.HIGH -> stringResource(R.string.priority_high)
                    }
                    Button(
                        onClick = { viewModel.setDefaultPriority(priority) },
                        colors = if (priority == defaultPriority)
                            ButtonDefaults.buttonColors()
                        else
                            ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(priorityText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.default_reminder_time), style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 60).forEach { minutes ->
                    Button(
                        onClick = { viewModel.setDefaultNotifyBefore(minutes) },
                        colors = if (minutes == defaultNotifyBefore)
                            ButtonDefaults.buttonColors()
                        else
                            ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("$minutes min")
                    }
                }
            }
        }
    }
}
