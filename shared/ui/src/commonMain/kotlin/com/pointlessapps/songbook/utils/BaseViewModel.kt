package com.pointlessapps.songbook.utils

import androidx.lifecycle.ViewModel
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.error_unknown_error
import com.pointlessapps.songbook.ui.theme.IconWarning
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class BaseViewModel(
    snackbarState: SongbookSnackbarState,
    dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel(
    CoroutineScope(
        dispatcher +
                SupervisorJob() +
                CoroutineExceptionHandler { _, throwable ->
                    throwable.printStackTrace()
                    snackbarState.showSnackbar(
                        message = Res.string.error_unknown_error,
                        icon = IconWarning,
                    )
                },
    ),
)
