package com.dariusepure.gofindauto.ui.settings

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
import com.dariusepure.gofindauto.R
import com.dariusepure.gofindauto.ui.common.supportedLanguages
import com.dariusepure.gofindauto.ui.theme.SettingsViewModel

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
    val userEmail = viewModel.getUserEmail() ?: ""

    var languageMenuExpanded by remember { mutableStateOf(false) }

    val locales = AppCompatDelegate.getApplicationLocales()
    val currentLocale = if (!locales.isEmpty) locales.get(0)?.language ?: "en" else "en"
    val currentLanguage = supportedLanguages.find { it.code == currentLocale } ?: supportedLanguages[0]

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.common_settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Profile Header
            ProfileHeader(email = userEmail)

            SettingsSection(title = stringResource(R.string.settings_section_appearance)) {
                ListItem(
                    headlineContent = { Text(stringResource(if (currentDark) R.string.theme_light_mode else R.string.theme_dark_mode)) },
                    leadingContent = {
                        Icon(
                            imageVector = if (currentDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = currentDark,
                            onCheckedChange = { viewModel.toggleTheme(currentDark) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.toggleTheme(currentDark) }
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_general)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.common_language)) },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Language,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${currentLanguage.flag} ${currentLanguage.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    },
                    modifier = Modifier.clickable { languageMenuExpanded = true }
                )
            }

            SettingsSection(title = stringResource(R.string.settings_section_account)) {
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
            }

            Spacer(Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.common_powered_by),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }

    if (languageMenuExpanded) {
        AlertDialog(
            onDismissRequest = { languageMenuExpanded = false },
            title = { Text(stringResource(R.string.common_language)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    supportedLanguages.forEach { language ->
                        ListItem(
                            headlineContent = { Text("${language.flag} ${language.name}") },
                            trailingContent = {
                                if (language.code == currentLocale) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                val appLocale: LocaleListCompat =
                                    LocaleListCompat.forLanguageTags(language.code)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                languageMenuExpanded = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { languageMenuExpanded = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = stringResource(R.string.auth_signin_title), // Or "User Account"
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email.ifBlank { "guest@gofindauto.app" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Spacer(Modifier.height(16.dp))
    }
}
