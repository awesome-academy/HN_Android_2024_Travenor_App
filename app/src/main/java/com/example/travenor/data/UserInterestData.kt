package com.example.travenor.data

import com.example.travenor.constant.Food
import com.example.travenor.constant.Place

object UserInterestData {
    var isFirstQueryUserInterest = true
    val interestedPlaceList = mutableListOf<Place>()
    val interestFoodList = mutableListOf<Food>()
}
