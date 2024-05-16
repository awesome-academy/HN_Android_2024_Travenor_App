package com.example.travenor.screen.detail

import android.content.Intent
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import com.example.travenor.R
import com.example.travenor.constant.IS_FAVORITE
import com.example.travenor.core.observable.FavoritePlaceObserver
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.repository.PlaceRepositoryImpl
import com.example.travenor.data.source.local.PlaceExploreLocalSource
import com.example.travenor.data.source.local.PlaceLocalDataSource
import com.example.travenor.data.source.remote.PlaceRemoteDataSource
import com.example.travenor.databinding.ActivityDetailBinding
import com.example.travenor.screen.moredetail.MoreDetailActivity
import com.example.travenor.utils.base.BaseActivity
import com.example.travenor.utils.ext.loadImageCenterCrop
import com.google.android.material.bottomsheet.BottomSheetBehavior

@Suppress("TooManyFunctions")
class DetailActivity : BaseActivity(), DetailContract.View, FavoritePlaceObserver {
    private var mPlaceId: String = ""
    private var mPresenter: DetailPresenter? = null
    private var mIsFavorite: Int = IS_FAVORITE
    private lateinit var mBinding: ActivityDetailBinding

    override fun getLayoutRoot(): View {
        mBinding = ActivityDetailBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        setLayoutBelowSystemBar()
        if (!intent.hasExtra(KEY_INTENT_PLACE_ID)) finish()

        mPlaceId = intent.getStringExtra(KEY_INTENT_PLACE_ID).toString()

        val remoteDataSource = PlaceRemoteDataSource.getInstance()
        val localDataSource = PlaceLocalDataSource.getInstance(baseContext)
        val localExploreDataSource = PlaceExploreLocalSource.getInstance(baseContext)

        val placeRepositoryImpl = PlaceRepositoryImpl.getInstance(
            remoteDataSource,
            localDataSource,
            localExploreDataSource
        )

        mPresenter = DetailPresenter(placeRepositoryImpl)
        mPresenter?.setView(this)

        mBinding.buttonBack.setOnClickListener { _ -> finish() }
        mBinding.buttonMoreDetail.setOnClickListener { _ -> openMoreDetailScreen() }
        mBinding.buttonMarkFavorite.setOnClickListener { _ ->
            if (mIsFavorite == IS_FAVORITE) {
                mPresenter?.markNotFavorite(mPlaceId)
            } else {
                mPresenter?.markFavorite(mPlaceId)
            }
        }

        initBottomSheet()

        // Register favorite place change
        placeRepositoryImpl.registerObserver(this)
    }

    private fun initBottomSheet() {
        mBinding.containerBottomSheet.minimumHeight =
            ((Resources.getSystem().displayMetrics.heightPixels) * SIXTEEN_PERCENT).toInt()

        val bottomBehavior = BottomSheetBehavior.from(mBinding.containerBottomSheet)
        bottomBehavior.peekHeight =
            ((Resources.getSystem().displayMetrics.heightPixels) * SIXTEEN_PERCENT).toInt()
        bottomBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun initData() {
        super.initData()
        mPresenter?.getPlaceDetail(mPlaceId)
        mPresenter?.getPlacePhotos(mPlaceId)
    }

    private fun openMoreDetailScreen() {
        val intent = Intent(this, MoreDetailActivity::class.java)
        intent.putExtra(MoreDetailActivity.EXTRA_PLACE_ID, mPlaceId)
        intent.putExtra(
            MoreDetailActivity.EXTRA_ACTIVITY_OPEN_MODE,
            MoreDetailActivity.MODE_DISPLAY_NEARBY_PLACE
        )
        startActivity(intent)
    }

    override fun onGetPlaceDetailSuccess(place: Place) {
        mBinding.textPlaceName.text = place.name
        mBinding.textPlaceDesc.text = place.description
        mBinding.textPlaceAddress.text = place.addressObj.addressString
        mBinding.textPlaceRating.text = place.rating.toString()

        mIsFavorite = place.isFavorite
        if (place.isFavorite == IS_FAVORITE) {
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_unmark_favorite)
            mBinding.buttonMarkFavorite.setImageDrawable(drawable)
        } else {
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_mark_favorite)
            mBinding.buttonMarkFavorite.setImageDrawable(drawable)
        }
        mBinding.buttonMarkFavorite.isClickable = true
    }

    override fun onGetPlaceDetailFail(e: Exception) {
        Toast.makeText(
            this,
            getString(R.string.detail_get_detail_fail_toast),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    override fun onGetPhotoSuccess(photos: PlacePhoto) {
        mBinding.imagePlaceHeader.loadImageCenterCrop(photos.imageList.original?.url.toString())
    }

    override fun onMarkFavoriteSuccess() {
        val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_unmark_favorite)
        mBinding.buttonMarkFavorite.setImageDrawable(drawable)
    }

    override fun onMarkNotFavoriteSuccess() {
        val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_mark_favorite)
        mBinding.buttonMarkFavorite.setImageDrawable(drawable)
    }

    private fun setLayoutBelowSystemBar() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.layoutAppBar) { v, insets ->
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = insets.systemWindowInsetTop
            v.layoutParams = layoutParams
            insets
        }
    }

    companion object {
        const val KEY_INTENT_PLACE_ID = "activity.detail.key.intent_place_id"
        private const val SIXTEEN_PERCENT = 0.6
    }

    /**
     * Favorite place status change, refresh mark favorite button icon
     */
    override fun onFavoritePlaceChange(placeId: String, isFavorite: Int) {
        mIsFavorite = isFavorite
        if (mIsFavorite == IS_FAVORITE) {
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_unmark_favorite)
            mBinding.buttonMarkFavorite.setImageDrawable(drawable)
        } else {
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_mark_favorite)
            mBinding.buttonMarkFavorite.setImageDrawable(drawable)
        }
    }
}
