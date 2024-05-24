package com.capstone.craftopiaproject.creation.product

class Feedback(
    var userId: String = "",
    var name: String? = "",
    var feedback: String = "",
    var rating: Float = 0.0f,
    var timestamp: Long = 0,
    var imageUrl: String? = ""
)