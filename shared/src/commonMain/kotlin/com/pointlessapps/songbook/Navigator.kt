package com.pointlessapps.songbook

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.pointlessapps.songbook.core.song.model.Section
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

internal sealed interface Route : NavKey {
    val hasBottomBar: Boolean
        get() = false

    @Serializable
    data object Library : Route {
        override val hasBottomBar = true
    }

    @Serializable
    data class Lyrics(val songId: Long? = null) : Route

    @Serializable
    data object Search : Route {
        override val hasBottomBar = true
    }

    @Serializable
    data class ImportSong(
        val id: Long? = null,
        val title: String? = null,
        val artist: String? = null,
        val lyrics: String? = null,
    ) : Route

    @Serializable
    data class PreviewSong(
        val title: String,
        val artist: String,
        val sections: List<Section>,
    ) : Route
}

internal val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Lyrics::class, Route.Lyrics.serializer())
            subclass(Route.Search::class, Route.Search.serializer())
            subclass(Route.ImportSong::class, Route.ImportSong.serializer())
            subclass(Route.PreviewSong::class, Route.PreviewSong.serializer())
        }
    }
}

private const val DEFAULT_TRANSITION_DURATION_MILLISECOND = 500

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun NavDisplay(backstack: NavBackStack<NavKey>) {
    NavDisplay(
        backStack = backstack,
        entryProvider = koinEntryProvider(),
        transitionSpec = {
            ContentTransform(
                fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
            )
        },
        predictivePopTransitionSpec = {
            ContentTransform(
                fadeIn(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
                fadeOut(animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND)),
            )
        },
    )
}

internal class Navigator(private val backStack: NavBackStack<NavKey>) {

    val currentRoute: Route?
        get() = backStack.lastOrNull() as? Route

    fun bottomNavigationTo(route: Route) {
        when (route) {
            is Route.Library -> {
                backStack.subList(1, backStack.size).removeAll { it is Route.Library }
                backStack.add(route)
            }

            else -> backStack.add(route)
        }
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToLyrics(songId: Long? = null) {
        backStack.add(Route.Lyrics(songId))
    }

    fun navigateToImportSong(
        id: Long? = null,
        title: String? = null,
        artist: String? = null,
        lyrics: String? = null,
    ) {
        backStack.add(Route.ImportSong(id, title, artist, lyrics))
    }

    fun navigateToPreview(title: String, artist: String, sections: List<Section>) {
        backStack.add(Route.PreviewSong(title, artist, sections))
    }
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }
