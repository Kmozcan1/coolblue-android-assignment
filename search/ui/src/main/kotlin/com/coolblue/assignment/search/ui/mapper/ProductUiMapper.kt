package com.coolblue.assignment.search.ui.mapper

import com.coolblue.assignment.core.ui.Dutch
import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.ui.model.ProductUiModel
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

private val Euro: Currency = Currency.getInstance("EUR")

internal fun Product.toUiModel(): ProductUiModel = ProductUiModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    price = formatPrice(salesPriceCents),
    // A list price only means something when it is higher than what the customer pays.
    listPrice = listPriceCents?.takeIf { it > salesPriceCents }?.let(::formatPrice),
    rating = formatRating(reviewAverage),
    reviewCount = reviewCount,
    reviewCountText = formatCount(reviewCount),
    usps = usps,
    promoText = promoText,
    nextDayDelivery = nextDayDelivery,
)

/** 107900 → "€ 1.079,00". */
private fun formatPrice(cents: Int): String =
    NumberFormat.getCurrencyInstance(Dutch).apply { currency = Euro }.format(cents / 100.0)

/** 2246 → "2.246". */
internal fun formatCount(count: Int): String = NumberFormat.getIntegerInstance(Dutch).format(count)

/** 9.1 → "9,1". */
private fun formatRating(average: Double): String =
    NumberFormat.getNumberInstance(Dutch).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
        roundingMode = RoundingMode.HALF_UP
    }.format(average)
