package io.github.rajeshsub.tessera.wear

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText

/**
 * Phase 0 placeholder Wear host. FLAG_SECURE matches the phone security posture.
 * Replaced in Phase 5 by the tethered code list that streams phone-computed codes
 * over MessageClient (no secrets stored on the watch).
 */
class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        setContent { WearRoot() }
    }
}

@Composable
private fun WearRoot(modifier: Modifier = Modifier) {
    MaterialTheme {
        Scaffold(modifier = modifier.fillMaxSize(), timeText = { TimeText() }) {
            Text(text = "Tessera")
        }
    }
}
