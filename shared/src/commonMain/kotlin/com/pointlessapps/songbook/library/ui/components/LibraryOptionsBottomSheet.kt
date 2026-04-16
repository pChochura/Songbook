package com.pointlessapps.songbook.library.ui.components

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.LibraryState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.library_menu_display_mode
import com.pointlessapps.songbook.shared.library_menu_display_mode_grid
import com.pointlessapps.songbook.shared.library_menu_display_mode_list
import com.pointlessapps.songbook.shared.library_menu_login
import com.pointlessapps.songbook.shared.library_menu_login_description
import com.pointlessapps.songbook.shared.library_menu_logout
import com.pointlessapps.songbook.shared.library_menu_logout_description
import com.pointlessapps.songbook.shared.library_menu_sync
import com.pointlessapps.songbook.shared.library_menu_sync_description
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconDisplayMode
import com.pointlessapps.songbook.ui.theme.IconLogin
import com.pointlessapps.songbook.ui.theme.IconLogout
import com.pointlessapps.songbook.ui.theme.IconSync
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryOptionsBottomSheet(
    show: Boolean,
    state: LibraryState,
    onDismissRequest: () -> Unit,
    onAction: (LibraryOptionsBottomSheetAction) -> Unit,
) {
    val isLoggedIn = state.loginStatus == LoginStatus.LOGGED_IN

    OptionsBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
        items = listOf(
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = if (isLoggedIn) IconLogout else IconLogin,
                label = if (isLoggedIn) {
                    Res.string.library_menu_logout
                } else {
                    Res.string.library_menu_login
                },
                description = stringResource(
                    if (isLoggedIn) {
                        Res.string.library_menu_logout_description
                    } else {
                        Res.string.library_menu_login_description
                    },
                ),
                onClick = { onAction(LibraryOptionsBottomSheetAction.ToggleLogin) },
            ),
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconDisplayMode,
                label = Res.string.library_menu_display_mode,
                description = when (state.displayMode) {
                    DisplayMode.List -> stringResource(Res.string.library_menu_display_mode_list)
                    DisplayMode.Grid -> stringResource(Res.string.library_menu_display_mode_grid)
                },
                onClick = { onAction(LibraryOptionsBottomSheetAction.DisplayMode) },
            ),
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconSync,
                label = Res.string.library_menu_sync,
                description = stringResource(Res.string.library_menu_sync_description),
                onClick = { onAction(LibraryOptionsBottomSheetAction.Sync) },
            ),
        ),
    )
}

internal enum class LibraryOptionsBottomSheetAction {
    ToggleLogin, DisplayMode, Sync
}
