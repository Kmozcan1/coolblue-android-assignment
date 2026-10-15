package com.coolblue.assignment.search.domain.model

/** One product as the app understands it. Prices are in cents so no arithmetic can drift. */
data class Product(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val salesPriceCents: Int,
    val listPriceCents: Int?,
    val reviewAverage: Double,
    val reviewCount: Int,
    val usps: List<String>,
    val promoText: String?,
    val nextDayDelivery: Boolean,
)
