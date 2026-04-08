package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.setlist_menu_delete
import com.pointlessapps.songbook.shared.setlist_menu_delete_description
import com.pointlessapps.songbook.shared.setlist_menu_edit
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconDelete
import com.pointlessapps.songbook.ui.theme.IconEdit
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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
        items = listOf(
            OptionsBottomSheetItem.Divider,
            OptionsBottomSheetItem.new(
                icon = IconEdit,
                label = Res.string.setlist_menu_edit,
                onClick = { onAction(SetlistOptionsBottomSheetAction.Edit) },
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
    Edit, Delete,
}
