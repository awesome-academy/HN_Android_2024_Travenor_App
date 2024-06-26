package com.example.travenor.data.source.local

import android.content.Context
import android.database.sqlite.SQLiteException
import com.example.travenor.constant.PlaceCategory
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.source.PlaceSource
import com.example.travenor.data.source.local.database.dao.PlaceDAO
import com.example.travenor.data.source.local.database.dao.PlaceExploreDAO
import com.example.travenor.utils.ext.notNull

class PlaceExploreLocalSource(
    private val placeDao: PlaceDAO,
    private val placeExploreDAO: PlaceExploreDAO
) : PlaceSource.ExplorePlaceLocal {
    override fun getExplorePlaceLocal(
        limit: Int,
        category: PlaceCategory
    ): List<Pair<Place, Long>> {
        try {
            val idList = when (category) {
                PlaceCategory.ATTRACTIONS -> {
                    placeExploreDAO.getAttractionExplore(limit)
                }

                PlaceCategory.RESTAURANTS -> {
                    placeExploreDAO.getRestaurantExplore(limit)
                }

                PlaceCategory.HOTELS -> {
                    placeExploreDAO.getHotelExplore(limit)
                }
            }

            val result = mutableListOf<Pair<Place, Long>>()

            idList.forEach { pair ->
                val locationId = pair.first
                val place = placeDao.getPlaceData(locationId)
                place.notNull { result.add(Pair(it, pair.second)) }
            }

            return result
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
            return emptyList()
        }
    }

    override fun saveExploreAttractionLocal(idList: List<String>) {
        try {
            idList.forEach {
                placeExploreDAO.saveExploreAttraction(it, System.currentTimeMillis())
            }
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    override fun saveExploreRestaurantLocal(idList: List<String>) {
        try {
            idList.forEach {
                placeExploreDAO.saveExploreRestaurant(it, System.currentTimeMillis())
            }
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    override fun saveExploreHotelLocal(idList: List<String>) {
        try {
            idList.forEach {
                placeExploreDAO.saveExploreHotel(it, System.currentTimeMillis())
            }
        } catch (e: SQLiteException) {
            e.run { printStackTrace() }
        }
    }

    companion object {
        private var instance: PlaceExploreLocalSource? = null

        fun getInstance(context: Context) = synchronized(this) {
            val placeDao = PlaceDAO.getInstance(context)
            val placeExploreDAO = PlaceExploreDAO(context)
            instance ?: PlaceExploreLocalSource(
                placeDao,
                placeExploreDAO
            ).also { instance = it }
        }
    }
}
