package io.github.rajeshsub.tessera.feature.vault.list

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.rajeshsub.tessera.core.model.OtpType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun AccountListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onLocked: () -> Unit,
    viewModel: AccountListViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AccountListViewModel.UiEffect.CopyToClipboard -> {
                    copyToClipboard(context, effect.code)
                    launch {
                        delay(effect.timeoutMs)
                        clearClipboard(context)
                    }
                    snackbar.showSnackbar("Code copied — clears in ${effect.timeoutMs / 1_000}s")
                }
                is AccountListViewModel.UiEffect.ShowSnackbar ->
                    snackbar.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tessera") },
                actions = {
                    IconButton(onClick = {
                        viewModel.lock()
                        onLocked()
                    }) { Icon(Icons.Default.Lock, "Lock") }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, "Add account")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No accounts yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(16.dp),
            ) {
                items(items, key = { it.account.id }) { item ->
                    AccountItem(
                        item = item,
                        onTapReveal = { viewModel.toggleReveal(item.account.id) },
                        onCopy = { viewModel.copyCode(item.account.id) },
                        onNextHotp = { viewModel.advanceHotp(item.account.id) },
                        onEdit = { onNavigateToEdit(item.account.id) },
                        onDelete = { viewModel.deleteAccount(item.account.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountItem(
    item: AccountListItem,
    onTapReveal: () -> Unit,
    onCopy: () -> Unit,
    onNextHotp: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val account = item.account
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(account.label, style = MaterialTheme.typography.titleMedium)
                    account.issuer?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
            }

            Spacer(Modifier.height(8.dp))

            if (account.type == OtpType.TOTP) {
                val code = if (item.isRevealed) item.currentCode else "••••••"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onTapReveal, modifier = Modifier.weight(1f)) {
                        Text(code, style = MaterialTheme.typography.headlineSmall)
                    }
                    if (item.isRevealed) {
                        TextButton(onClick = onCopy) { Text("Copy") }
                    }
                }
                val progress = item.secondsRemaining.toFloat() / account.period.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color =
                        if (item.secondsRemaining <= 5) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                )
                Text(
                    "${item.secondsRemaining}s",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.End),
                )
            } else {
                if (item.currentCode.isEmpty()) {
                    TextButton(onClick = onNextHotp) { Text("Get next code") }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.currentCode, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = onCopy) { Text("Copy") }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(
    context: Context,
    text: String,
) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    val clip =
        ClipData.newPlainText("OTP", text).also { data ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data.description.extras =
                    PersistableBundle().apply {
                        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
            }
        }
    cm.setPrimaryClip(clip)
}

private fun clearClipboard(context: Context) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        cm.clearPrimaryClip()
    }
}
