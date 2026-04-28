package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_menu
import com.pointlessapps.songbook.shared.ui.setlist_menu_delete
import com.pointlessapps.songbook.shared.ui.setlist_menu_delete_description
import com.pointlessapps.songbook.shared.ui.setlist_menu_rename
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconEdit
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetlistOptionsBottomSheet(
    show: Boolean,
    state: SetlistState,
    onDismissRequest: () -> Unit,
    onAction: (SetlistOptionsBottomSheetAction) -> Unit,
) {
    OptionsBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        header = { OptionsBottomSheetTitleHeader(stringResource(Res.string.common_menu)) },
        items = persistentListOf(
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconEdit,
                label = Res.string.setlist_menu_rename,
                onClick = { onAction(SetlistOptionsBottomSheetAction.Rename) },
            ),
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconDelete,
                color = MaterialTheme.colorScheme.error,
                label = Res.string.setlist_menu_delete,
                description = stringResource(Res.string.setlist_menu_delete_description),
                onClick = { onAction(SetlistOptionsBottomSheetAction.Delete) },
            ),
        ),
    )
}

internal enum class SetlistOptionsBottomSheetAction {
    Rename, Delete,
}
