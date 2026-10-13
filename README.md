# Coolblue product search

[![CI](https://github.com/Kmozcan1/coolblue-android-assignment/actions/workflows/ci.yml/badge.svg)](https://github.com/Kmozcan1/coolblue-android-assignment/actions/workflows/ci.yml)

An Android app for the Coolblue mobile assignment: type a query, scroll through the results.
Kotlin, Jetpack Compose, one screen.

<img src="docs/screenshot.png" alt="Search results for apple" width="300" />

- **The mock API ignores the search term.** The app shows what it returns; a version that searches
  in the app is on the [`in-app-search`](https://github.com/Kmozcan1/coolblue-android-assignment/tree/in-app-search) branch.
- **Only the newest request counts.** A slow, stale response never overwrites newer results.
- **Clean Architecture, MVVM, Hilt and Retrofit**, in nine modules to show how the project would grow.
- **35 unit tests**, written test-first. GitHub Actions runs them on every push.
- **Pair-programmed with Claude Code**, every design decision discussed first and every line reviewed.

From [Architecture](#architecture) down is optional detail. Engineers: start at
[One request pipeline](#one-request-pipeline), then [Left out on purpose](#left-out-on-purpose).

## Run it

Open the project in Android Studio (latest stable) and run `app`, or from the command line:

```bash
./gradlew assembleDebug test
```

Requires Android 8.0 (API 26) or newer on the device and JDK 17 or newer to build. The app talks
to the assignment's mock API; its URL is a constant in `core/network`.

## What it does

- Search as you type, debounced by 300 ms. A blank field shows a prompt instead of a list.
- The query goes to the API with every page request and the list shows what comes back. The
  assignment's API is a mock that returns the same catalogue for every search term, so every
  query lists the same products. The [`in-app-search`](https://github.com/Kmozcan1/coolblue-android-assignment/tree/in-app-search) branch searches in the app
  instead: "ipad" shows the iPads and the Apple Pencil, "samsung" shows "No products found".
- Results come in pages of 24. Scrolling near the end loads the next page until the last one.
- Each row shows the product image, name, review score and count, up to three USPs, a promo
  label, the price (with the old price struck through when it is higher than the current one)
  and the next-day delivery promise.
- Errors are shown in place: a message with Retry under the field when the first page fails, a
  footer row with Retry when a later page fails, so the rows already on screen stay.
- Tapping a row opens the product in a bottom sheet: the full name, every USP, a larger photo.
- The query survives rotation and process death. After a restore the search runs again by itself.

## Architecture

Multi-module Clean Architecture with MVVM and unidirectional data flow. One feature, `search`,
split into three layers, plus a few shared modules.

```
app             Application (also Coil's image loader), MainActivity, the theme wrapped around the one screen
build-logic     Gradle convention plugins, so each module's build file is about ten lines
core/domain     AppResult and AppError, the result contract every repository returns
core/network    OkHttp, kotlinx-serialization and Retrofit, provided through Hilt
core/ui         Material 3 theme in Coolblue colours
core/test       MainDispatcherRule, shared by ViewModel tests
search/domain   Product, ProductPage, SearchRepository, SearchProductsUseCase
search/data     Retrofit API, DTOs, DTO-to-domain mapper, SearchRepositoryImpl, Hilt bindings
search/ui       SearchViewModel, UiState, UiAction, composables, UI model and its mapper
```

Dependencies point inwards: `search/ui` and `search/data` both depend on `search/domain`,
never on each other, and `search/domain` depends only on `core/domain`. Both are plain Kotlin
modules with no Android dependency. `app` is the only module that sees everything, which is
what lets Hilt build the object graph.

### Data flow

```
SearchScreen ─onAction─▶ SearchViewModel ─▶ SearchProductsUseCase ─▶ SearchRepository (interface)
                                                                          │
                                                        SearchRepositoryImpl ─▶ SearchApi ─▶ HTTP
```

- The screen is stateless. It receives one `SearchUiState` and sends `SearchUiAction`s back.
  Nothing else crosses that boundary, so the screen can be previewed in every state without a
  ViewModel.
- `SearchUiState` holds the query and a sealed `SearchContent`: `Idle`, `Loading`, `Empty`,
  `Error` or `Loaded`. Exactly one is active, so there is no combination of booleans that can
  disagree with each other.
- Tapping a row opens the same product in a bottom sheet, nothing clipped, every USP. Which
  sheet is open is screen-local `rememberSaveable` state, not part of `SearchUiState`: it is
  transient chrome over a product the `Loaded` content already holds. A query change closes it,
  and so does a restore after process death, since the search starts over.
- `Loading` only shows when there are no rows to keep: a cold start, a restore after process
  death, Retry after an error, or a refinement from an empty result. Refining a query that has
  rows keeps them on screen with `Loaded.isRefreshing` set until the new rows replace them at
  the top; `LoadMore` is ignored meanwhile, since page 2 would belong to the new query.
- The repository returns `AppResult`: `Success` with data or `Failure` with an `AppError`.
  Exceptions stop at the data layer; the ViewModel only ever sees these two cases.

### One request pipeline

Every request the ViewModel makes goes through a single flow:

```kotlin
merge(query.debounce(300).map { 1 }, pageRequests)
    .collectLatest { page -> load(query.value, page) }
```

The debounced query asks for page 1. `Retry` and `LoadMore` push a page number into
`pageRequests`. `collectLatest` cancels the previous load whenever a new request arrives, so at
most one request is ever in flight for the screen and a slow, stale response can never overwrite
a newer state. There are no job fields to cancel by hand.

## Libraries and why

| Library | Why |
|---|---|
| Retrofit 3 + OkHttp 5 | One `@GET` with two `@Query` parameters covers the whole API. The most widely known networking stack on Android. |
| kotlinx-serialization | JSON parsing with compiler-generated serializers, no reflection. Retrofit's own converter plugs it in. |
| Hilt | Dependency injection with compile-time checking. ViewModels get their use case, the repository gets its API, nothing is constructed by hand. |
| Coil 3 | Image loading for Compose. Shares the OkHttp client with Retrofit. |
| Compose + Material 3 | The UI toolkit the assignment asks for. |
| JUnit 4, MockK, Truth, Turbine | Unit tests, mocking, readable assertions, Flow testing. |
| MockWebServer | Tests the repository against real HTTP responses, including the actual JSON the API returns. |

## Decisions and trade-offs

**Hand-rolled paging instead of Paging 3.** The API has three pages of 24. Paging 3 would
replace the single `UiState` with `PagingData` plus `LoadState` plumbing, making both the code
and the tests harder to follow for no visible gain. The repository returns `ProductPage` with
`page` and `pageCount`, which is exactly what a `PagingSource.load` would return, so Paging 3
can be introduced at the data layer if the catalogue ever grows. The last page is derived from
`pageCount` on purpose: the API repeats the last page for any page number beyond it, so an
empty response never comes.

**Main shows what the API returns; in-app search is a branch.** The API ignores the search
term, and the PDF says so. Whether the app should search the catalogue itself is a product
call, so main sends the query and shows the response, as it would against a real search
backend. The `in-app-search` branch is [one commit on top of main](https://github.com/Kmozcan1/coolblue-android-assignment/compare/main...in-app-search): `SearchProductsUseCase`
filters each page by name and USPs and skips pages that filter down to nothing. That commit
touches only `search/domain` and this README; the ViewModel, the repository and the UI stay
untouched. That is why main keeps the use case even though it is a single repository call.

**Product images are cropped to a 1:2 tile.** Every image the API links to is 300×750 on white
with the product centred and filling the width; measured across phones, tablets, a watch, a
keyboard and a cable, every product sits inside the middle 600 px. So the row shows the image
with `ContentScale.Crop` in an 84×168 dp tile: the empty top and bottom tenths go, every product
stays whole (the Pencil loses its ends), and no grey box with a white strip inside it.

**Prices as integer cents.** The API sends euros as a decimal number. The domain stores
`salesPriceCents: Int` because floating-point money is a known source of bugs. Formatting
happens once, in the UI mapper, with the Dutch locale (`€ 369,00`).

**A sealed `AppError` with three cases.** `Network`, `Server(code)` and `Unknown` are the only
distinctions the UI acts on. The error never carries the exception, so states stay comparable
in tests. `Network` and `Server` come from `IOException` and `HttpException`; anything else the
stack throws, including a body that will not parse, becomes `Unknown` instead of a crash.
`CancellationException` is rethrown so coroutine cancellation keeps working.

**No dispatcher switch in the repository.** Retrofit's suspend functions already run on OkHttp's
own threads, so `withContext(Dispatchers.IO)` would add nothing.

**Query in `SavedStateHandle`.** The ViewModel survives rotation but not process death. Keeping
the query in `SavedStateHandle` costs two lines, and because the request pipeline observes
that flow, the search runs again on its own after a restore.

**Classic `TextField(value, onValueChange)`.** The query is updated synchronously in
`onAction`, so the field never lags behind the keyboard. The newer `TextFieldState` API was
considered and left out: it would move Compose state into the ViewModel and complicate the
tests.

**Base URL as a constant.** There is one environment. With a second one it becomes a
`buildConfigField` per flavour.

**Nine modules for one screen.** Heavier than the screen needs, but it shows how the project
would grow: a second feature is three more modules and three lines in `settings.gradle.kts`,
and its repository returns the same `AppResult` from `core/domain` without depending on
`search`. The convention plugins in `build-logic` keep each module's build file small. Hilt is
its own plugin, applied only by the modules that have bindings or injection points, so
`core/ui` carries no DI.

## Tests

| Module | Test | Covers |
|---|---|---|
| search/domain | `ProductPageTest` | next page from page count |
| search/data | `ProductMapperTest` | DTO to domain, euros to cents, optional fields |
| search/data | `SearchRepositoryImplTest` | MockWebServer: the real page 1 JSON (every mapped field), query parameters, 500, no connection, bad JSON, an unexpected exception, cancellation passing through |
| search/ui | `ProductUiMapperTest` | Dutch price and rating formatting, list price rule |
| search/ui | `SearchViewModelTest` | debounce, first page, empty, error and retry, load more, duplicate guard, load-more error and retry, cancellation on query change, saved query and restore, clear to idle, refine keeps the rows, a late result after clearing is dropped, retry while refreshing, edit-and-undo keeps the rows, empty keeps its query |

Run them with `./gradlew test`. They run on every push through GitHub Actions, together with
Android lint.

Every logic unit was written test-first: the failing test, then the smallest implementation
that makes it pass. Each commit carries its tests with it, and test names read as behaviour
(`given a load more in flight, when the query changes, then it is cancelled and the new query
loads`). Composables are kept stateless so the ViewModel tests cover the screen's logic;
there are no Compose UI tests.

## Built with

Claude Code as a pair programmer. Every design decision above was discussed first, every line
was reviewed, and the commit history reads as the build sequence.

## Left out on purpose

- A product detail screen: outside the user story. The bottom sheet shows what the search
  result already holds; there is no second request and no navigation.
- Paging 3: see above.
- A navigation library: one screen.
- ktlint or detekt: valuable on a team, noise in an assignment of this size.
- Compose UI tests: the screen is stateless and its logic lives in the tested ViewModel.
- Release build configuration: nothing here ships.
- An offline cache: search-as-you-type results are live by nature, and the API is a mock.
  A load failure shows in place with Retry; the rows already on screen stay.
- Crash reporting: nothing ships, so there is no backend to report to. The repository logs
  what it turns into `Unknown`, which is the only failure that carries no other clue.
