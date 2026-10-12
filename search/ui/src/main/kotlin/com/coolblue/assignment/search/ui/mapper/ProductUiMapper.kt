package com.coolblue.assignment.search.ui.mapper

import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.ui.model.ProductUiModel
import java.text.NumberFormat
import java.util.Locale

private val Dutch: Locale = Locale.forLanguageTag("nl-NL")
private const val MAX_USPS = 3

internal fun Product.toUiModel(): ProductUiModel = ProductUiModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    price = formatPrice(salesPriceCents),
    // A list price only means something when it is higher than what the customer pays.
    listPrice = listPriceCents?.takeIf { it > salesPriceCents }?.let(::formatPrice),
    rating = formatRating(reviewAverage),
    reviewCount = reviewCount,
    usps = usps.take(MAX_USPS),
    promoText = promoText,
    nextDayDelivery = nextDayDelivery,
)

/** 36900 → "€ 369,00". The data is Dutch, so the price format is too. */
private fun formatPrice(cents: Int): String =
    NumberFormat.getCurrencyInstance(Dutch).format(cents / 100.0)

/** 9.1 → "9,1". */
private fun formatRating(average: Double): String = "%.1f".format(Dutch, average)
