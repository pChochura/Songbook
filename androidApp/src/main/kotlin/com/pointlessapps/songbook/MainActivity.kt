package com.pointlessapps.songbook

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pointlessapps.songbook.core.app.AndroidAppViewModel
import com.pointlessapps.songbook.widget.SongbookWidget.Companion.EXTRA_FILTER_LETTER
import com.pointlessapps.songbook.widget.SongbookWidget.Companion.EXTRA_OPEN_SEARCH
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AndroidAppViewModel by viewModel()

    private var filterLetter by mutableStateOf<String?>(null)
    private var openSearch by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            appViewModel.state.isInitializing
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            if (!appViewModel.state.isInitializing) {
                App(
                    initialFilterLetter = filterLetter,
                    openSearch = openSearch,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the activity intent so that any future access returns the latest one
        setIntent(intent)
        // Update state to trigger recomposition in App
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        filterLetter = intent.getStringExtra(EXTRA_FILTER_LETTER)
        openSearch = intent.getBooleanExtra(EXTRA_OPEN_SEARCH, false)
    }
}
