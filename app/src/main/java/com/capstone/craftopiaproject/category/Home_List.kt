package com.capstone.craftopiaproject.category

class Home_List (
    var categoryImg : Int = 0,
    var categoryName: String
)

class lists{
    companion object{
        val listOfCategory = mutableListOf<Home_List>()

        fun addCategory(home_List: Home_List){
            listOfCategory.add(home_List)
        }

        fun getCategory(): List<Home_List>{
            return listOfCategory.toList()
        }
    }
}

