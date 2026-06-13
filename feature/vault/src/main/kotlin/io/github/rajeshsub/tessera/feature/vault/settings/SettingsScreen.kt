package io.github.rajeshsub.tessera.feature.vault.settings

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import javax.crypto.Cipher

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onExport: () -> Unit,
    onBackupRestore: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val clipboardTimeout by viewModel.clipboardTimeout.collectAsState()
    val sntpEnabled by viewModel.sntpEnabled.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val enrollBusy by viewModel.enrollBusy.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showEnrollDialog by remember { mutableStateOf(false) }
    var enrollPassword by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SettingsViewModel.UiEffect.PromptBiometric -> {
                    showEnrollDialog = false
                    showBiometricEnrollPrompt(context, effect.cipher) { authenticatedCipher ->
                        viewModel.finishBiometricEnrollment(authenticatedCipher, enrollPassword.toCharArray())
                        enrollPassword = ""
                    }
                }
                is SettingsViewModel.UiEffect.BiometricEnrolled ->
                    snackbar.showSnackbar("Biometric unlocking enabled")
                is SettingsViewModel.UiEffect.BiometricDisabled ->
                    snackbar.showSnackbar("Biometric unlocking disabled")
                is SettingsViewModel.UiEffect.Error -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    if (showEnrollDialog) {
        EnrollBiometricDialog(
            password = enrollPassword,
            onPasswordChange = { enrollPassword = it },
            isBusy = enrollBusy,
            onConfirm = {
                viewModel.enrollBiometric(enrollPassword.toCharArray())
            },
            onDismiss = {
                showEnrollDialog = false
                enrollPassword = ""
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            SettingHeader("Security")
            SettingRow("Clipboard clear timeout: ${clipboardTimeout}s") {
                Slider(
                    value = clipboardTimeout.toFloat(),
                    onValueChange = { viewModel.setClipboardTimeout(it.toInt()) },
                    valueRange = 10f..120f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HorizontalDivider()
            SwitchRow(
                label = "Biometric unlock",
                checked = biometricEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showEnrollDialog = true
                    } else {
                        viewModel.disableBiometric()
                    }
                },
            )
            HorizontalDivider()
            SettingHeader("Time")
            SwitchRow(
                label = "SNTP drift advisory",
                checked = sntpEnabled,
                onCheckedChange = viewModel::setSntpEnabled,
            )
            HorizontalDivider()
            SettingHeader("Vault")
            Button(
                onClick = onBackupRestore,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Backup / Restore") }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Export accounts") }
        }
    }
}

@Composable
private fun SettingHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingRow(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        content()
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun EnrollBiometricDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    isBusy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text("Enable biometric unlock") },
        text = {
            Column {
                Text("Enter your current passphrase to enable biometric unlock.")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Passphrase") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isBusy) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = password.isNotEmpty() && !isBusy,
            ) { Text("Enable") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) { Text("Cancel") }
        },
    )
}

private fun showBiometricEnrollPrompt(
    context: android.content.Context,
    cipher: Cipher,
    onSuccess: (Cipher) -> Unit,
) {
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt =
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.cipher?.let(onSuccess)
                }
            },
        )
    val info =
        BiometricPrompt.PromptInfo
            .Builder()
            .setTitle("Enroll biometric")
            .setSubtitle("Authenticate to enable biometric unlock")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
}
