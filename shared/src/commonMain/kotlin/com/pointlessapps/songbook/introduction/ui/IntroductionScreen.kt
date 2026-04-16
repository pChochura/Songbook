package com.pointlessapps.songbook.introduction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessapps.songbook.LocalNavigator
import com.pointlessapps.songbook.introduction.IntroductionEvent.NavigateToLibrary
import com.pointlessapps.songbook.introduction.IntroductionViewModel
import com.pointlessapps.songbook.shared.Res
import com.pointlessapps.songbook.shared.common_app_name
import com.pointlessapps.songbook.shared.common_or
import com.pointlessapps.songbook.shared.introduction_continue_as_guest
import com.pointlessapps.songbook.shared.introduction_description
import com.pointlessapps.songbook.shared.introduction_sign_in_with_google
import com.pointlessapps.songbook.shared.introduction_title
import com.pointlessapps.songbook.ui.components.SongbookButton
import com.pointlessapps.songbook.ui.components.SongbookIcon
import com.pointlessapps.songbook.ui.components.SongbookLoader
import com.pointlessapps.songbook.ui.components.SongbookScaffoldLayout
import com.pointlessapps.songbook.ui.components.SongbookText
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookButtonTextStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookIconStyle
import com.pointlessapps.songbook.ui.components.defaultSongbookTextStyle
import com.pointlessapps.songbook.ui.theme.DEFAULT_BORDER_WIDTH
import com.pointlessapps.songbook.ui.theme.IconNote
import com.pointlessapps.songbook.ui.theme.IconPerson
import com.pointlessapps.songbook.ui.theme.spacing
import com.pointlessapps.songbook.utils.collectWithLifecycle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun IntroductionScreen(
    viewModel: IntroductionViewModel,
) {
    val navigator = LocalNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.events.collectWithLifecycle {
        when (it) {
            NavigateToLibrary -> navigator.navigateToLibrary()
        }
    }

    SongbookScaffoldLayout {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.extraLarge)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = MaterialTheme.spacing.medium,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            SongbookIcon(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(MaterialTheme.spacing.medium)
                    .size(ICON_SIZE),
                icon = IconNote,
                iconStyle = defaultSongbookIconStyle().copy(
                    tint = MaterialTheme.colorScheme.onPrimary,
                ),
            )
            SongbookText(
                text = stringResource(Res.string.common_app_name),
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.primary,
                    typography = MaterialTheme.typography.displayMedium,
                ),
            )

            Spacer(Modifier.height(MaterialTheme.spacing.extraLarge * 2))

            SongbookText(
                text = stringResource(Res.string.introduction_title),
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    typography = MaterialTheme.typography.titleLarge,
                ),
            )
            SongbookText(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraLarge),
                text = stringResource(Res.string.introduction_description),
                textStyle = defaultSongbookTextStyle().copy(
                    textAlign = TextAlign.Center,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    typography = MaterialTheme.typography.bodyLarge,
                ),
            )

            Spacer(Modifier.height(MaterialTheme.spacing.extraLarge * 2))

            SongbookButton(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.introduction_continue_as_guest),
                onClick = viewModel::onContinueAsGuestClicked,
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    textStyle = defaultSongbookButtonTextStyle().copy(
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                    icon = IconPerson,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.extraLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = MaterialTheme.spacing.medium,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.3f,
                    ),
                )
                SongbookText(
                    text = stringResource(Res.string.common_or).lowercase(),
                    textStyle = defaultSongbookTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.3f,
                    ),
                )
            }

            SongbookButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = DEFAULT_BORDER_WIDTH,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                    ),
                label = stringResource(Res.string.introduction_sign_in_with_google),
                onClick = viewModel::onSignInWithGoogleClicked,
                buttonStyle = defaultSongbookButtonStyle().copy(
                    containerColor = Color.Transparent,
                    textStyle = defaultSongbookButtonTextStyle().copy(
                        textAlign = TextAlign.Center,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        typography = MaterialTheme.typography.titleMedium,
                    ),
                ),
            )
        }
    }

    SongbookLoader(state.isLoading)
}

private val ICON_SIZE = 64.dp
