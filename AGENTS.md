# Songbook Mobile - Agents Configuration

## Code Style & Patterns

### Composables

Always prefer using the horizontalArrangement, or verticalArrangement in Row and Column composables
instead of Spacer. When the Row or Column might not fit on the screen, prefer using
FlowRow or FlowColumn instead. Make sure the design is responsive and prepared for multitude of
screen resolutions.

Whenever there is a string used in the composition, prefer referencing it via stringResource
instead of hardcoding it.

### Screen Conventions

Every functionality is split by screens (Lyrics, Library, Settings and so on).
The navigation between them is routed via the Route class and using the Navigator class with the 
koin being the middle-man:
```kotlin
internal val lyricsModule = module {
    viewModelOf(::LyricsViewModel)

    navigation<Route.Lyrics> {
        LyricsScreen(
            viewModel = koinViewModel(),
        )
    }
}
```

### Naming Conventions

- **Classes**: PascalCase (`SongControlBar`, `LyricsLine`)
- **Packages**: lowercase, screen-based (`library`, `lyrics`, `settings`)
- **Suffixes**: `*ViewModel`, `*Repository`, `*Module` (DI)
- **Composables**: PascalCase function names

### Project structure

Each package (screen-based) follows this architecture:

```
package/
├── ui/
│   └── components/           # Module-specific UI
├── domain/
│   ├── models/               # Domain models
│   └── repository/           # Repository interfaces (and implementations in a form of *RepositoryImpl)
└── di/Module.kt              # Koin DI registration
```

### ViewModel Pattern

```kotlin
internal sealed interface TempEvent {
    data class NavigateTo(val route: Route) : TempEvent
    data class ShowSnackbar(@StringRes val message: Int) : TempEvent
}

internal data class TempState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
)

internal class ViewModelTemp : ViewModel() {

    var state by mutableStateOf(TempState())
        private set

    private val eventChannel = Channel<TempEvent>()
    val events = eventChannel.receiveAsFlow()

    fun temp() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // some action here

            state = state.copy(isLoading = false)
        }
    }
}
```

### DRY (Don't Repeat Yourself)
- Extract repeated logic into shared functions, composables
- Reuse existing UI components from `ui/components/` before creating new ones
- Share domain models and mappers across modules via `core/`
