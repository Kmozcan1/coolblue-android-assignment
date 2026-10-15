package com.coolblue.assignment.search.ui.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import com.coolblue.assignment.core.ui.theme.CoolblueTheme
import com.coolblue.assignment.search.ui.model.ProductUiModel
import com.coolblue.assignment.search.ui.presentation.Paging
import com.coolblue.assignment.search.ui.presentation.SearchContent
import com.coolblue.assignment.search.ui.presentation.SearchUiAction
import com.coolblue.assignment.search.ui.presentation.SearchUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchScreenTest {

    // A finger puts a phone in touch mode; Robolectric's injected touches don't, and outside touch mode
    // Android hands a cleared focus straight back to the first field. Set before the window exists.
    @get:Rule(order = 0)
    val touchMode = object : ExternalResource() {
        override fun before() = InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
    }

    @get:Rule(order = 1)
    val compose = createComposeRule()

    private var uiState by mutableStateOf(SearchUiState())
    private val sentActions = mutableListOf<SearchUiAction>()

    private val searchField get() = compose.onNode(hasSetTextAction())
    private val productList get() = compose.onNode(hasScrollToIndexAction())

    private fun showScreen(state: SearchUiState) {
        uiState = state
        compose.setContent { CoolblueTheme { SearchScreen(uiState, sentActions::add) } }
    }

    private fun product(id: Int) = ProductUiModel(
        id = id,
        name = "Product $id",
        imageUrl = null,
        price = "€ 9,99",
        listPrice = null,
        rating = "8,0",
        reviewCount = 3,
        reviewCountText = "3",
        usps = emptyList(),
        promoText = null,
        nextDayDelivery = false,
    )

    private fun results(vararg ids: Int, paging: Paging = Paging.End) =
        SearchContent.Loaded(searchedQuery = "", products = ids.map(::product), paging = paging)

    private val manyIds = (1..24).toList().toIntArray()

    @Test
    fun `given a product's sheet is open, when the product leaves the results and comes back, then the sheet stays closed`() {
        showScreen(SearchUiState(content = results(1, 2)))
        compose.onNodeWithText("Product 1").performClick()
        compose.onAllNodesWithText("Product 1").assertCountEquals(2)

        uiState = SearchUiState(content = results(2))
        compose.waitForIdle()
        uiState = SearchUiState(content = results(1, 2))

        compose.onAllNodesWithText("Product 1").assertCountEquals(1)
    }

    @Test
    fun `given rows, when the list is pulled down, then a refresh is asked for`() {
        showScreen(SearchUiState(content = results(1, 2)))

        productList.performTouchInput { swipeDown() }

        compose.runOnIdle { assertThat(sentActions).contains(SearchUiAction.Refresh) }
    }

    @Test
    fun `given more pages, when the list scrolls near its end, then the next page is asked for`() {
        showScreen(SearchUiState(content = results(*manyIds, paging = Paging.Idle(nextPage = 2))))
        assertThat(sentActions).doesNotContain(SearchUiAction.LoadMore)

        productList.performScrollToIndex(20)

        compose.runOnIdle { assertThat(sentActions).contains(SearchUiAction.LoadMore) }
    }

    @Test
    fun `given the end was reached while refining, when the refine is undone, then the next page is asked for again`() {
        showScreen(SearchUiState(query = "apple", content = results(1, 2, paging = Paging.Idle(nextPage = 2))))
        sentActions.clear()

        uiState = uiState.copy(query = "")

        compose.runOnIdle { assertThat(sentActions).contains(SearchUiAction.LoadMore) }
    }

    @Test
    fun `when the list scrolls down, then the search field slides away, and it comes back on scroll up`() {
        showScreen(SearchUiState(content = results(*manyIds)))

        productList.performTouchInput { swipeUp() }
        searchField.assertIsNotDisplayed()

        productList.performTouchInput { swipeDown() }
        searchField.assertIsDisplayed()
    }

    @Test
    fun `given the search field is focused, when the list is dragged, then the field loses focus`() {
        showScreen(SearchUiState(content = results(*manyIds)))
        searchField.requestFocus()
        searchField.assertIsFocused()

        productList.performTouchInput { swipeUp() }

        searchField.assertIsNotFocused()
    }
}
