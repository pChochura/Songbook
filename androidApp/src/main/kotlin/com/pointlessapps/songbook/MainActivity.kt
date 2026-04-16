package com.pointlessapps.songbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.core.app.AndroidAppViewModel
import com.pointlessapps.songbook.widget.EXTRA_FILTER_LETTER
import com.pointlessapps.songbook.widget.EXTRA_OPEN_SEARCH
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AndroidAppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appViewModel.isInitializing.value
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val filterLetter = intent.getStringExtra(EXTRA_FILTER_LETTER)
        val openSearch = intent.getBooleanExtra(EXTRA_OPEN_SEARCH, false)

        setContent {
            val isInitializing by appViewModel.isInitializing.collectAsStateWithLifecycle()
            if (!isInitializing) {
                App(
                    initialFilterLetter = filterLetter,
                    openSearch = openSearch,
                )
            }
        }
    }
}
