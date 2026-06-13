package io.github.rajeshsub.tessera

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.rajeshsub.tessera.data.vault.VaultRepository
import io.github.rajeshsub.tessera.feature.vault.TesseraNavGraph
import io.github.rajeshsub.tessera.feature.vault.theme.TesseraTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var vaultRepository: VaultRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        setContent {
            TesseraTheme {
                TesseraNavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vaultRepository.lock()
    }
}
