package io.github.rajeshsub.tessera.feature.vault

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.rajeshsub.tessera.feature.vault.add.AddAccountScreen
import io.github.rajeshsub.tessera.feature.vault.add.QrScanScreen
import io.github.rajeshsub.tessera.feature.vault.backup.BackupRestoreScreen
import io.github.rajeshsub.tessera.feature.vault.edit.EditAccountScreen
import io.github.rajeshsub.tessera.feature.vault.export.ExportWarningScreen
import io.github.rajeshsub.tessera.feature.vault.list.AccountListScreen
import io.github.rajeshsub.tessera.feature.vault.navigation.AccountList
import io.github.rajeshsub.tessera.feature.vault.navigation.AddAccount
import io.github.rajeshsub.tessera.feature.vault.navigation.BackupRestore
import io.github.rajeshsub.tessera.feature.vault.navigation.EditAccount
import io.github.rajeshsub.tessera.feature.vault.navigation.ExportWarning
import io.github.rajeshsub.tessera.feature.vault.navigation.QrScan
import io.github.rajeshsub.tessera.feature.vault.navigation.Settings
import io.github.rajeshsub.tessera.feature.vault.navigation.Unlock
import io.github.rajeshsub.tessera.feature.vault.settings.SettingsScreen
import io.github.rajeshsub.tessera.feature.vault.unlock.UnlockScreen

@Composable
fun TesseraNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Unlock,
        modifier = modifier,
    ) {
        composable<Unlock> {
            UnlockScreen(onUnlocked = { navController.navigate(AccountList) })
        }
        composable<AccountList> {
            AccountListScreen(
                onNavigateToAdd = { navController.navigate(AddAccount()) },
                onNavigateToEdit = { id -> navController.navigate(EditAccount(id)) },
                onNavigateToSettings = { navController.navigate(Settings) },
                onLocked = {
                    navController.navigate(Unlock) {
                        popUpTo(AccountList) { inclusive = true }
                    }
                },
            )
        }
        composable<AddAccount> {
            AddAccountScreen(
                onBack = { navController.popBackStack() },
                onQrScan = { navController.navigate(QrScan) },
            )
        }
        composable<QrScan> {
            QrScanScreen(onBack = { navController.popBackStack() })
        }
        composable<EditAccount> {
            EditAccountScreen(onBack = { navController.popBackStack() })
        }
        composable<Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onExport = { navController.navigate(ExportWarning) },
                onBackupRestore = { navController.navigate(BackupRestore) },
            )
        }
        composable<ExportWarning> {
            ExportWarningScreen(onBack = { navController.popBackStack() })
        }
        composable<BackupRestore> {
            BackupRestoreScreen(
                onBack = { navController.popBackStack() },
                onRestored = {
                    navController.navigate(Unlock) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
