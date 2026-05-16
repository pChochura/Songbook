package com.pointlessapps.songbook.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.core.auth.model.LoginStatus
import com.pointlessapps.songbook.library.ui.dialogs.LogoutDialog
import com.pointlessapps.songbook.settings.SettingsEvent.NavigateToIntroduction
import com.pointlessapps.songbook.settings.SettingsViewModel
import com.pointlessapps.songbook.shared.ui.Res
import com.pointlessapps.songbook.shared.ui.common_remove_account
import com.pointlessapps.songbook.shared.ui.common_remove_account_description
import com.pointlessapps.songbook.shared.ui.common_settings
import com.pointlessapps.songbook.shared.ui.settings_login
import com.pointlessapps.songbook.shared.ui.settings_login_description
import com.pointlessapps.songbook.shared.ui.settings_logout
import com.pointlessapps.songbook.shared.ui.settings_logout_description
import com.pointlessapps.songbook.shared.ui.settings_remove_account
import com.pointlessapps.songbook.shared.ui.settings_remove_account_description
import com.pointlessapps.songbook.ui.OptionsBottomSheetDivider
import com.pointlessapps.songbook.ui.OptionsBottomSheetItem
import com.pointlessapps.songbook.ui.OptionsBottomSheetItemButton
import com.pointlessapps.songbook.ui.TopBar
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.dialogs.ConfirmationDialog
import com.pointlessapps.songbook.ui.theme.IconDeletePermanently
import com.pointlessapps.songbook.ui.theme.IconLogin
import com.pointlessapps.songbook.ui.theme.IconLogout
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.SyncingTopBarButton
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRemoveAccountDialogVisible by rememberSaveable { mutableStateOf(false) }

    viewModel.events.collectWithLifecycle {
        when (it) {
            is NavigateToIntroduction -> navigator.navigateToIntroduction()
        }
    }

    SongbookScaffoldLayout(
        topBar = @Composable {
            TopBar(
                leftButton = @Composable { SyncingTopBarButton(state.syncStatus) },
                rightButton = null,
                title = stringResource(Res.string.common_settings),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(all = MaterialTheme.spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        ) {
            item { Spacer(Modifier.padding(paddingValues)) }

            if (state.loginStatus == LoginStatus.ANONYMOUS) {
                item(key = "item_login") {
                    OptionsBottomSheetItemButton(
                        OptionsBottomSheetItem.new(
                            icon = IconLogin,
                            label = Res.string.settings_login,
                            description = stringResource(Res.string.settings_login_description),
                            onClick = viewModel::onLoginClicked,
                        ),
                    )
                }
            }

            item(key = "item_logout") {
                OptionsBottomSheetItemButton(
                    OptionsBottomSheetItem.new(
                        icon = IconLogout,
                        label = Res.string.settings_logout,
                        description = stringResource(Res.string.settings_logout_description),
                        onClick = { isLogoutDialogVisible = true },
                    ),
                )
            }

            item { OptionsBottomSheetDivider() }

            item(key = "item_remove_account") {
                OptionsBottomSheetItemButton(
                    OptionsBottomSheetItem.new(
                        color = MaterialTheme.colorScheme.error,
                        icon = IconDeletePermanently,
                        label = Res.string.settings_remove_account,
                        description = stringResource(Res.string.settings_remove_account_description),
                        onClick = { isRemoveAccountDialogVisible = true },
                    ),
                )
            }

            item { Spacer(Modifier.navigationBarsPadding()) }
        }
    }

    if (isLogoutDialogVisible) {
        LogoutDialog(
            loginStatus = state.loginStatus,
            onConfirmClicked = {
                viewModel.onLogoutClicked()
                isLogoutDialogVisible = false
            },
            onDismissRequest = { isLogoutDialogVisible = false },
        )
    }

    if (isRemoveAccountDialogVisible) {
        ConfirmationDialog(
            title = Res.string.common_remove_account,
            description = Res.string.common_remove_account_description,
            onConfirmClicked = {
                viewModel.onRemoveAccountClicked()
                isRemoveAccountDialogVisible = false
            },
            onDismissRequest = { isRemoveAccountDialogVisible = false },
        )
    }
}
