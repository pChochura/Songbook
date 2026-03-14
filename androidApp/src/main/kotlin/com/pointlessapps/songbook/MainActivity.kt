package com.pointlessapps.songbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pointlessapps.songbook.widget.EXTRA_FILTER_LETTER
import com.pointlessapps.songbook.widget.EXTRA_OPEN_SEARCH

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val filterLetter = intent.getStringExtra(EXTRA_FILTER_LETTER)
        val openSearch = intent.getBooleanExtra(EXTRA_OPEN_SEARCH, false)

        setContent {
            App(
                initialFilterLetter = filterLetter,
                openSearch = openSearch,
            )
        }
    }
}
