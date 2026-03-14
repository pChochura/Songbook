package com.pointlessapps.songbook

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

public sealed interface Route : NavKey {
    @Serializable
    data class Library(
        val initialFilterLetter: String? = null,
        val openSearch: Boolean = false,
    ) : Route

    @Serializable
    data class Lyrics(val songId: Long? = null) : Route
}

private val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Library::class, Route.Library.serializer())
            subclass(Route.Lyrics::class, Route.Lyrics.serializer())
        }
    }
}

private const val DEFAULT_TRANSITION_DURATION_MILLISECOND = 500

@OptIn(KoinExperimentalAPI::class)
@Composable
public fun Navigator(
    startingRoute: Route,
    backStack: NavBackStack<NavKey> = rememberNavBackStack(
        configuration = navigationConfig,
        startingRoute,
    ),
) {
    val navigator = Navigator(backStack)
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
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
}

public class Navigator(private val backStack: NavBackStack<NavKey>) {
    fun navigateToLibrary() {
        backStack.add(Route.Library())
    }

    fun navigateToLyrics(songId: Long? = null) {
        backStack.add(Route.Lyrics(songId))
    }
}

public val LocalNavigator: ProvidableCompositionLocal<Navigator> =
    staticCompositionLocalOf { error("LocalNavigator not initialized") }
