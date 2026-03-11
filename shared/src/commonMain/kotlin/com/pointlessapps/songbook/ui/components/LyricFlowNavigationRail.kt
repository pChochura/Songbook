package com.pointlessapps.songbook.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.pointlessapps.songbook.ui.theme.spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.pointlessapps.songbook.shared.generated.resources.Res
import com.pointlessapps.songbook.shared.generated.resources.*

enum class NavigationDestination(val labelRes: StringResource, val icon: ImageVector) {
    Library(Res.string.lyric_flow_navigation_rail_library, Icons.Default.LibraryMusic),
    NowPlaying(Res.string.lyric_flow_navigation_rail_now_playing, Icons.Default.PlayArrow),
    Controls(Res.string.lyric_flow_navigation_rail_controls, Icons.Default.Settings)
}

@Composable
fun LyricFlowNavigationRail(
    selectedDestination: NavigationDestination,
    onDestinationSelected: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        header = {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(MaterialTheme.spacing.huge)
                    .padding(vertical = MaterialTheme.spacing.large)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationDestination.entries.forEach { destination ->
                NavigationRailItem(
                    selected = selectedDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                    label = { Text(stringResource(destination.labelRes), style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.outline,
                        unselectedTextColor = MaterialTheme.colorScheme.outline,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
