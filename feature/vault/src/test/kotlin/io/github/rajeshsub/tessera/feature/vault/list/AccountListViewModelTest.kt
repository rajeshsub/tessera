package io.github.rajeshsub.tessera.feature.vault.list

import io.github.rajeshsub.tessera.core.model.Account
import io.github.rajeshsub.tessera.core.model.OtpType
import io.github.rajeshsub.tessera.feature.vault.fake.FakePrefsRepository
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
class AccountListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeVaultRepository
    private lateinit var prefs: FakePrefsRepository

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = FakeVaultRepository()
        prefs = FakePrefsRepository()
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm() =
        AccountListViewModel(
            vaultRepository = repo,
            prefsRepository = prefs,
        )

    @Test fun itemsEmptyOnInit() =
        runTest {
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertTrue(vm.items.value.isEmpty())
        }

    @Test fun itemsReflectRepositoryAccounts() =
        runTest {
            repo.seed(listOf(makeTotpAccount("a1")))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertEquals(1, vm.items.value.size)
            assertEquals(
                "a1",
                vm.items.value[0]
                    .account.id,
            )
        }

    @Test fun toggleRevealChangesIsRevealed() =
        runTest {
            repo.seed(listOf(makeTotpAccount("a1")))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertFalse(vm.items.value[0].isRevealed)
            vm.toggleReveal("a1")
            assertTrue(vm.items.value[0].isRevealed)
        }

    @Test fun deleteAccountRemovesFromRepo() =
        runTest {
            repo.seed(listOf(makeTotpAccount("a1"), makeTotpAccount("a2")))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            vm.deleteAccount("a1")
            dispatcher.scheduler.runCurrent()
            assertEquals(1, repo.accounts.value.size)
            assertEquals("a2", repo.accounts.value[0].id)
        }

    @Test fun lockCallsRepoLock() =
        runTest {
            repo.seed(listOf(makeTotpAccount("a1")))
            repo.unlock(charArrayOf('p'))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertTrue(repo.isUnlocked.value)
            vm.lock()
            assertFalse(repo.isUnlocked.value)
        }

    @Test fun totpItemHasCurrentCode() =
        runTest {
            repo.seed(listOf(makeTotpAccount("a1")))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertTrue(
                vm.items.value[0]
                    .currentCode
                    .isNotEmpty(),
            )
            assertEquals(
                6,
                vm.items.value[0]
                    .currentCode.length,
            )
        }

    @Test fun hotpItemHasEmptyCodeInitially() =
        runTest {
            repo.seed(listOf(makeHotpAccount("h1")))
            val vm = makeVm()
            dispatcher.scheduler.runCurrent()
            assertEquals("", vm.items.value[0].currentCode)
        }

    private fun makeTotpAccount(id: String) =
        Account(
            id = id,
            type = OtpType.TOTP,
            label = "label-$id",
            issuer = "Issuer",
            secret = ByteArray(20) { it.toByte() },
        )

    private fun makeHotpAccount(id: String) =
        Account(
            id = id,
            type = OtpType.HOTP,
            label = "hotp-$id",
            issuer = null,
            secret = ByteArray(20) { it.toByte() },
            counter = 0L,
        )
}
