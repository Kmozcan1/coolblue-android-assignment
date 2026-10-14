package com.coolblue.assignment.search.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Only the fields the app reads.

@Serializable
internal data class SearchResponseDto(
    val products: List<ProductDto>,
    val currentPage: Int,
    val pageCount: Int,
)

@Serializable
internal data class ProductDto(
    val productId: Int? = null,
    val productName: String? = null,
    val salesPriceIncVat: Double? = null,
    val listPriceIncVat: Double? = null,
    val productImage: String? = null,
    val reviewInformation: ReviewInformationDto = ReviewInformationDto(),
    @SerialName("USPs") val usps: List<String> = emptyList(),
    val promoIcon: PromoIconDto? = null,
    val nextDayDelivery: Boolean = false,
)

@Serializable
internal data class ReviewInformationDto(
    val reviewSummary: ReviewSummaryDto = ReviewSummaryDto(),
)

@Serializable
internal data class ReviewSummaryDto(
    val reviewAverage: Double = 0.0,
    val reviewCount: Int = 0,
)

@Serializable
internal data class PromoIconDto(
    val text: String,
)
