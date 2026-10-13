package com.coolblue.assignment.search.ui.model

import androidx.compose.runtime.Immutable

/** A product ready to draw: prices and rating are already text. */
@Immutable
data class ProductUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val price: String,
    val listPrice: String?,
    val rating: String,
    val reviewCount: Int,
    val usps: List<String>,
    val promoText: String?,
    val nextDayDelivery: Boolean,
)
