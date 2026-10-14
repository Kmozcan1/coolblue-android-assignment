package com.coolblue.assignment.search.data.mapper

import android.util.Log
import com.coolblue.assignment.search.data.api.ProductDto
import com.coolblue.assignment.search.data.api.SearchResponseDto
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.domain.model.ProductPage
import kotlin.math.roundToInt

internal fun SearchResponseDto.toDomain(): ProductPage = ProductPage(
    products = products.mapNotNull { it.toDomainOrNull() },
    page = currentPage,
    pageCount = pageCount,
)

/** Null for a product with no id, name, image or price: one incomplete product must not cost the page the others. */
internal fun ProductDto.toDomainOrNull(): Product? {
    if (productId == null || productName == null || productImage == null || salesPriceIncVat == null) {
        Log.w(TAG, "Skipped product $productId: it has no id, name, image or price")
        return null
    }
    return Product(
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
}

/** The API sends euros as a decimal number; the domain keeps cents. Rounding absorbs float noise (26.99 * 100 = 2698.9999…). */
private fun Double.toCents(): Int = (this * 100).roundToInt()

private const val TAG = "ProductMapper"
