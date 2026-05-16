package com.pointlessapps.songbook

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.pointlessapps.songbook.core.song.model.Section
import com.pointlessapps.songbook.core.utils.Keep
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@Keep
internal sealed interface Route : NavKey {
    val hasBottomBar: Boolean
        get() = false

    @Keep
    @Serializable
    data object Introduction : Route {
        override val hasBottomBar = false
    }

    @Keep
    @Serializable
    data class Library(val initialFilterLetter: String? = null) : Route {
        override val hasBottomBar = true
    }

    @Keep
    @Serializable
    data object Settings : Route

    @Keep
    @Serializable
    data object Lyrics : Route

    @Keep
    @Serializable
    data object Search : Route {
        override val hasBottomBar = true
    }

    @Keep
    @Serializable
    data class ImportSong(
        val id: String? = null,
        val title: String? = null,
        val artist: String? = null,
        val lyrics: String? = null,
    ) : Route

    @Keep
    @Serializable
    data class PreviewSong(
        val title: String,
        val artist: String,
        val sections: ImmutableList<Section>,
    ) : Route

    @Keep
    @Serializable
    data class Setlist(val id: String) : Route {
        override val hasBottomBar = true
    }
}

internal val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Introduction::class, Route.Introduction.serializer())
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Settings::class, Route.Settings.serializer())
            subclass(Route.Lyrics::class, Route.Lyrics.serializer())
            subclass(Route.Search::class, Route.Search.serializer())
            subclass(Route.ImportSong::class, Route.ImportSong.serializer())
            subclass(Route.PreviewSong::class, Route.PreviewSong.serializer())
            subclass(Route.Setlist::class, Route.Setlist.serializer())
        }
    }
}

private const val DEFAULT_TRANSITION_DURATION_MILLISECOND = 500

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun NavDisplay(navigator: Navigator) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = navigator::navigateBack,
                entryProvider = koinEntryProvider(),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
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
                sharedTransitionScope = this,
            )
        }
    }
}

@Stable
internal class Navigator(val backStack: NavBackStack<NavKey>) {

    val currentRoute: Route?
        get() = backStack.lastOrNull() as? Route

    fun bottomNavigationTo(route: Route) {
        backStack.add(route)
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToIntroduction() {
        backStack.clear()
        backStack.add(Route.Introduction)
    }

    fun navigateToLibrary() {
        backStack.clear()
        backStack.add(Route.Library())
    }

    fun navigateToSettings() {
        backStack.add(Route.Settings)
    }

    fun navigateToLyrics() {
        backStack.add(Route.Lyrics)
    }

    fun navigateToImportSong(
        id: String? = null,
        title: String? = null,
        artist: String? = null,
        lyrics: String? = null,
    ) {
        backStack.add(Route.ImportSong(id, title, artist, lyrics))
    }

    fun navigateToPreview(title: String, artist: String, sections: ImmutableList<Section>) {
        backStack.add(Route.PreviewSong(title, artist, sections))
    }

    fun navigateToSetlist(id: String) {
        backStack.add(Route.Setlist(id))
    }
}

internal val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }

internal val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    staticCompositionLocalOf { error("LocalSharedTransitionScope not initialized") }

@Composable
internal fun LocalSharedTransitionScope(
    content: @Composable SharedTransitionScope.(AnimatedContentScope) -> Unit,
) = with(LocalSharedTransitionScope.current) {
    content(LocalNavAnimatedContentScope.current)
}
