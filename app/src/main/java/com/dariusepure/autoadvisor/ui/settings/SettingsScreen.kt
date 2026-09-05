package com.dariusepure.autoadvisor.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dariusepure.autoadvisor.R
import com.dariusepure.autoadvisor.ui.common.supportedLanguages
import com.dariusepure.autoadvisor.ui.theme.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val currentDark = isDarkMode ?: systemDark

    var languageMenuExpanded by remember { mutableStateOf(false) }

    val locales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!locales.isEmpty) locales.get(0)?.language ?: "en" else "en"
    val currentLanguage = supportedLanguages.find { it.code == currentLocale } ?: supportedLanguages[0]

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.common_settings)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text(stringResource(if (currentDark) R.string.theme_light_mode else R.string.theme_dark_mode)) },
                leadingContent = { Icon(if (currentDark) Icons.Default.LightMode else Icons.Default.DarkMode, null) },
                trailingContent = {
                    Switch(checked = currentDark, onCheckedChange = { viewModel.toggleTheme(currentDark) })
                },
                modifier = Modifier.clickable { viewModel.toggleTheme(currentDark) }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.common_language)) },
                leadingContent = { Icon(Icons.Outlined.Language, null) },
                trailingContent = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { languageMenuExpanded = true }
                        ) {
                            Text(
                                text = "${currentLanguage.flag} ${currentLanguage.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        DropdownMenu(
                            expanded = languageMenuExpanded,
                            onDismissRequest = { languageMenuExpanded = false }
                        ) {
                            supportedLanguages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text("${language.flag} ${language.name}") },
                                    onClick = {
                                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(language.code)
                                        AppCompatDelegate.setApplicationLocales(appLocale)
                                        languageMenuExpanded = false
                                    },
                                    trailingIcon = {
                                        if (language.code == currentLocale) {
                                            Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.clickable { languageMenuExpanded = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.auth_logout),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onLogout() }
            )

            Spacer(Modifier.height(32.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.common_powered_by),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
