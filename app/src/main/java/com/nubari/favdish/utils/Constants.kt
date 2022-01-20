package com.nubari.favdish.utils

object Constants {
    const val DISH_TYPE: String = "DishType"
    const val DISH_CATEGORY: String = "DishCategory"
    const val DISH_COOKING_TIME: String = "DishCookingTime"

    const val DISH_IMAGE_SOURCE_LOCAL: String = "Local"
    const val DISH_IMAGE_SOURCE_ONLINE: String = "Online"

    const val EXTRA_DISH_DETAILS: String = "DishDetails"

    const val ALL_ITEMS: String = "ALL"
    const val FILTER_SELECTION: String = "FilterSelection"

    const val API_ENDPOINT: String = "recipes/random"
    const val API_KEY: String = "apiKey"
    const val LIMIT_LICENSE: String = "limitLicense"
    const val TAGS: String = "tags"
    const val NUMBER: String = "number"

    const val BASE_URL = "https://api.spoonacular.com/"

    const val API_KEY_VALUE: String = "258535a55a894d5581d445940f739945"
    const val LIMIT_LICENSE_VALUE: Boolean = true
    const val TAGS_VALUE: String = "vegetarian, dessert"
    const val NUMBER_VALUE: Int = 1

    const val NOTIFICATION_ID = "FavDish_notification_id"
    const val NOTIFICATION_NAME = "FavDish"
    const val NOTIFICATION_CHANNEL = "FavDish_channel_01"

    fun dishTypes(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("Breakfast")
        list.add("lunch")
        list.add("Dinner")
        list.add("Snacks")
        return list
    }

    fun dishCategories(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("Pizza")
        list.add("BBQ")
        list.add("Bakery")
        list.add("Drinks")
        return list
    }

    fun dishCookingTime(): ArrayList<String> {
        val list = ArrayList<String>()
        list.add("10")
        list.add("20")
        list.add("30")
        list.add("40")
        list.add("50")
        return list
    }
}