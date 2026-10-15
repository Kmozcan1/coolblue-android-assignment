# Coolblue Android assignment

Kadir's technical assignment for Coolblue, pair-programmed with Claude Code. AI tools are
allowed, but Kadir is asked about the code in the interview: he must understand and be able to
explain every line. That decides most trade-offs below.

## The task

User story: *As a user, I want to search for a product, so that I can scroll through a list of
products and find something interesting to buy.*

Requirements: Kotlin and Jetpack Compose, code in an online repository, known design patterns
and a proper architecture, tests, open source components where useful, images.

## The API

`GET https://bdk0sta2n0.execute-api.eu-west-1.amazonaws.com/mobile-assignment/search?query=<term>&page=<n>`

- A mock: every query returns the same 70 products, in 3 pages of 24.
- Response: `products`, `currentPage`, `pageSize`, `totalResults`, `pageCount`.
- A product: `productId`, `productName`, `salesPriceIncVat`, `listPriceIncVat`, `productImage`,
  `reviewInformation.reviewSummary` (`reviewAverage`, `reviewCount`), `USPs`, `nextDayDelivery`,
  optional `promoIcon`.

## The code

Modules, data flow, libraries and trade-offs are in `README.md`; keep it true when the code
changes, including the unit test count and the Tests table. The module conventions come from
Kadir's own app, BeanBound (`~/AndroidStudioProjects/BeanBoundDenemee/mobile/ARCHITECTURE.md`),
trimmed to one feature.

- Dutch only: strings live in `values/`, numbers are written with `core.ui.Dutch`, and
  `MainActivity` sets the language on every phone.
- Prices are `Int` cents from the data mapper on; the UI mapper writes them in Dutch once.
- Paging is hand-rolled with a sealed `Paging` state, not Paging 3 (the README says why).
- UI tests run on Robolectric in `src/test`, so `./gradlew test` covers them without an emulator.

## Way of working

- Brainstorm before building: one question at a time, a recommendation first, and Kadir decides.
  When he asks for an honest opinion, give the textbook rule and the trade-off, not a comfortable
  answer.
- Multi-step work gets an estimate per step before it starts. Reviews come back as numbered
  findings grouped by severity, each with a recommendation; Kadir picks which to fix.
- Test first. Every new test is seen failing: before its fix, or by breaking the code on purpose
  (copy the file aside, change one line, run, restore the copy).
- Readable means names, not comments. Names say what the code does in product terms
  (`loadMoreProducts`, `retryFailedLoad`); a comment is only for a why the code cannot say.
- Plain over clever, and invisible quality (tests, accessibility, performance) over visible extras
  next to the search field.
- Small commits, one concept each, every one green, so the history reads as the build sequence.
  Messages say what changed for the user and end with Claude's `Co-Authored-By` trailer.
- Before merging, a skeptical review with one reviewer per lens (logic, test strength, docs);
  only real findings get fixed. Then a check on the emulator.
- Verify with `./gradlew test lint assembleDebug assembleRelease` on JDK 21; CI runs
  `test lint assembleDebug` on every push.
- Work happens on a branch and lands on `main` as a fast-forward. Nothing is pushed without
  Kadir's go.

**Never merge `in-app-search` into `main`.** It is one commit on an older `main` that searches in
the app, because the mock ignores the query: an option to show, not the submission.
