package io.github.rajeshsub.tessera.feature.vault.add

import app.cash.turbine.test
import io.github.rajeshsub.tessera.feature.vault.fake.FakeVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddAccountViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeVaultRepository

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeVaultRepository()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm() = AddAccountViewModel(repo)

    @Test fun initialFormIsEmpty() {
        val form = makeVm().form.value
        assertEquals("", form.label)
        assertEquals("", form.secret)
    }

    @Test fun updateLabelReflectsInForm() {
        val vm = makeVm()
        vm.updateLabel("Alice")
        assertEquals("Alice", vm.form.value.label)
    }

    @Test fun saveEmitsErrorForEmptyLabel() =
        runTest {
            val vm = makeVm()
            vm.effects.test {
                vm.save()
                dispatcher.scheduler.advanceUntilIdle()
                val effect = awaitItem()
                assertTrue(effect is AddAccountViewModel.UiEffect.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test fun saveEmitsErrorForInvalidSecret() =
        runTest {
            val vm = makeVm()
            vm.updateLabel("Alice")
            vm.updateSecret("NOT_BASE32!!!")
            vm.effects.test {
                vm.save()
                dispatcher.scheduler.advanceUntilIdle()
                val effect = awaitItem()
                assertTrue(effect is AddAccountViewModel.UiEffect.Error)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test fun saveValidAccountEmitsSaved() =
        runTest {
            val vm = makeVm()
            vm.updateLabel("Alice")
            vm.updateSecret("JBSWY3DPEHPK3PXP")
            vm.effects.test {
                vm.save()
                dispatcher.scheduler.advanceUntilIdle()
                assertEquals(AddAccountViewModel.UiEffect.Saved, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test fun saveValidAccountAddsToRepository() =
        runTest {
            val vm = makeVm()
            vm.updateLabel("Alice")
            vm.updateSecret("JBSWY3DPEHPK3PXP")
            vm.save()
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(1, repo.accounts.value.size)
            assertEquals("Alice", repo.accounts.value[0].label)
        }

    @Test fun handleScannedTotpUri() =
        runTest {
            val vm = makeVm()
            vm.handleScannedUri("otpauth://totp/Example:alice?secret=JBSWY3DPEHPK3PXP&issuer=Example")
            assertEquals("JBSWY3DPEHPK3PXP", vm.form.value.secret)
            assertEquals("Example:alice", vm.form.value.label)
        }
}
