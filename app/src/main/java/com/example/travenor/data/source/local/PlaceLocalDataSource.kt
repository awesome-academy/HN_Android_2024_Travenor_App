package com.example.travenor.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteException
import com.example.travenor.constant.IS_NOT_FAVORITE
import com.example.travenor.constant.PlaceCategory
import com.example.travenor.core.ResultListener
import com.example.travenor.data.model.location.LatLng
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.source.PlaceSource
import com.example.travenor.data.source.local.database.dao.PlaceDAO
import com.example.travenor.data.source.local.database.dao.PlacePhotoDAO

@Suppress("TooManyFunctions")
class PlaceLocalDataSource(
    private val placeDao: PlaceDAO,
    private val placePhotoDAO: PlacePhotoDAO

) : PlaceSource.Local {

    override fun getPlaceDetail(locationId: String): Place? {
        try {
            return placeDao.getPlaceData(locationId)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            return null
        }
    }

    override fun getPlacePhoto(locationId: String): List<PlacePhoto>? {
        try {
            return placePhotoDAO.getPhotoData(locationId)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            return null
        }
    }

    override fun savePlaceDetail(place: Place) {
        try {
            placeDao.insertPlace(place)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    override fun savePlacePhoto(placePhotos: List<PlacePhoto>) {
        try {
            placePhotos.forEach {
                placePhotoDAO.insertPhoto(it)
            }
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    override fun searchPlace(
        query: String,
        category: PlaceCategory?,
        listener: ResultListener<List<Place>>
    ) {
        try {
            val result = placeDao.search(query, category)

            listener.onSuccess(result)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            return listener.onError(e)
        }
    }

    override fun savePlaceAddress(place: Place) {
        try {
            placeDao.insertPlaceAddress(place.addressObj, place.locationId)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    override fun getNearbyPlaceLocal(
        latLng: LatLng,
        category: PlaceCategory,
        limit: Int,
        radius: Double
    ): List<Place>? {
        return try {
            val result = placeDao.getNearByPlace(latLng.lat, latLng.lng, limit, radius, category)
            result.ifEmpty { null }
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            null
        }
    }

    override fun markFavorite(placeId: String, listener: ResultListener<Boolean>) {
        try {
            val result = placeDao.markFavorite(placeId)
            listener.onSuccess(result)
        } catch (e: SQLiteException) {
            listener.onError(e)
        }
    }

    override fun markNotFavorite(placeId: String, listener: ResultListener<Boolean>) {
        try {
            val result = placeDao.markNotFavorite(placeId)
            listener.onSuccess(result)
        } catch (e: SQLiteException) {
            listener.onError(e)
        }
    }

    override fun getFavoriteState(placeId: String): Int {
        try {
            return placeDao.isFavoritePlace(placeId)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            return IS_NOT_FAVORITE
        }
    }

    override fun getFavoritePlace(listener: ResultListener<List<Place>>) {
        try {
            val result = placeDao.getFavoritePlace()
            listener.onSuccess(result)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            listener.onError(e)
        }
    }

    override fun getRecentSearchPlaces(listener: ResultListener<List<String>>) {
        try {
            val result = placeDao.getRecentSearch()
            listener.onSuccess(result)
        } catch (e: SQLiteException) {
            listener.onError(e)
        }
    }

    override fun saveRecentSearchPlaces(keyword: List<String>) {
        try {
            placeDao.saveRecentSearch(keyword)
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    companion object {
        private var instance: PlaceLocalDataSource? = null

        fun getInstance(context: Context) = synchronized(this) {
            val placeDao = PlaceDAO.getInstance(context)
            val placePhotoDAO = PlacePhotoDAO(context)
            instance ?: PlaceLocalDataSource(
                placeDao,
                placePhotoDAO
            ).also { instance = it }
        }
    }
}
