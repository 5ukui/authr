package com.sukui.authenticator.ui.screen.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sukui.authenticator.R
import com.sukui.authenticator.ui.component.lazygroup.itemGrouped
import com.sukui.authenticator.ui.component.rememberBiometricHandler
import com.sukui.authenticator.ui.component.rememberBiometricPromptData
import com.sukui.authenticator.ui.screen.settings.component.SettingsNavigateItem
import com.sukui.authenticator.ui.screen.settings.component.SettingsSwitchItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSetupPinCode: () -> Unit,
    onDisablePinCode: () -> Unit,
    onThemeNavigate: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val secureMode by viewModel.secureMode.collectAsStateWithLifecycle()
    val pinLock by viewModel.pinLock.collectAsStateWithLifecycle()
    val biometrics by viewModel.biometrics.collectAsStateWithLifecycle()

    val biometricHandler = rememberBiometricHandler(
        onAuthSuccess = viewModel::toggleBiometrics
    )
    val setupPromptData = rememberBiometricPromptData(
        title = stringResource(R.string.settings_biometrics_setup_title),
        negativeButtonText = stringResource(R.string.settings_biometrics_setup_cancel)
    )
    val disablePromptData = rememberBiometricPromptData(
        title = stringResource(R.string.settings_biometrics_disable_title),
        negativeButtonText = stringResource(R.string.settings_biometrics_disable_cancel)
    )

    BackHandler(onBack = onBack)
    SettingsScreen(
        onBack = onBack,
        secureMode = secureMode,
        onSecureModeChange = viewModel::updateSecureMode,
        pinCode = pinLock,
        onPinCodeChange = {
            if (it) {
                onSetupPinCode()
            } else {
                onDisablePinCode()
            }
        },
        showBiometrics = biometricHandler.canUseBiometrics(),
        biometrics = biometrics,
        onBiometricsChange = {
            val promptData = if (it) setupPromptData else disablePromptData
            biometricHandler.requestBiometrics(promptData)
        },
        onThemeNavigate = onThemeNavigate
    )
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    secureMode: Boolean,
    onSecureModeChange: (Boolean) -> Unit,
    pinCode: Boolean,
    onPinCodeChange: (Boolean) -> Unit,
    showBiometrics: Boolean,
    biometrics: Boolean,
    onBiometricsChange: (Boolean) -> Unit,
    onThemeNavigate: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(16.dp),
        ) {
            itemGrouped(header = { Text(stringResource(R.string.settings_category_security)) }) {
                SettingsSwitchItem(
                    onCheckedChange = onSecureModeChange,
                    checked = secureMode,
                    title = {
                        Text(stringResource(R.string.settings_prefs_securemode))
                    },
                    description = {
                        Text(stringResource(R.string.settings_prefs_securemode_description))
                    }
                )
                SettingsSwitchItem(
                    onCheckedChange = onPinCodeChange,
                    checked = pinCode,
                    title = {
                        Text(stringResource(R.string.settings_prefs_pincode))
                    },
                    description = {
                        Text(stringResource(R.string.settings_prefs_pincode_description))
                    }
                )
                if (showBiometrics) {
                    SettingsSwitchItem(
                        onCheckedChange = onBiometricsChange,
                        checked = biometrics,
                        title = {
                            Text(stringResource(R.string.settings_prefs_biometrics))
                        },
                        enabled = pinCode
                    )
                }
            }
            itemGrouped(header = { Text(stringResource(R.string.settings_category_appearance)) }) {
                SettingsNavigateItem(
                    onClick = onThemeNavigate,
                    title = {
                        Text(stringResource(R.string.settings_prefs_theme))
                    },
                )
            }
        }
    }
}