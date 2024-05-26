package com.example.travenor.home

import com.example.travenor.constant.DEFAULT_LAT
import com.example.travenor.constant.DEFAULT_LONG
import com.example.travenor.constant.Food
import com.example.travenor.constant.PlaceCategory
import com.example.travenor.core.ResultListener
import com.example.travenor.data.model.photo.Image
import com.example.travenor.data.model.photo.ImageList
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Address
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.repository.PlaceRepository
import com.example.travenor.data.repository.UserRepository
import com.example.travenor.screen.home.ExplorePlaceContract
import com.example.travenor.screen.home.ExplorePlacePresenter
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

@Suppress("LargeClass")
class ExplorePlacePresenterTest {
    private val mView = mockk<ExplorePlaceContract.View>(relaxed = true)
    private val mPlaceRepository = mockk<PlaceRepository>(relaxed = true)
    private val mUserRepository = mockk<UserRepository>(relaxed = true)

    private val mPresenter = ExplorePlacePresenter(mPlaceRepository, mUserRepository)

    @Before
    fun setUp() {
        mPresenter.setView(mView)
    }

    @After
    fun tearDown() {
        mPresenter.setView(null)
    }

    @Test
    fun `getExploreAttraction when data is not null or empty should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.ATTRACTIONS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreAttraction()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreAttraction when cannot get user last location should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.ATTRACTIONS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreAttraction()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreAttraction when repo raise a exception should callback onFail`() {
        val exception = mockk<Exception>()
        val callback = slot<ResultListener<List<Place>>>()

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onError(exception)
        }

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        mPresenter.getExploreAttraction()

        verify {
            mView.onGetExplorePlaceFail(exception, PlaceCategory.ATTRACTIONS)
        }
    }

    @Test
    fun `getExploreAttraction when data is null or empty should callback onFail`() {
        val category = PlaceCategory.ATTRACTIONS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(emptyList())
        }

        mPresenter.getExploreAttraction()

        verify {
            mView.onGetExplorePlaceFail(any(), category)
        }
    }

    @Test
    fun `getExploreAttraction when can not get user Interest (empty search keyword) should success with random`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.ATTRACTIONS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        // Test a private func
        val method = ExplorePlacePresenter::class.java.getDeclaredMethod("randomPlaceKeyword")
        method.isAccessible = true
        val s: Any? = method.invoke(mPresenter)

        if (s is String) {
            every {
                mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
            }.answers {
                callback.captured.onSuccess(result)
            }
        } else {
            assert(false) { "Random result is not String" }
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreAttraction()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreRestaurant when data is not null or empty should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "SampleName", "Best sushi in town", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "restaurants"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.RESTAURANTS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreRestaurant()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreRestaurant when repo raise a exception should callback onFail`() {
        val exception = mockk<Exception>()
        val callback = slot<ResultListener<List<Place>>>()

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onError(exception)
        }

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        mPresenter.getExploreRestaurant()

        verify {
            mView.onGetExplorePlaceFail(exception, PlaceCategory.RESTAURANTS)
        }
    }

    @Test
    fun `getExploreRestaurant when cannot get user last location should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.RESTAURANTS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreRestaurant()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreRestaurant when data is null or empty should callback onFail`() {
        val category = PlaceCategory.RESTAURANTS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(emptyList())
        }

        mPresenter.getExploreRestaurant()

        verify {
            mView.onGetExplorePlaceFail(any(), category)
        }
    }

    @Test
    fun `getExploreRestaurant when can not get user Interest (empty search keyword) should success with random`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.RESTAURANTS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        // Test a private func
        val method = ExplorePlacePresenter::class.java.getDeclaredMethod("randomPlaceKeyword")
        method.isAccessible = true
        val s: Any? = method.invoke(mPresenter)

        if (s is String) {
            every {
                mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
            }.answers {
                callback.captured.onSuccess(result)
            }
        } else {
            assert(false) { "Random result is not String" }
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreRestaurant()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreHotel when data is not null or empty should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "SampleName", "Best sushi in town", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "restaurants"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.HOTELS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreHotel()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreHotel when repo raise a exception should callback onFail`() {
        val exception = mockk<Exception>()
        val callback = slot<ResultListener<List<Place>>>()

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onError(exception)
        }

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        mPresenter.getExploreHotel()

        verify {
            mView.onGetExplorePlaceFail(exception, PlaceCategory.HOTELS)
        }
    }

    @Test
    fun `getExploreHotel when cannot get user last location should return a result`() {
        val result = mutableListOf<Place>()

        val expectedPlaces = listOf(
            Place(
                "1", "Mountain", "Beautiful mountain", "http://example.com",
                Address(
                    "1",
                    "Street",
                    "City",
                    "Country",
                    "12345",
                    "s"
                ),
                21.03f, 105.83f, 4.5f, 100, 5, "http://photos.com", "attractions"
            )
        )
        result.addAll(expectedPlaces)

        val category = PlaceCategory.HOTELS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(0.0, 0.0)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(result)
        }

        mPresenter.getExploreHotel()

        verify {
            mView.onGetExplorePlaceSuccess(result, category)
        }
    }

    @Test
    fun `getExploreHotel when data is null or empty should callback onFail`() {
        val category = PlaceCategory.HOTELS

        val callback = slot<ResultListener<List<Place>>>()

        every {
            mUserRepository.getUserLastLocation()
        }.answers {
            Pair(DEFAULT_LAT, DEFAULT_LONG)
        }

        every {
            mPlaceRepository.searchExplorePlace(any(), any(), any(), capture(callback))
        }.answers {
            callback.captured.onSuccess(emptyList())
        }

        mPresenter.getExploreHotel()

        verify {
            mView.onGetExplorePlaceFail(any(), category)
        }
    }

    @Test
    fun `getUserInterest success has bolt places and foods`() {
        val expectedPlaces = listOf(com.example.travenor.constant.Place.CAVE)
        val expectedFoods = listOf(Food.ASIAN_FOOD)

        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onSuccess(expectedFoods)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onSuccess(expectedPlaces)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `getUserInterest success has only places`() {
        val expectedPlaces = listOf(com.example.travenor.constant.Place.CAVE)

        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onSuccess(null)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onSuccess(expectedPlaces)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `getUserInterest when food success or place raise exception should callback success`() {
        val expectedFoods = listOf(Food.ASIAN_FOOD)
        val exception = mockk<Exception>()

        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onError(exception)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onError(exception)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `getUserInterest success has only foods`() {
        val expectedFoods = listOf(Food.ASIAN_FOOD)

        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onSuccess(expectedFoods)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onSuccess(null)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `getUserInterest success when data is null`() {
        val expectedFoods = listOf(Food.ASIAN_FOOD)

        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onSuccess(expectedFoods)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onSuccess(null)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `getUserInterest should callback success when repo get fail`() {
        val exception = mockk<Exception>()
        val foodCallback = slot<ResultListener<List<Food>>>()
        val placeCallback = slot<ResultListener<List<com.example.travenor.constant.Place>>>()

        every {
            mUserRepository.getUserInterestedFood(capture(foodCallback))
        }.answers {
            foodCallback.captured.onError(exception)
        }

        every {
            mUserRepository.getUserInterestedPlace(capture(placeCallback))
        }.answers {
            placeCallback.captured.onError(exception)
        }

        mPresenter.getUserInterest()

        verify {
            mView.onGetUserInterestPlaceDone()
            mView.onGetUserInterestFoodDone()
        }
    }

    @Test
    fun `markFavorite success`() {
        val id = "exampleId"
        val callback = slot<ResultListener<Boolean>>()

        every {
            mPlaceRepository.markFavorite(id, capture(callback))
        } answers {
            callback.captured.onSuccess(true)
        }

        mPresenter.markFavorite("exampleId")

        // verify that method run
        verify {
            mPlaceRepository.markFavorite(id, any())
            mView wasNot called // No callback
        }
    }

    @Test
    fun `markFavorite fail`() {
        val id = "exampleId"
        val exception = mockk<Exception>()
        val callback = slot<ResultListener<Boolean>>()

        every {
            mPlaceRepository.markFavorite(id, capture(callback))
        } answers {
            callback.captured.onError(exception)
        }

        every {
            exception.printStackTrace()
        } answers {
            // do nothing
        }

        mPresenter.markFavorite("exampleId")

        // verify that method run and callback false
        verify {
            mPlaceRepository.markFavorite(eq(id), any())
            mView wasNot called // No callback
        }
    }

    @Test
    fun `markNotFavorite success`() {
        val id = "exampleId"
        val callback = slot<ResultListener<Boolean>>()

        every {
            mPlaceRepository.markNotFavorite(id, capture(callback))
        } answers {
            callback.captured.onSuccess(true)
        }

        mPresenter.markNotFavorite("exampleId")

        // verify that method run
        verify {
            mPlaceRepository.markNotFavorite(id, any())
            mView wasNot called // No callback
        }
    }

    @Test
    fun `markNotFavorite fail`() {
        val id = "exampleId"
        val exception = mockk<Exception>()
        val callback = slot<ResultListener<Boolean>>()

        every {
            mPlaceRepository.markNotFavorite(id, capture(callback))
        } answers {
            callback.captured.onError(exception)
        }

        every {
            exception.printStackTrace()
        } answers {
            // do nothing
        }

        mPresenter.markNotFavorite("exampleId")

        // verify that method run and callback false
        verify {
            mPlaceRepository.markNotFavorite(eq(id), any())
            mView wasNot called // No callback
        }
    }

    @Test
    fun `getThumbnail success`() {
        val presenter = spyk(mPresenter)
        val placeIdList = mutableListOf("place1", "place2")
        val category = PlaceCategory.ATTRACTIONS

        val image1 = Image(0, 0, "url")
        val image2 = Image(0, 0, "url")
        val imageList1 = ImageList(null, null, null, null, image1)
        val imageList2 = ImageList(null, null, null, null, image2)

        val placePhoto1 = PlacePhoto("", "", "", imageList1)
        val placePhoto2 = PlacePhoto("", "", "", imageList2)

        val callback = slot<ResultListener<List<PlacePhoto>>>()
        every {
            mPlaceRepository.getPlacePhoto(any(), capture(callback))
        } answers { invocation ->
            val placeId = invocation.invocation.args[0] as String

            when (placeId) {
                "place1" -> callback.captured.onSuccess(listOf(placePhoto1))
                "place2" -> callback.captured.onSuccess(listOf(placePhoto2))
            }
        }

        // Because it is private method
        val method = ExplorePlacePresenter::class.java.getDeclaredMethod(
            "getThumbnail",
            MutableList::class.java,
            PlaceCategory::class.java
        )
        method.isAccessible = true
        method.invoke(presenter, placeIdList, category)

        verify(atLeast = 1) {
            mView.onGetPhotoSuccess(placePhoto1, category)
        }
        verify(atLeast = 1) {
            mView.onGetPhotoSuccess(placePhoto2, category)
        }
    }

    @Test
    fun `getThumbnail no data mView should not call`() {
        val presenter = spyk(mPresenter)
        val placeIdList = mutableListOf("place1", "place2")
        val category = PlaceCategory.ATTRACTIONS
        val callback = slot<ResultListener<List<PlacePhoto>>>()
        every {
            mPlaceRepository.getPlacePhoto(any(), capture(callback))
        } answers { invocation ->
            val placeId = invocation.invocation.args[0] as String

            when (placeId) {
                "place1" -> callback.captured.onSuccess(null)
                "place2" -> callback.captured.onSuccess(null)
            }
        }

        // Because it is private method
        val method = ExplorePlacePresenter::class.java.getDeclaredMethod(
            "getThumbnail",
            MutableList::class.java,
            PlaceCategory::class.java
        )
        method.isAccessible = true
        method.invoke(presenter, placeIdList, category)

        verify {
            mView wasNot called
        }
    }

    @Test
    fun `getThumbnail when repo callback onError mView should not call`() {
        val presenter = spyk(mPresenter)
        val placeIdList = mutableListOf("place1", "place2")
        val category = PlaceCategory.ATTRACTIONS
        val callback = slot<ResultListener<List<PlacePhoto>>>()
        val exception = mockk<Exception>()
        every {
            mPlaceRepository.getPlacePhoto(any(), capture(callback))
        } answers { invocation ->
            val placeId = invocation.invocation.args[0] as String

            when (placeId) {
                "place1" -> callback.captured.onError(exception)
                "place2" -> callback.captured.onError(exception)
            }
        }

        // Because it is private method
        val method = ExplorePlacePresenter::class.java.getDeclaredMethod(
            "getThumbnail",
            MutableList::class.java,
            PlaceCategory::class.java
        )
        method.isAccessible = true
        method.invoke(presenter, placeIdList, category)

        verify {
            mView wasNot called
        }
    }
}
