package com.pointlessapps.songbook.setlist.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.pointlessapps.songbook.setlist.SetlistState
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_menu
import com.pointlessapps.songbook.shared.import_menu_rescan
import com.pointlessapps.songbook.shared.import_menu_rescan_description
import com.pointlessapps.songbook.ui.OptionsBottomSheet
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetTitleHeader
import com.pointlessapps.songbook.ui.theme.IconScan
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
                icon = IconScan,
                label = Res.string.import_menu_rescan,
                description = stringResource(Res.string.import_menu_rescan_description),
                onClick = { },
            ),
        ),
    )
}

internal enum class SetlistOptionsBottomSheetAction {

}
