package com.example.travenor.screen.search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import com.example.travenor.data.model.photo.PlacePhoto
import com.example.travenor.data.model.place.Place
import com.example.travenor.data.repository.PlaceRepositoryImpl
import com.example.travenor.data.source.local.PlaceExploreLocalSource
import com.example.travenor.data.source.local.PlaceLocalDataSource
import com.example.travenor.data.source.remote.PlaceRemoteDataSource
import com.example.travenor.databinding.FragmentSearchBinding
import com.example.travenor.screen.detail.DetailActivity
import com.example.travenor.screen.search.adapter.RecentSearchAdapter
import com.example.travenor.screen.search.adapter.SearchResultAdapter
import com.example.travenor.utils.base.BaseFragment

@Suppress("TooManyFunctions")
class SearchFragment :
    BaseFragment<FragmentSearchBinding>(),
    SearchContract.View,
    RecentSearchAdapter.OnItemClickListener,
    SearchResultAdapter.OnItemClickListener {

    private val mRecentSearchAdapter: RecentSearchAdapter by lazy { RecentSearchAdapter() }
    private val mSearchResultAdapter: SearchResultAdapter by lazy { SearchResultAdapter() }
    private var mPresenter: SearchPresenter? = null

    override fun inflateViewBinding(inflater: LayoutInflater): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater)
    }

    override fun initData() {
        val remoteDataSource = PlaceRemoteDataSource.getInstance()
        val localDataSource = PlaceLocalDataSource.getInstance(requireContext())
        val localExploreDataSource = PlaceExploreLocalSource.getInstance(requireContext())

        val placeRepository = PlaceRepositoryImpl.getInstance(
            remoteDataSource,
            localDataSource,
            localExploreDataSource
        )

        mPresenter = SearchPresenter(placeRepository)
        mPresenter?.setView(this)

        mPresenter?.getRecentSearchString()
    }

    override fun initView() {
        setLayoutBelowSystemBar()
        toggleDisplayShowRecentSearch(true)

        viewBinding.recyclerViewRecentSearchText.adapter = mRecentSearchAdapter
        mRecentSearchAdapter.setOnItemClickListener(this)

        viewBinding.recyclerViewResultSearchPlace.adapter = mSearchResultAdapter
        mSearchResultAdapter.setOnItemClickListener(this)

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                doSearch(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Display recent search
                toggleDisplayShowRecentSearch(true)
                toggleDisplayShowResultSearch(false)
                toggleDisplayLoading(false)

                if (newText.isNullOrEmpty()) {
                    // No query text
                    return true
                }

                mRecentSearchAdapter.filter.filter(newText)
                return true
            }
        })

        toggleDisplayLoading(false)
        toggleDisplayShowResultSearch(false)
        toggleDisplayShowRecentSearch(false)
    }

    override fun onStop() {
        super.onStop()

        // Save recent search string before activity stopped
        mPresenter?.saveRecentSearchString(mRecentSearchAdapter.getRecentSearchList())
    }

    override fun onGetRecentSearchStringSuccess(data: List<String>) {
        mRecentSearchAdapter.updateList(data)

        // Display recent search
        toggleDisplayShowRecentSearch(true)
        toggleDisplayLoading(false)
        toggleDisplayShowResultSearch(false)
    }

    override fun onSearchPlaceSuccess(data: List<Place>) {
        // Display no result
        toggleDisplayShowRecentSearch(false)
        toggleDisplayLoading(false)
        toggleDisplayShowResultSearch(true)

        if (data.isEmpty()) {
            mSearchResultAdapter.clearSearchResultList()
        } else {
            mSearchResultAdapter.setSearchResultList(data)
            data.forEach {
                mPresenter?.getPlaceThumbnailPhoto(it.locationId)
            }
        }
    }

    override fun onSearchPlaceFail(message: String) {
        mSearchResultAdapter.clearSearchResultList()

        // Display no result
        toggleDisplayShowRecentSearch(false)
        toggleDisplayLoading(false)
        toggleDisplayShowResultSearch(true)
    }

    override fun onGetPlacePhotoSuccess(placeId: String, data: List<PlacePhoto>) {
        if (data.isNotEmpty()) {
            mSearchResultAdapter.setThumbnailPhotoMap(placeId, data.first())
        }
    }

    override fun onRecentSearchTextClick(text: String) {
        viewBinding.searchView.setQuery(text, false)
        doSearch(text)
    }

    override fun onPlaceClick(placeId: String) {
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra(DetailActivity.KEY_INTENT_PLACE_ID, placeId)
        startActivity(intent)
    }

    private fun doSearch(query: String) {
        // Search
        mPresenter?.onSearchPlace(query, null)

        // close soft keyboard
        hideKeyboard()

        // Display searching
        toggleDisplayLoading(true)
        toggleDisplayShowRecentSearch(false)
        toggleDisplayShowResultSearch(false)

        // Add recent search string
        mRecentSearchAdapter.addRecentSearchText(query)
    }

    private fun hideKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity?.currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun toggleDisplayLoading(isDisplay: Boolean) {
        if (isDisplay) {
            viewBinding.progressbar.visibility = View.VISIBLE
        } else {
            viewBinding.progressbar.visibility = View.GONE
        }
    }

    private fun toggleDisplayShowRecentSearch(isShow: Boolean) {
        if (isShow) {
            viewBinding.groupRecentSearchText.visibility = View.VISIBLE
        } else {
            viewBinding.groupRecentSearchText.visibility = View.GONE
        }
    }

    private fun toggleDisplayShowResultSearch(isShow: Boolean) {
        if (isShow) {
            viewBinding.groupResult.visibility = View.VISIBLE
        } else {
            viewBinding.groupResult.visibility = View.GONE
        }
    }

    private fun setLayoutBelowSystemBar() {
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.layoutAppBar) { v, insets ->
            val layoutParams = v.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = insets.systemWindowInsetTop
            v.layoutParams = layoutParams
            insets
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
