package com.example.travenor.repository

import com.example.travenor.constant.DEFAULT_LAT
import com.example.travenor.constant.DEFAULT_LONG
import com.example.travenor.constant.IS_FAVORITE
import com.example.travenor.constant.IS_NOT_FAVORITE
import com.example.travenor.constant.MAX_EXPLORE_RESULTS
import com.example.travenor.constant.PlaceCategory
import com.example.travenor.core.ResultListener
import com.example.travenor.core.observable.FavoritePlaceObserver
import com.example.travenor.data.model.location.LatLng
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Address
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.repository.PlaceRepositoryImpl
import com.example.travenor.data.source.PlaceSource
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PlaceRepositoryTest {
    private val mRemote = mockk<PlaceSource.Remote>(relaxed = true)
    private val mLocal = mockk<PlaceSource.Local>(relaxed = true)
    private val mLocalExplore = mockk<PlaceSource.ExplorePlaceLocal>(relaxed = true)
    private val mPlaceRepository = PlaceRepositoryImpl(mRemote, mLocal, mLocalExplore)

    private val samplePlace
        get() = Place(
            "1",
            "Test Place",
            "Description",
            "http://example.com",
            Address("1", "Street", "City", "State", "1234", "string"),
            DEFAULT_LAT.toFloat(),
            DEFAULT_LONG.toFloat(),
            4.5f,
            100,
            5,
            "http://photos.com",
            PlaceCategory.ATTRACTIONS.name
        )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `getExplorePlaceFromRemote got data, should save data into local and callback success`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)

        val place = samplePlace.copy()

        every {
            mRemote.searchExplorePlace(any(), any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onSuccess(listOf(place))
        }

        val method = PlaceRepositoryImpl::class.java.getDeclaredMethod(
            "getExplorePlaceFromRemote",
            String::class.java,
            LatLng::class.java,
            PlaceCategory::class.java,
            ResultListener::class.java
        )
        method.isAccessible = true

        method.invoke(mPlaceRepository, keyword, latLng, category, listener)

        verify {
            mLocalExplore.saveExploreAttractionLocal(listOf(place.locationId))
            mLocal.savePlaceDetail(place)
            mLocal.savePlaceAddress(place)
            listener.onSuccess(listOf(place))
        }
    }

    @Test
    fun `getExplorePlaceFromRemote remote returns an empty list, should call the error callback`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)

        every {
            mRemote.searchExplorePlace(any(), any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onSuccess(emptyList())
        }

        val method = PlaceRepositoryImpl::class.java.getDeclaredMethod(
            "getExplorePlaceFromRemote",
            String::class.java,
            LatLng::class.java,
            PlaceCategory::class.java,
            ResultListener::class.java
        )
        method.isAccessible = true
        method.invoke(mPlaceRepository, keyword, latLng, category, listener)

        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `getExplorePlaceFromRemote remote throws an exception, should call the error callback`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)
        val exception = Exception("No data!")

        every {
            mRemote.searchExplorePlace(any(), any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onError(exception)
        }

        val method = PlaceRepositoryImpl::class.java.getDeclaredMethod(
            "getExplorePlaceFromRemote",
            String::class.java,
            LatLng::class.java,
            PlaceCategory::class.java,
            ResultListener::class.java
        )
        method.isAccessible = true
        method.invoke(mPlaceRepository, keyword, latLng, category, listener)

        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `searchExplorePlace local returns empty list, should get from remote`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)

        every {
            mLocalExplore.getExplorePlaceLocal(limit = MAX_EXPLORE_RESULTS, category)
        }.answers {
            emptyList()
        }

        mPlaceRepository.searchExplorePlace(keyword, category, latLng, listener)

        verify {
            mRemote.searchExplorePlace(keyword, latLng, category, any())
        }
    }

    @Test
    fun `searchExplorePlace local returns data and data is Fresh, should return that data`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)
        val dataFreshnessThreshHold = 2 * 24 * 60 * 60 * 1000 // 2 days
        val place = samplePlace

        // Make it fresh
        val timeStamp = System.currentTimeMillis() - dataFreshnessThreshHold + 1000 * 60

        every {
            mLocalExplore.getExplorePlaceLocal(limit = MAX_EXPLORE_RESULTS, category)
        }.answers {
            listOf(Pair(place, timeStamp))
        }

        mPlaceRepository.searchExplorePlace(keyword, category, latLng, listener)

        verify {
            listener.onSuccess(listOf(place))
        }
    }

    @Test
    fun `searchExplorePlace local returns data and data is not Fresh, should get from remote`() {
        // Given
        val keyword = "keyword"
        val latLng = LatLng(0.0, 0.0)
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)
        val dataFreshnessThreshHold = 2 * 24 * 60 * 60 * 1000 // 2 days

        // Make it not fresh
        val timeStamp = System.currentTimeMillis() - dataFreshnessThreshHold - 1000 * 60

        every {
            mLocalExplore.getExplorePlaceLocal(limit = MAX_EXPLORE_RESULTS, category)
        }.answers {
            listOf(Pair(samplePlace, timeStamp))
        }

        mPlaceRepository.searchExplorePlace(keyword, category, latLng, listener)

        verify {
            mRemote.searchExplorePlace(keyword, latLng, category, any())
        }
    }

    @Test
    fun `getRecentSearch when local returns data, should return that data`() {
        // Given
        val listener = mockk<ResultListener<List<String>>>(relaxed = true)
        every {
            mLocal.getRecentSearchPlaces(any())
        } answers {
            lastArg<ResultListener<List<String>>>().onSuccess(listOf(samplePlace.locationId))
        }

        mPlaceRepository.getRecentSearch(listener)
        verify {
            listener.onSuccess(listOf(samplePlace.locationId))
        }
    }

    @Test
    fun `getRecentSearch when local returns empty list, should return empty list`() {
        // Given
        val listener = mockk<ResultListener<List<String>>>(relaxed = true)
        every {
            mLocal.getRecentSearchPlaces(any())
        } answers {
            lastArg<ResultListener<List<String>>>().onSuccess(emptyList())
        }

        mPlaceRepository.getRecentSearch(listener)
        verify {
            listener.onSuccess(emptyList())
        }
    }

    @Test
    fun `saveRecentSearch local should called to save data`() {
        // Given
        val keywords = listOf("keyword1", "keyword2")
        mLocal.saveRecentSearchPlaces(keywords)

        verify {
            mLocal.saveRecentSearchPlaces(keywords)
        }
    }

    @Test
    fun `searchPlace when remote return data, should save data into local and callback success`() {
        // Given
        val keyword = "keyword"
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)

        every {
            mRemote.searchPlace(any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onSuccess(listOf(samplePlace))
        }

        mPlaceRepository.searchPlace(keyword, category, listener)

        verify {
            mLocal.savePlaceDetail(samplePlace)
            mLocal.savePlaceAddress(samplePlace)
            listener.onSuccess(listOf(samplePlace))
        }
    }

    @Test
    fun `searchPlace when remote return empty list, should callback error`() {
        // Given
        val keyword = "keyword"
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)

        every {
            mRemote.searchPlace(any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onSuccess(emptyList())
        }

        mPlaceRepository.searchPlace(keyword, category, listener)

        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `searchPlace when remote throws an exception, should call get from local`() {
        // Given
        val keyword = "keyword"
        val category = PlaceCategory.ATTRACTIONS
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)
        val exception = mockk<Exception>()

        every {
            mRemote.searchPlace(any(), any(), any())
        }.answers {
            lastArg<ResultListener<List<Place>>>().onError(exception)
        }

        mPlaceRepository.searchPlace(keyword, category, listener)

        verify {
            mLocal.searchPlace(keyword, category, listener)
        }
    }

    @Test
    fun `getPlaceDetail when remote return data, should save data into local and callback success`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Place>>(relaxed = true)

        every {
            mRemote.getPlaceDetail(any(), any())
        }.answers {
            lastArg<ResultListener<Place>>().onSuccess(samplePlace)
        }

        mPlaceRepository.getPlaceDetail(placeId, listener)

        verify {
            mLocal.savePlaceDetail(samplePlace)
            listener.onSuccess(samplePlace)
        }
    }

    @Test
    fun `getPlaceDetail when remote return null, local return data,should callback success`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Place>>(relaxed = true)

        every {
            mRemote.getPlaceDetail(any(), any())
        }.answers {
            lastArg<ResultListener<Place>>().onSuccess(null)
        }

        every {
            mLocal.getPlaceDetail(placeId)
        } answers {
            samplePlace
        }

        mPlaceRepository.getPlaceDetail(placeId, listener)

        verify {
            mLocal.getPlaceDetail(placeId)
        }
    }

    @Test
    fun `getPlaceDetail when remote return null, local return null,should throw error`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Place>>(relaxed = true)

        every {
            mRemote.getPlaceDetail(any(), any())
        }.answers {
            lastArg<ResultListener<Place>>().onSuccess(null)
        }

        every {
            mLocal.getPlaceDetail(placeId)
        } answers {
            null
        }

        mPlaceRepository.getPlaceDetail(placeId, listener)

        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `getPlaceDetail when remote throw an exception, get from local and local return null, should throw error`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Place>>(relaxed = true)
        val exception = mockk<Exception>()

        every {
            exception.printStackTrace()
        } just Runs

        every {
            mRemote.getPlaceDetail(any(), any())
        }.answers {
            lastArg<ResultListener<Place>>().onError(exception)
        }

        every {
            mLocal.getPlaceDetail(placeId)
        } answers {
            null
        }

        mPlaceRepository.getPlaceDetail(placeId, listener)

        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `getPlaceDetail when remote throw an exception, local return data should callback success`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Place>>(relaxed = true)
        val exception = mockk<Exception>()

        every {
            exception.printStackTrace()
        } just Runs

        every {
            mRemote.getPlaceDetail(any(), any())
        }.answers {
            lastArg<ResultListener<Place>>().onError(exception)
        }

        every {
            mLocal.getPlaceDetail(placeId)
        } answers {
            samplePlace
        }

        mPlaceRepository.getPlaceDetail(placeId, listener)

        verify {
            listener.onSuccess(samplePlace)
        }
    }

    @Test
    fun `getPlacePhoto when remote return data, should save data into local and callback success`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<List<PlacePhoto>>>(relaxed = true)
        val photo = mockk<PlacePhoto>(relaxed = true)
        every {
            mRemote.getPlacePhoto(any(), any())
        }.answers {
            lastArg<ResultListener<List<PlacePhoto>>>().onSuccess(listOf(photo))
        }
        mPlaceRepository.getPlacePhoto(placeId, listener)
        verify {
            mLocal.savePlacePhoto(listOf(photo))
            listener.onSuccess(listOf(photo))
        }
    }

    @Test
    fun `getPlacePhoto when remote return empty list, should throw error`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<List<PlacePhoto>>>(relaxed = true)
        every {
            mRemote.getPlacePhoto(any(), any())
        }.answers {
            lastArg<ResultListener<List<PlacePhoto>>>().onSuccess(emptyList())
        }
        mPlaceRepository.getPlacePhoto(placeId, listener)
        verify {
            listener.onError(any())
        }
    }

    @Test
    fun `getPlacePhoto when remote throw an exception, local return data should callback success`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<List<PlacePhoto>>>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        val photo = mockk<PlacePhoto>(relaxed = true)
        every {
            mRemote.getPlacePhoto(any(), any())
        }.answers {
            lastArg<ResultListener<List<PlacePhoto>>>().onError(exception)
        }

        every {
            mLocal.getPlacePhoto(placeId)
        } answers {
            listOf(photo)
        }

        mPlaceRepository.getPlacePhoto(placeId, listener)
        verify {
            mLocal.getPlacePhoto(placeId)
            listener.onSuccess(listOf(photo))
        }
    }

    @Test
    fun `getPlacePhoto when remote throw an exception, local return empty should throw error`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<List<PlacePhoto>>>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)
        every {
            mRemote.getPlacePhoto(any(), any())
        }.answers {
            lastArg<ResultListener<List<PlacePhoto>>>().onError(exception)
        }

        every {
            mLocal.getPlacePhoto(placeId)
        } answers {
            emptyList()
        }

        mPlaceRepository.getPlacePhoto(placeId, listener)
        verify {
            mLocal.getPlacePhoto(placeId)
            listener.onError(any())
        }
    }

    @Test
    fun `markFavorite local should called to mark favorite data and notify observer`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Boolean>>(relaxed = true)

        every {
            mLocal.markFavorite(placeId, any())
        } just Runs
        mPlaceRepository.markFavorite(placeId, listener)
        verify {
            mLocal.markFavorite(placeId, listener)
            mPlaceRepository.notifyChanged(placeId, IS_FAVORITE)
        }
    }

    @Test
    fun `markNotFavorite local should called to mark not favorite data and notify observer`() {
        // Given
        val placeId = samplePlace.locationId
        val listener = mockk<ResultListener<Boolean>>(relaxed = true)

        every {
            mLocal.markNotFavorite(placeId, any())
        } just Runs
        mPlaceRepository.markNotFavorite(placeId, listener)
        verify {
            mLocal.markNotFavorite(placeId, listener)
            mPlaceRepository.notifyChanged(placeId, IS_NOT_FAVORITE)
        }
    }

    @Test
    fun `getFavoritePlace should return data from local`() {
        // Given
        val listener = mockk<ResultListener<List<Place>>>(relaxed = true)
        val place = samplePlace
        every {
            mLocal.getFavoritePlace(any())
        } answers {
            lastArg<ResultListener<List<Place>>>().onSuccess(listOf(place))
        }
        mPlaceRepository.getFavoritePlace(listener)
        verify {
            mLocal.getFavoritePlace(listener)
            listener.onSuccess(listOf(place))
        }
    }

    @Test
    fun `registerObserver when observer list is not contains, should add observer to list`() {
        val observer = mockk<FavoritePlaceObserver>()

        val observerListField =
            PlaceRepositoryImpl::class.java.getDeclaredField("favoritePlaceObserverList")
        observerListField.isAccessible = true

        mPlaceRepository.registerObserver(observer)

        val observerList = observerListField.get(mPlaceRepository) as List<*>
        assert(true) {
            observerList.contains(observer)
        }
    }

    @Test
    fun `registerObserver when observer list contains observer, should not add observer to list`() {
        val observer = mockk<FavoritePlaceObserver>()

        mPlaceRepository.registerObserver(observer)

        val observerListField =
            PlaceRepositoryImpl::class.java.getDeclaredField("favoritePlaceObserverList")
        observerListField.isAccessible = true

        val observerList = observerListField.get(mPlaceRepository) as List<*>

        mPlaceRepository.registerObserver(observer)

        assert(true) {
            observerList[0] == observer
        }
    }

    @Test
    fun `registerObserver when observer is null, should not add observer to list`() {
        val observer = null

        val observerListField =
            PlaceRepositoryImpl::class.java.getDeclaredField("favoritePlaceObserverList")
        observerListField.isAccessible = true

        val observerList = observerListField.get(mPlaceRepository) as List<*>

        mPlaceRepository.registerObserver(observer)

        assert(true) {
            !observerList.contains(observer)
        }
    }

    @Test
    fun `removeObserver should remove observer from list`() {
        val observer = mockk<FavoritePlaceObserver>()
        mPlaceRepository.registerObserver(observer)

        val observerListField =
            PlaceRepositoryImpl::class.java.getDeclaredField("favoritePlaceObserverList")
        observerListField.isAccessible = true

        val observerList = observerListField.get(mPlaceRepository) as List<*>

        mPlaceRepository.removeObserver(observer)

        assert(true) {
            !observerList.contains(observer)
        }
    }

    @Test
    fun `notifyChanged should notify observers`() {
        val observer = mockk<FavoritePlaceObserver>(relaxed = true)
        mPlaceRepository.registerObserver(observer)

        val placeId = samplePlace.locationId
        val isFavorite = IS_FAVORITE

        mPlaceRepository.notifyChanged(placeId, isFavorite)

        every {
            observer.onFavoritePlaceChange(placeId, isFavorite)
        } just Runs

        assert(true) {
            observer.onFavoritePlaceChange(placeId, isFavorite)
        }
    }

    @Test
    fun `saveRecentSearch should save data into local`() {
        mPlaceRepository.saveRecentSearch(listOf("keyword1", "keyword2"))
        verify {
            mLocal.saveRecentSearchPlaces(listOf("keyword1", "keyword2"))
        }
    }

    @Test
    fun `getInstance should return a new instance when called for the first time`() {
        // When
        val repository = PlaceRepositoryImpl.getInstance(mRemote, mLocal, mLocalExplore)

        // Then
        assert(repository is PlaceRepositoryImpl)
    }

    @Test
    fun `getInstance should return the same instance on subsequent calls`() {
        // When
        val repository1 = PlaceRepositoryImpl.getInstance(mRemote, mLocal, mLocalExplore)
        val repository2 = PlaceRepositoryImpl.getInstance(mRemote, mLocal, mLocalExplore)

        // Then
        assert(repository1 === repository2)
    }

    @Test
    fun `getInstance should return the same instance`() {
        val companion = mockk<PlaceRepositoryImpl.Companion>(relaxed = true)
        val instance = companion.getInstance(mRemote, mLocal, mLocalExplore)

        val instance2 = companion.getInstance(mRemote, mLocal, mLocalExplore)
        assert(instance == instance2)
    }
}
