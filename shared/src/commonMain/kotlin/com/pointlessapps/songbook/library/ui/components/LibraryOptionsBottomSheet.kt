package com.pointlessapps.songbook.library.ui.components

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.core.utils.persistentListOfNotNull
import com.pointlessapps.songbook.library.DisplayMode
import com.pointlessapps.songbook.library.LibraryState
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_menu
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_grid
import com.pointlessapps.songbook.shared.ui.library_menu_display_mode_list
import com.pointlessapps.songbook.shared.ui.library_menu_settings
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconDisplayMode
import com.pointlessapps.songbook.ui.theme.IconSettings
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
        headerContent = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
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
                icon = IconSettings,
                label = Res.string.library_menu_settings,
                onClick = { onAction(LibraryOptionsBottomSheetAction.Settings) },
            ),
        ),
    )
}

internal enum class LibraryOptionsBottomSheetAction {
    DisplayMode, Settings
}
