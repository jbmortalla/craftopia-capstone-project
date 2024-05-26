package com.capstone.craftopiaproject.transaction

data class CartList(
    var productId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var subcategory: String = "",
    var imageUrl: String = "",
    var quantity: Int = 0,
    var isSelected: Boolean = false
)

