package com.coolblue.assignment.search.ui.mapper

import com.coolblue.assignment.search.domain.model.Product
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProductUiMapperTest {

    private val product = Product(
        id = 1,
        name = "Apple iPhone 6 32GB Grijs",
        imageUrl = "https://image.coolblue.nl/300x750/products/818870",
        salesPriceCents = 2699,
        listPriceCents = null,
        reviewAverage = 9.1,
        reviewCount = 952,
        usps = listOf("a", "b", "c", "d"),
        promoText = null,
        nextDayDelivery = true,
    )

    @Test
    fun `given 2699 cents, when mapped, then the price reads as Dutch euros`() {
        assertThat(product.toUiModel().price.normalised()).isEqualTo("€ 26,99")
    }

    @Test
    fun `given a higher list price, when mapped, then it is formatted too`() {
        val ui = product.copy(listPriceCents = 36900).toUiModel()

        assertThat(ui.listPrice?.normalised()).isEqualTo("€ 369,00")
    }

    @Test
    fun `given a list price not above the sales price, when mapped, then no list price is shown`() {
        val ui = product.copy(listPriceCents = 2699).toUiModel()

        assertThat(ui.listPrice).isNull()
    }

    @Test
    fun `given a 9_1 average, when mapped, then the rating uses a decimal comma`() {
        assertThat(product.toUiModel().rating).isEqualTo("9,1")
    }

    @Test
    fun `given four usps, when mapped, then only the first three are kept`() {
        assertThat(product.toUiModel().usps).containsExactly("a", "b", "c").inOrder()
    }

    // ICU puts a non-breaking space after the euro sign; the assertions read better with a plain one.
    private fun String.normalised() = replace(' ', ' ')
}
