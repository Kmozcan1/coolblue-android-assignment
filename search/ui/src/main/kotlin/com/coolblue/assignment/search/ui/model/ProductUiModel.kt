package com.coolblue.assignment.search.ui.model

import androidx.compose.runtime.Immutable

/** A product ready to draw. Prices and rating stay numbers, formatted in Dutch when drawn. */
@Immutable
internal data class ProductUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val priceCents: Int,
    /** Only when higher than [priceCents]: the old price. */
    val listPriceCents: Int?,
    val rating: Double,
    val reviewCount: Int,
    val usps: List<String>,
    val promoText: String?,
    val nextDayDelivery: Boolean,
)
