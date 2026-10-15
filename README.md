Disclaimer: didn't use git-flow or pull requests for the sake of being faster.

# Coolblue product search

[![CI](https://github.com/Kmozcan1/coolblue-android-assignment/actions/workflows/ci.yml/badge.svg)](https://github.com/Kmozcan1/coolblue-android-assignment/actions/workflows/ci.yml)

An Android app for the Coolblue mobile assignment: type a query, scroll through the results.
Kotlin, Jetpack Compose, one screen.

<img src="docs/screenshot.png" alt="Search results for apple" width="300" />

- **The mock API ignores the search term.** The app shows what it returns; a version that searches
  in the app is on the [`in-app-search`](https://github.com/Kmozcan1/coolblue-android-assignment/tree/in-app-search) branch.
- **Only the newest request counts.** A slow, stale response never overwrites newer results.
- **Clean Architecture, MVVM, Hilt and Retrofit**, in nine modules to show how the project would grow.
- **60 unit tests**, written test-first, **and 6 Compose UI tests**. GitHub Actions runs them on every push.
- **Pair-programmed with Claude Code**, every design decision discussed first and every line reviewed.

From [Architecture](#architecture) down is optional detail. Engineers: start with its
diagram, then [Trade-offs](#trade-offs).

## Run it

Open the project in Android Studio (latest stable) and run `app`, or from the command line:

```bash
./gradlew assembleDebug test
```

Requires Android 8.0 (API 26) or newer on the device and JDK 21 or newer to build. The app talks
to the assignment's mock API; its URL is a constant in `core/network`.

## What it does

- Opens on the whole catalogue. Typing searches (debounced by 300 ms); clearing the field brings
  the catalogue back.
- Pages of 24 load as you scroll; pulling the list down fetches it again.
- The search field slides away as the list scrolls down and comes back on scroll up.
- Tapping a row opens the full product in a bottom sheet.
- Every error offers a retry; a failed later page or refresh keeps the rows on screen.
- Dutch on every phone, prices and counts written the Dutch way. The query survives rotation and process death.

## Architecture

![How the app is built: nine modules with dependencies pointing inwards, a search going down to the API and back as one screen state, and one request at a time through collectLatest](docs/architecture.svg)

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
| Robolectric + Compose UI test | Runs the screen's UI tests on the JVM with the unit tests, so neither you nor CI needs an emulator. |

## Trade-offs

- **The app shows what the API returns.** The mock ignores the search term, so whether the app
  should search the catalogue itself is a product call. The `in-app-search` branch does, in
  [one commit on top of main](https://github.com/Kmozcan1/coolblue-android-assignment/compare/main...in-app-search) that touches only `search/domain` and this README. Main
  keeps the use case, which also skips a page left with no product to show, so turning in-app
  search on never touches the ViewModel.
- **Hand-rolled paging, not Paging 3.** Three pages of 24 don't need it. `ProductPage` maps
  straight onto a `PagingSource`, so the data side of a switch is small; the ViewModel and the
  list would trade their sealed `Paging` state, already shaped like `LoadState.append`, for
  `PagingData` and `LoadState`.
- **Nine modules for one screen.** More than the screen needs, but a second feature is three
  more modules and no edits to `search`.
- **Prices as integer cents.** The API's decimal euros are rounded to `Int` cents once, in the
  mapper, so the domain never does arithmetic on floats. The UI model's mapper writes them in Dutch,
  once per product, so drawing a row formats no numbers.
- **Dutch only.** The API sends product names, USPs and promo labels in Dutch only, so the app's
  own words, prices, ratings and counts are Dutch too, whatever the phone's language. `MainActivity`
  overrides only the language, so plural forms and the libraries' own strings follow it.

## Tests

| Module | Test | Covers |
|---|---|---|
| search/domain | `ProductPageTest` | next page from page count |
| search/domain | `SearchProductsUseCaseTest` | a page left with no product to show is skipped |
| search/data | `ProductMapperTest` | DTO to domain, euros to cents, optional fields |
| search/data | `SearchRepositoryImplTest` | MockWebServer: the real page 1 JSON (every mapped field), query parameters, 500, no connection, bad JSON, an unexpected exception, cancellation passing through, a product with no reviews or explicit nulls, products with no id, name or price skipped, one with no image kept |
| search/ui | `ProductUiMapperTest` | list price rule, Dutch price, rating and count formatting |
| search/ui | `SearchScreenTest` (Robolectric) | the product sheet stays closed once its product left the results, pull to refresh, loading more near the end and again when a refine is undone, the search field sliding away and back, a drag on the list closing the keyboard |
| search/ui | `SearchViewModelTest` | debounce, the opening query is not debounced, first page, empty, error and retry, load more, a row repeated within page 1 or by the next page, edit-and-undo during a load more, duplicate request guard, load-more error and retry, cancellation on query change, saved query and restore, opens on the whole catalogue, clearing brings it back, refine keeps the rows, a late result after clearing is dropped, retry while refining, edit-and-undo keeps the rows, also after the debounce, Retry shows Loading, empty keeps its query, pull to refresh replaces the rows, a failed refresh keeps them, refresh during a refine or a load more, a refresh landing after the query changed is dropped, a refresh survives an edit-and-undo, each failed refresh is a new value, a fast refresh stays visible, a refresh right after a load more, load more and a second refresh ignored during a refresh, a load-more error survives an edit-and-undo but not a refresh |

Run them with `./gradlew test`. They run on every push through GitHub Actions, together with
Android lint.

Every logic unit was written test-first: the failing test, then the smallest implementation
that makes it pass. Each commit carries its tests with it, and test names read as behaviour
(`given a load more in flight, when the query changes, then it is cancelled and the new query
loads`). The screen takes its state from the ViewModel, so the ViewModel tests cover its logic.
What the composables keep themselves (the open sheet, the scroll effects, the sliding field) has
Compose UI tests, added after the code and each checked by breaking the feature it covers.

## Built with

Claude Code as a pair programmer. Every design decision above was discussed first, every line
was reviewed, and the commit history reads as the build sequence. [CLAUDE.md](CLAUDE.md) is the
brief Claude worked from: the task, the conventions and the way of working.

## Left out on purpose

- A product detail screen: outside the user story. The bottom sheet shows what the search
  result already holds; there is no second request and no navigation.
- A navigation library: one screen.
- ktlint or detekt: valuable on a team, noise in an assignment of this size.
- Release signing: nothing here ships.
- An offline cache: search-as-you-type results are live by nature, and the API is a mock.
  A failed load offers a retry; the rows already on screen stay.
- Crash reporting: nothing ships, so there is no backend to report to. The repository logs
  what it turns into `AppError.Unknown`, which is the only failure that carries no other clue,
  and the mapper logs each product it skips.
