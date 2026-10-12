package com.coolblue.assignment.search.data.mapper

import com.coolblue.assignment.search.data.api.ProductDto
import com.coolblue.assignment.search.data.api.PromoIconDto
import com.coolblue.assignment.search.data.api.ReviewInformationDto
import com.coolblue.assignment.search.data.api.ReviewSummaryDto
import com.coolblue.assignment.search.data.api.SearchResponseDto
import com.coolblue.assignment.search.domain.model.Product
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProductMapperTest {

    private val dto = ProductDto(
        productId = 785359,
        productName = "Apple iPhone 6 32GB Grijs",
        salesPriceIncVat = 26.99,
        listPriceIncVat = 29.99,
        productImage = "https://image.coolblue.nl/300x750/products/818870",
        reviewInformation = ReviewInformationDto(ReviewSummaryDto(reviewAverage = 9.1, reviewCount = 952)),
        usps = listOf("32 GB opslagcapaciteit", "4,7 inch Retina HD scherm", "iOS 11"),
        promoIcon = PromoIconDto(text = "middenklasse iPhone", type = "coolblues-choice"),
        nextDayDelivery = true,
    )

    @Test
    fun `given a full product dto, when mapped, then every field lands and prices become cents`() {
        assertThat(dto.toDomain()).isEqualTo(
            Product(
                id = 785359,
                name = "Apple iPhone 6 32GB Grijs",
                imageUrl = "https://image.coolblue.nl/300x750/products/818870",
                salesPriceCents = 2699,
                listPriceCents = 2999,
                reviewAverage = 9.1,
                reviewCount = 952,
                usps = listOf("32 GB opslagcapaciteit", "4,7 inch Retina HD scherm", "iOS 11"),
                promoText = "middenklasse iPhone",
                nextDayDelivery = true,
            ),
        )
    }

    @Test
    fun `given no list price and no promo, when mapped, then both are null`() {
        val product = dto.copy(listPriceIncVat = null, promoIcon = null).toDomain()

        assertThat(product.listPriceCents).isNull()
        assertThat(product.promoText).isNull()
    }

    @Test
    fun `given a response dto, when mapped, then page numbers are kept`() {
        val page = SearchResponseDto(products = listOf(dto), currentPage = 2, pageCount = 3).toDomain()

        assertThat(page.page).isEqualTo(2)
        assertThat(page.pageCount).isEqualTo(3)
        assertThat(page.products).hasSize(1)
    }
}
