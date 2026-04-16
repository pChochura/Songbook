package com.pointlessapps.songbook.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("ComposableNaming")
@Composable
internal fun <T> Flow<T>.collectWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: FlowCollector<T>,
) {
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            collect(collector)
        }
    }
}

@Suppress("ComposableNaming")
@Composable
internal fun <T> Flow<T>.collectWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
) {
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        lifecycle.repeatOnLifecycle(minActiveState) {
            collect()
        }
    }
}
