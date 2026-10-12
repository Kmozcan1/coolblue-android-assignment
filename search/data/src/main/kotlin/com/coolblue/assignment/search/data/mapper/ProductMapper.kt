package com.coolblue.assignment.search.data.mapper

import com.coolblue.assignment.search.data.api.ProductDto
import com.coolblue.assignment.search.data.api.SearchResponseDto
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import kotlin.math.roundToInt

internal fun SearchResponseDto.toDomain(): ProductPage = ProductPage(
    products = products.map { it.toDomain() },
    page = currentPage,
    pageCount = pageCount,
)

internal fun ProductDto.toDomain(): Product = Product(
    id = productId,
    name = productName,
    imageUrl = productImage,
    salesPriceCents = salesPriceIncVat.toCents(),
    listPriceCents = listPriceIncVat?.toCents(),
    reviewAverage = reviewInformation.reviewSummary.reviewAverage,
    reviewCount = reviewInformation.reviewSummary.reviewCount,
    usps = usps,
    promoText = promoIcon?.text,
    nextDayDelivery = nextDayDelivery,
)

/** The API sends euros as a decimal number; the domain keeps cents. Rounding absorbs float noise (26.99 * 100 = 2698.9999…). */
private fun Double.toCents(): Int = (this * 100).roundToInt()
