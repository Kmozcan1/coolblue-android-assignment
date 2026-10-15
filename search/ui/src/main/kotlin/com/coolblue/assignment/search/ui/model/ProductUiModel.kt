package com.coolblue.assignment.search.ui.model

import androidx.compose.runtime.Immutable

/** A product ready to draw, its prices, rating and review count already written the Dutch way. */
@Immutable
internal data class ProductUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val price: String,
    /** Only when higher than [price]: the old price. */
    val listPrice: String?,
    val rating: String,
    /** Picks the Dutch plural; [reviewCountText] is the number shown. */
    val reviewCount: Int,
    val reviewCountText: String,
    val usps: List<String>,
    val promoText: String?,
    val nextDayDelivery: Boolean,
)
