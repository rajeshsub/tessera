package io.github.rajeshsub.tessera.feature.vault.unlock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel

@Suppress("LongMethod")
@Composable
fun UnlockScreen(
    onUnlocked: () -> Unit,
    viewModel: UnlockViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val isCreating = !viewModel.vaultExists

    LaunchedEffect(state) {
        when (val s = state) {
            is UnlockViewModel.State.Unlocked -> onUnlocked()
            is UnlockViewModel.State.Error -> {
                snackbar.showSnackbar(s.message)
                viewModel.clearError()
            }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Tessera", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                if (isCreating) "Set up your vault" else "Welcome back",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            if (isCreating) {
                PassphraseWarningBox()
                Spacer(Modifier.height(20.dp))
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(if (isCreating) "Choose a passphrase" else "Passphrase") },
                visualTransformation =
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide passphrase" else "Show passphrase",
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isCreating) ImeAction.Next else ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = { if (!isCreating) submitPassword(viewModel, password, password) },
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (isCreating) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm passphrase") },
                    visualTransformation =
                        if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (confirmVisible) "Hide passphrase" else "Show passphrase",
                            )
                        }
                    },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = { submitPassword(viewModel, password, confirm) },
                        ),
                    isError = confirm.isNotEmpty() && confirm != password,
                    supportingText = {
                        if (confirm.isNotEmpty() && confirm != password) {
                            Text("Passphrases do not match")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(16.dp))

            if (state == UnlockViewModel.State.Busy) {
                CircularProgressIndicator()
            } else {
                val canSubmit =
                    password.isNotEmpty() &&
                        (!isCreating || (confirm == password && confirm.isNotEmpty()))
                Button(
                    onClick = { submitPassword(viewModel, password, confirm) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit,
                ) {
                    Text(if (isCreating) "Create vault" else "Unlock")
                }
                if (!isCreating && viewModel.biometricEnrolled) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showBiometricPrompt(context, viewModel) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Use Biometric")
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PassphraseWarningBox() {
    val warningColor = MaterialTheme.colorScheme.error
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, warningColor, RoundedCornerShape(8.dp))
                .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = warningColor,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                "Your passphrase cannot be recovered or changed",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = warningColor,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "This passphrase is the only key to your vault. If you forget it, " +
                "all your 2FA accounts on this device will be permanently inaccessible — " +
                "there is no reset, recovery, or change option.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Store it in a password manager or write it down somewhere secure before continuing.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun submitPassword(
    viewModel: UnlockViewModel,
    password: String,
    confirm: String,
) {
    if (viewModel.vaultExists) {
        viewModel.unlock(password.toCharArray())
    } else {
        if (password == confirm && password.isNotEmpty()) {
            viewModel.createVault(password.toCharArray())
        }
    }
}

private fun showBiometricPrompt(
    context: android.content.Context,
    viewModel: UnlockViewModel,
) {
    val cipher = viewModel.getBiometricDecryptCipher() ?: return
    val activity = context as? FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt =
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.cipher?.let { viewModel.unlockWithBiometric(it) }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    // silent — user can fall back to passphrase
                }
            },
        )
    val info =
        BiometricPrompt.PromptInfo
            .Builder()
            .setTitle("Unlock Tessera")
            .setSubtitle("Authenticate to access your vault")
            .setNegativeButtonText("Use passphrase")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    prompt.authenticate(info, BiometricPrompt.CryptoObject(cipher))
}
