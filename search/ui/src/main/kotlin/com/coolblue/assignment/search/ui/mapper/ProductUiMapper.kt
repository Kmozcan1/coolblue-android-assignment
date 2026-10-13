package com.coolblue.assignment.search.ui.mapper

import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.ui.model.ProductUiModel
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

private val Dutch: Locale = Locale.forLanguageTag("nl-NL")

// Formatters are not thread-safe. Mapping only ever runs on the main thread (viewModelScope), so one of each is enough.
private val PriceFormat: NumberFormat = NumberFormat.getCurrencyInstance(Dutch)
private val RatingFormat: NumberFormat = NumberFormat.getNumberInstance(Dutch).apply {
    minimumFractionDigits = 1
    maximumFractionDigits = 1
    roundingMode = RoundingMode.HALF_UP
}

internal fun Product.toUiModel(): ProductUiModel = ProductUiModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    price = formatPrice(salesPriceCents),
    // A list price only means something when it is higher than what the customer pays.
    listPrice = listPriceCents?.takeIf { it > salesPriceCents }?.let(::formatPrice),
    rating = formatRating(reviewAverage),
    reviewCount = reviewCount,
    usps = usps,
    promoText = promoText,
    nextDayDelivery = nextDayDelivery,
)

/** 36900 → "€ 369,00". The data is Dutch, so the price format is too. */
private fun formatPrice(cents: Int): String = PriceFormat.format(cents / 100.0)

/** 9.1 → "9,1". */
private fun formatRating(average: Double): String = RatingFormat.format(average)
