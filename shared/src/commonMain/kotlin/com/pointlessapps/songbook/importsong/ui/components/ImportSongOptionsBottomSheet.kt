package com.pointlessapps.songbook.importsong.ui.components

import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.importsong.ImportSongState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.import_menu_add_to_setlists
import com.pointlessapps.songbook.shared.import_menu_preview
import com.pointlessapps.songbook.shared.import_menu_preview_description
import com.pointlessapps.songbook.shared.import_menu_rescan
import com.pointlessapps.songbook.shared.import_menu_rescan_description
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconAddFolder
import com.pointlessapps.songbook.ui.theme.IconScan
import com.pointlessapps.songbook.ui.theme.IconVisibility
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ImportSongOptionsBottomSheet(
    show: Boolean,
    state: ImportSongState,
    onDismissRequest: () -> Unit,
    onAction: (ImportSongOptionsBottomSheetAction) -> Unit,
) {
    OptionsBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
        items = listOf(
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconAddFolder,
                label = Res.string.import_menu_add_to_setlists,
                description = state.selectedSetlists.joinToString { it.name }
                    .takeIf { state.selectedSetlists.isNotEmpty() },
                onClick = { onAction(ImportSongOptionsBottomSheetAction.AddToSetlists) },
            ),
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconScan,
                label = Res.string.import_menu_rescan,
                description = stringResource(Res.string.import_menu_rescan_description),
                onClick = { onAction(ImportSongOptionsBottomSheetAction.Rescan) },
            ),
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconVisibility,
                label = Res.string.import_menu_preview,
                description = stringResource(Res.string.import_menu_preview_description),
                onClick = { onAction(ImportSongOptionsBottomSheetAction.Preview) },
            ),
        ),
    )
}

internal enum class ImportSongOptionsBottomSheetAction {
    AddToSetlists, Rescan, Preview
}
