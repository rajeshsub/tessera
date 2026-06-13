package io.github.rajeshsub.tessera.data.vault

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
object VaultModule {
    @Provides
    @Singleton
    @VaultFile
    fun provideVaultFile(
        @ApplicationContext ctx: Context,
    ): File = File(ctx.filesDir, "tessera.vault")

    @Provides
    @Singleton
    @BiometricEncFile
    fun provideBiometricEncFile(
        @ApplicationContext ctx: Context,
    ): File = File(ctx.filesDir, "tessera.biometric.enc")

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext ctx: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { ctx.preferencesDataStoreFile("tessera_prefs") },
        )

    @Provides
    @Singleton
    fun provideVaultRepository(
        @VaultFile file: File,
    ): VaultRepository = VaultRepositoryImpl(VaultStorage(file))

    @Provides
    @Singleton
    fun providePrefsRepository(dataStore: DataStore<Preferences>): PrefsRepository = PrefsRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideSafBackupManager(
        @VaultFile file: File,
    ): SafBackupManager = SafBackupManager(file)

    @Provides
    @Singleton
    fun provideBiometricKeystore(
        @BiometricEncFile file: File,
    ): BiometricKeystore = BiometricKeystore(file)
}
