package io.github.rajeshsub.tessera.feature.vault.unlock

import io.github.rajeshsub.tessera.feature.vault.fake.FakeVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnlockViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeVaultRepository

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeVaultRepository()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm() =
        UnlockViewModel(
            vaultRepository = repo,
            biometricKeystore = FakeBiometricKeystore(),
        )

    @Test fun initialStateIsIdle() =
        runTest {
            val vm = makeVm()
            assertEquals(UnlockViewModel.State.Idle, vm.state.value)
        }

    @Test fun vaultExistsFalseByDefault() =
        runTest {
            assertFalse(makeVm().vaultExists)
        }

    @Test fun createVaultTransitionsToUnlocked() =
        runTest {
            val vm = makeVm()
            vm.createVault(charArrayOf('p'))
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(UnlockViewModel.State.Unlocked, vm.state.value)
        }

    @Test fun unlockSuccessTransitionsToUnlocked() =
        runTest {
            repo.setExists(true)
            repo.unlockShouldSucceed = true
            val vm = makeVm()
            vm.unlock(charArrayOf('p'))
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(UnlockViewModel.State.Unlocked, vm.state.value)
        }

    @Test fun unlockFailureTransitionsToError() =
        runTest {
            repo.setExists(true)
            repo.unlockShouldSucceed = false
            val vm = makeVm()
            vm.unlock(charArrayOf('p'))
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(vm.state.value is UnlockViewModel.State.Error)
        }

    @Test fun clearErrorResetsToIdle() =
        runTest {
            repo.setExists(true)
            repo.unlockShouldSucceed = false
            val vm = makeVm()
            vm.unlock(charArrayOf('p'))
            dispatcher.scheduler.advanceUntilIdle()
            vm.clearError()
            assertEquals(UnlockViewModel.State.Idle, vm.state.value)
        }

    @Test fun biometricEnrolledFalseWhenNotEnrolled() {
        assertFalse(makeVm().biometricEnrolled)
    }
}

private class FakeBiometricKeystore :
    io.github.rajeshsub.tessera.data.vault.BiometricKeystore(
        java.io.File(System.getProperty("java.io.tmpdir"), "fake_biometric_test.enc"),
    ) {
    override fun isEnrolled() = false

    override fun isKeyAvailable() = false
}
