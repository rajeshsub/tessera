package io.github.rajeshsub.tessera.feature.vault.settings

import io.github.rajeshsub.tessera.feature.vault.fake.FakePrefsRepository
import io.github.rajeshsub.tessera.feature.vault.fake.FakeVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var prefs: FakePrefsRepository
    private lateinit var vault: FakeVaultRepository

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        prefs = FakePrefsRepository()
        vault = FakeVaultRepository()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm() =
        SettingsViewModel(
            prefsRepository = prefs,
            biometricKeystore = FakeBiometricKeystore(),
            vaultRepository = vault,
        )

    @Test fun defaultClipboardTimeoutIs30() =
        runTest {
            val vm = makeVm()
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(30, vm.clipboardTimeout.value)
        }

    @Test fun setClipboardTimeoutUpdatesPrefs() =
        runTest {
            val vm = makeVm()
            backgroundScope.launch { vm.clipboardTimeout.collect {} }
            vm.setClipboardTimeout(60)
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(60, vm.clipboardTimeout.value)
        }

    @Test fun sntpEnabledDefaultFalse() =
        runTest {
            val vm = makeVm()
            dispatcher.scheduler.advanceUntilIdle()
            assertFalse(vm.sntpEnabled.value)
        }

    @Test fun setSntpEnabledUpdatesPrefs() =
        runTest {
            val vm = makeVm()
            backgroundScope.launch { vm.sntpEnabled.collect {} }
            vm.setSntpEnabled(true)
            dispatcher.scheduler.advanceUntilIdle()
            assert(vm.sntpEnabled.value)
        }

    @Test fun defaultThemeIsSystem() =
        runTest {
            val vm = makeVm()
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals("system", vm.theme.value)
        }

    @Test fun setThemeUpdatesPrefs() =
        runTest {
            val vm = makeVm()
            backgroundScope.launch { vm.theme.collect {} }
            vm.setTheme("dark")
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals("dark", vm.theme.value)
        }

    @Test fun enrollBiometricWrongPasswordSendsError() =
        runTest {
            vault.unlockShouldSucceed = false
            val vm = makeVm()
            val effects = mutableListOf<SettingsViewModel.UiEffect>()
            backgroundScope.launch { vm.effects.collect { effects.add(it) } }
            vm.enrollBiometric("wrong".toCharArray())
            dispatcher.scheduler.advanceUntilIdle()
            assert(effects.any { it is SettingsViewModel.UiEffect.Error })
            assertFalse(vm.enrollBusy.value)
        }
}

private class FakeBiometricKeystore :
    io.github.rajeshsub.tessera.data.vault.BiometricKeystore(
        java.io.File(System.getProperty("java.io.tmpdir"), "fake_biometric_settings_test.enc"),
    ) {
    override fun isEnrolled() = false

    override fun isKeyAvailable() = false
}
