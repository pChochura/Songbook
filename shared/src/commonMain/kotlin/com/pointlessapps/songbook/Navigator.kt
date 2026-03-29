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
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

internal sealed interface Route : NavKey {
    val hasBottomBar: Boolean
        get() = false

    @Serializable
    data class Library(
        val initialFilterLetter: String? = null,
        val openSearch: Boolean = false,
    ) : Route {
        override val hasBottomBar = true
    }

    @Serializable
    data class Lyrics(val songId: Long? = null) : Route

    @Serializable
    data object Search : Route

    @Serializable
    data object ImportSong : Route
}

internal val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Lyrics::class, Route.Lyrics.serializer())
            subclass(Route.Search::class, Route.Search.serializer())
            subclass(Route.ImportSong::class, Route.ImportSong.serializer())
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

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToLibrary() {
        backStack.add(Route.Library())
    }

    fun navigateToLyrics(songId: Long? = null) {
        backStack.add(Route.Lyrics(songId))
    }

    fun navigateToImportSong() {
        backStack.add(Route.ImportSong)
    }
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }
