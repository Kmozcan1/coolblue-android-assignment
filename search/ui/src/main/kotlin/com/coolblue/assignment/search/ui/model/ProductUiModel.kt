package com.coolblue.assignment.search.ui.model

/** A product ready to draw: prices and rating are already text. */
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
