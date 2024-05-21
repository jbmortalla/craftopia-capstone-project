package com.capstone.craftopiaproject.creation.product

class Product_List (
    var imageLink: String = "",
    var name: String = "",
    var price: Int = 0,
    var category: String = ""
)

object Lists {
    private val products = mutableListOf<Product_List>()

    fun addProduct(product: Product_List) {
        products.add(product)
    }

    fun getProductsByCategory(category: String): List<Product_List> {
        return products.filter { it.category == category }
    }

    fun getAllProducts(): List<Product_List> {
        return products
    }
}