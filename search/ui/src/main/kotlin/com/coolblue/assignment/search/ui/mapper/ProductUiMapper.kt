package com.coolblue.assignment.search.ui.mapper

import com.coolblue.assignment.search.domain.model.Product
import com.coolblue.assignment.search.ui.model.ProductUiModel
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private val Euro: Currency = Currency.getInstance("EUR")

internal fun Product.toUiModel(): ProductUiModel = ProductUiModel(
    id = id,
    name = name,
    imageUrl = imageUrl,
    priceCents = salesPriceCents,
    // A list price only means something when it is higher than what the customer pays.
    listPriceCents = listPriceCents?.takeIf { it > salesPriceCents },
    rating = reviewAverage,
    reviewCount = reviewCount,
    usps = usps,
    promoText = promoText,
    nextDayDelivery = nextDayDelivery,
)

/** 107900 → "€ 1.079,00" in Dutch, "€1,079.00" in English. */
internal fun formatPrice(cents: Int, locale: Locale): String =
    NumberFormat.getCurrencyInstance(locale).apply { currency = Euro }.format(cents / 100.0)

/** 2246 → "2.246" in Dutch, "2,246" in English. */
internal fun formatCount(count: Int, locale: Locale): String = NumberFormat.getIntegerInstance(locale).format(count)

/** 9.1 → "9,1" in Dutch, "9.1" in English. */
internal fun formatRating(average: Double, locale: Locale): String =
    NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
        roundingMode = RoundingMode.HALF_UP
    }.format(average)
