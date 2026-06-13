package io.github.rajeshsub.tessera.feature.vault.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.rajeshsub.tessera.core.model.OtpAlgorithm
import io.github.rajeshsub.tessera.core.model.OtpType

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
fun AddAccountScreen(
    onBack: () -> Unit,
    onQrScan: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AddAccountViewModel.UiEffect.Saved -> onBack()
                is AddAccountViewModel.UiEffect.Error -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Account") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onQrScan) {
                        Icon(Icons.Default.QrCode, "Scan QR")
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
            OutlinedTextField(
                value = form.label,
                onValueChange = viewModel::updateLabel,
                label = { Text("Label *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.issuer,
                onValueChange = viewModel::updateIssuer,
                label = { Text("Issuer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.secret,
                onValueChange = viewModel::updateSecret,
                label = { Text("Secret (Base32) *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("Type")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OtpType.entries.forEach { type ->
                    RadioButton(
                        selected = form.type == type,
                        onClick = { viewModel.updateType(type) },
                    )
                    Text(type.name)
                    Spacer(Modifier.padding(end = 16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            AlgorithmDropdown(
                selected = form.algorithm,
                onSelect = viewModel::updateAlgorithm,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.digits.toString(),
                onValueChange = { it.toIntOrNull()?.let(viewModel::updateDigits) },
                label = { Text("Digits") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            if (form.type == OtpType.TOTP) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.period.toString(),
                    onValueChange = { it.toIntOrNull()?.let(viewModel::updatePeriod) },
                    label = { Text("Period (seconds)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (form.type == OtpType.HOTP) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.counter.toString(),
                    onValueChange = { it.toLongOrNull()?.let(viewModel::updateCounter) },
                    label = { Text("Counter") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlgorithmDropdown(
    selected: OtpAlgorithm,
    onSelect: (OtpAlgorithm) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text("Algorithm") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OtpAlgorithm.entries.forEach { alg ->
                DropdownMenuItem(
                    text = { Text(alg.name) },
                    onClick = {
                        onSelect(alg)
                        expanded = false
                    },
                )
            }
        }
    }
}
