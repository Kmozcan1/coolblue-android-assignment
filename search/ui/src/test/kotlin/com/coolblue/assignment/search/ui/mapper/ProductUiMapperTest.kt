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
    fun `given a higher list price, when mapped, then it is kept`() {
        assertThat(product.copy(listPriceCents = 36900).toUiModel().listPriceCents).isEqualTo(36900)
    }

    @Test
    fun `given a list price not above the sales price, when mapped, then no list price is shown`() {
        assertThat(product.copy(listPriceCents = 2699).toUiModel().listPriceCents).isNull()
    }

    @Test
    fun `given 2699 cents, then the price reads as euros with a decimal comma`() {
        assertThat(formatPrice(2699).normalised()).isEqualTo("€ 26,99")
    }

    @Test
    fun `given a price over a thousand euros, then thousands are grouped with a dot`() {
        assertThat(formatPrice(107900).normalised()).isEqualTo("€ 1.079,00")
    }

    @Test
    fun `given 2246 reviews, then the count is grouped with a dot`() {
        assertThat(formatCount(2246)).isEqualTo("2.246")
    }

    @Test
    fun `given a 9_1 average, then the rating uses a decimal comma`() {
        assertThat(formatRating(9.1)).isEqualTo("9,1")
    }

    // ICU puts a non-breaking space after the euro sign; the assertions read better with a plain one.
    private fun String.normalised() = replace('\u00A0', ' ')
}
