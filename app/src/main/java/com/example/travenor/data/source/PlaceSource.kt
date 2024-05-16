package com.example.travenor.data.source

import com.example.travenor.constant.PlaceCategory
import com.example.travenor.core.ResultListener
import com.example.travenor.data.model.location.LatLng
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Place

interface PlaceSource {
    interface Remote {
        fun searchPlace(
            query: String,
            category: PlaceCategory?,
            listener: ResultListener<List<Place>>
        )

        fun searchExplorePlace(
            keyword: String,
            latLng: LatLng,
            category: PlaceCategory,
            listener: ResultListener<List<Place>>
        )

        fun getPlaceDetail(placeId: String, listener: ResultListener<Place>)

        fun getPlacePhoto(placeId: String, listener: ResultListener<List<PlacePhoto>>)

        fun getNearbyPlace(
            latLng: LatLng,
            category: PlaceCategory,
            radius: Double = 10000.0,
            listener: ResultListener<List<Place>>
        )
    }

    @Suppress("TooManyFunctions")
    interface Local {
        fun getPlaceDetail(locationId: String): Place?

        fun getPlacePhoto(locationId: String): List<PlacePhoto>?

        fun savePlaceDetail(place: Place)

        fun savePlacePhoto(placePhotos: List<PlacePhoto>)

        fun searchPlace(
            query: String,
            category: PlaceCategory?,
            listener: ResultListener<List<Place>>
        )

        fun savePlaceAddress(place: Place)
        fun getNearbyPlaceLocal(
            latLng: LatLng,
            category: PlaceCategory,
            limit: Int = 5,
            radius: Double = 10000.0
        ): List<Place>?

        fun markFavorite(placeId: String, listener: ResultListener<Boolean>)

        fun markNotFavorite(placeId: String, listener: ResultListener<Boolean>)

        fun getFavoriteState(placeId: String): Int

        fun getFavoritePlace(listener: ResultListener<List<Place>>)

        fun getRecentSearchPlaces(listener: ResultListener<List<String>>)

        fun saveRecentSearchPlaces(keyword: List<String>)
    }

    interface ExplorePlaceLocal {
        fun getExplorePlaceLocal(limit: Int = 5, category: PlaceCategory): List<Pair<Place, Long>>

        fun saveExploreAttractionLocal(idList: List<String>)

        fun saveExploreRestaurantLocal(idList: List<String>)

        fun saveExploreHotelLocal(idList: List<String>)
    }
}
