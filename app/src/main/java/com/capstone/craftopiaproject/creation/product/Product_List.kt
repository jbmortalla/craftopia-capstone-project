package com.capstone.craftopiaproject.creation.product

class Product_List (
    var productId: String? = null,
    var imageLink: String? = null,
    var name: String? = null,
    var price: Double? = null,
    var description: String? = null,
    var subcategory: String? = null,
    var category: String? = null
){
    constructor() : this("", "", "", 0.00, "", "")
}

object Lists {
    private val products = mutableListOf<Product_List>()

    fun addProduct(product: Product_List) {
        products.add(product)
    }

}