package com.pointlessapps.songbook.library.ui.components

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.core.utils.persistentListOfNotNull
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
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconDisplayMode
import com.pointlessapps.songbook.ui.theme.IconLogin
import com.pointlessapps.songbook.ui.theme.IconLogout
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryOptionsBottomSheet(
    show: Boolean,
    state: LibraryState,
    onDismissRequest: () -> Unit,
    onAction: (LibraryOptionsBottomSheetAction) -> Unit,
) {
    OptionsBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
        items = persistentListOfNotNull(
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
                icon = IconLogin,
                label = Res.string.library_menu_login,
                description = stringResource(Res.string.library_menu_login_description),
                onClick = { onAction(LibraryOptionsBottomSheetAction.Login) },
            ).takeIf { state.loginStatus == LoginStatus.ANONYMOUS },
            OptionsBottomSheetItem.new(
                icon = IconLogout,
                label = Res.string.library_menu_logout,
                description = stringResource(Res.string.library_menu_logout_description),
                onClick = { onAction(LibraryOptionsBottomSheetAction.Logout) },
            ),
        ),
    )
}

internal enum class LibraryOptionsBottomSheetAction {
    DisplayMode, Login, Logout
}
