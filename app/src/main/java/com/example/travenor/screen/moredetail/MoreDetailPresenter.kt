package com.example.travenor.screen.moredetail

import com.example.travenor.constant.NEARBY_DISTANCE_IN_METERS
import com.example.travenor.constant.PlaceCategory
import com.example.travenor.core.ResultListener
import com.example.travenor.data.model.location.LatLng
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.repository.NearbyRepository
import com.example.travenor.data.repository.PlaceRepository

class MoreDetailPresenter internal constructor(
    private val placeRepository: PlaceRepository,
    private val nearbyRepository: NearbyRepository
) : MoreDetailContract.Presenter {
    private var mView: MoreDetailContract.View? = null
    override fun getPlaceDetail(locationId: String) {
        placeRepository.getPlaceDetail(
            locationId,
            object : ResultListener<Place> {
                override fun onSuccess(data: Place?) {
                    if (data != null) {
                        mView?.onGetPlaceDetailSuccess(data)
                    } else {
                        mView?.onGetPlaceDetailFail(Exception("Data null when get place detail"))
                    }
                }

                override fun onError(exception: Exception?) {
                    exception?.printStackTrace()
                    mView?.onGetPlaceDetailFail(exception!!)
                }
            }
        )
    }

    override fun markFavorite(placeId: String, isFavorite: Boolean) {
        // Toggle favorite
        if (isFavorite) {
            placeRepository.markFavorite(
                placeId,
                object : ResultListener<Boolean> {
                    override fun onSuccess(data: Boolean?) {
                        if (data == true) mView?.onMarkFavoriteSuccess()
                    }

                    override fun onError(exception: Exception?) {
                        exception?.printStackTrace()/* no-op */
                    }
                }
            )
        } else {
            placeRepository.markNotFavorite(
                placeId,
                object : ResultListener<Boolean> {
                    override fun onSuccess(data: Boolean?) {
                        if (data == true) mView?.onMarkNotFavoriteSuccess()
                    }

                    override fun onError(exception: Exception?) {
                        exception?.printStackTrace()/* no-op */
                    }
                }
            )
        }
    }

    override fun getPlacePhotos(locationId: String) {
        placeRepository.getPlacePhoto(
            locationId,
            object : ResultListener<List<PlacePhoto>> {
                override fun onSuccess(data: List<PlacePhoto>?) {
                    if (data.isNullOrEmpty()) return

                    for (photo in data) photo.locationId = locationId

                    mView?.onGetPhotoSuccess(data)
                }

                override fun onError(exception: Exception?) { /* no-op */
                }
            }
        )
    }

    override fun getNearbyRestaurant(lat: Double, long: Double) {
        nearbyRepository.getNearbyRestaurant(
            LatLng(lat, long),
            NEARBY_DISTANCE_IN_METERS,
            object : ResultListener<List<Place>> {
                override fun onSuccess(data: List<Place>?) {
                    if (!data.isNullOrEmpty()) {
                        for (place in data) {
                            getNearbyPlacePhoto(place.locationId, PlaceCategory.RESTAURANTS)
                        }
                        mView?.onGetNearbyRestaurantSuccess(data)
                    }
                }

                override fun onError(exception: Exception?) {
                    exception?.run { printStackTrace() }
                }
            }
        )
    }

    override fun getNearbyHotel(lat: Double, long: Double) {
        nearbyRepository.getNearbyHotel(
            LatLng(lat, long),
            NEARBY_DISTANCE_IN_METERS,
            object : ResultListener<List<Place>> {
                override fun onSuccess(data: List<Place>?) {
                    if (!data.isNullOrEmpty()) {
                        for (place in data) {
                            getNearbyPlacePhoto(place.locationId, PlaceCategory.HOTELS)
                        }
                        mView?.onGetNearbyHotelSuccess(data)
                    }
                }

                override fun onError(exception: Exception?) {
                    exception?.printStackTrace()
                }
            }
        )
    }

    override fun getNearbyPlacePhoto(locationId: String, category: PlaceCategory) {
        placeRepository.getPlacePhoto(
            locationId,
            object : ResultListener<List<PlacePhoto>> {
                override fun onSuccess(data: List<PlacePhoto>?) {
                    if (data.isNullOrEmpty()) return

                    for (photo in data) photo.locationId = locationId
                    mView?.onGetNearbyPlacePhotoSuccess(data, category)
                }

                override fun onError(exception: Exception?) { /* no-op */
                }
            }
        )
    }

    override fun setView(view: MoreDetailContract.View?) {
        mView = view
    }
}
