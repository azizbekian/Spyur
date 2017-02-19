package com.incentive.yellowpages.ui.main

import android.app.SearchManager
import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.transition.AutoTransition
import android.view.View
import android.view.inputmethod.EditorInfo
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.DataManager
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.data.remote.ApiContract
import com.incentive.yellowpages.injection.ConfigPersistent
import com.incentive.yellowpages.misc.LoadMoreListener
import com.incentive.yellowpages.misc.isConnected
import com.incentive.yellowpages.ui.base.BaseApplication.Companion.context
import com.incentive.yellowpages.ui.base.BaseContract
import com.incentive.yellowpages.ui.detail.DetailPresenter
import com.incentive.yellowpages.utils.LogUtils
import rx.functions.Action1
import java.util.*
import javax.inject.Inject

@ConfigPersistent
class MainPresenter @Inject constructor(val dataManager: DataManager)
    : BaseContract.BasePresenter<MainView>() {

    companion object {
        val IME_OPTIONS = EditorInfo.IME_ACTION_SEARCH or
                EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                EditorInfo.IME_FLAG_NO_FULLSCREEN

        /**
         * The number of items remaining before more info should be loaded
         */
        private val THRESHOLD_LOAD_MORE = 10

    }

    private var searchedPageIndex = ApiContract.SearchApi.INITIAL_SEARCH_PAGE - 1
    private var isDataLoading = false
    private var hasNextPage = true
    private var adapter: MainAdapter? = null
    private val transition = AutoTransition()
    private val loadMoreListener = object : LoadMoreListener {
        override fun isDataLoading(): Boolean {
            return isDataLoading
        }
    }
    private var reenterBundle: Bundle? = null
    /**
     * If [clearResults] has been called recently and no other changes have
     * been made, there's no point to perform that operation again.
     */
    private var cleanedRecently: Boolean = false
    private var previousQuery: String? = null

    override fun create(view: MainView, savedInstanceState: Bundle?, intent: Intent?,
                        arguments: Bundle?, isPortrait: Boolean) {
        super.create(view, savedInstanceState, intent, arguments, isPortrait)

        if (null == adapter) {
            adapter = MainAdapter(loadMoreListener,
                    if (view is MainAdapter.ISearchItemClicked) view
                    else throw IllegalArgumentException("MainContract.View does not implement " +
                            "MainAdapter.ISearchItemClicked"))
        } else this.view?.showResults(true)

        this.view?.apply {
            setupSearchView(context.getString(R.string.hint_search),
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    IME_OPTIONS,
                    Action1 {
                        val currentQuery = it.queryText().toString()
                        if (it.isSubmitted) {
                            if (!TextUtils.isEmpty(currentQuery) && !Objects.equals(previousQuery,
                                    currentQuery)) {
                                cleanedRecently = false
                                previousQuery = currentQuery
                                clearResults()
                                performSearch()
                            } else if (!cleanedRecently) clearResults()

                            clearSearchViewFocus()
                            hideKeyboard()
                        } else if (!cleanedRecently || TextUtils.isEmpty(currentQuery)) {
                            previousQuery = ""
                            clearResults()
                        }
                    })
            focusSearchView()
            hideKeyboard()
            setupRecyclerView(adapter!!, loadMoreListener, THRESHOLD_LOAD_MORE)
        }
    }

    override fun activityReenter(resultCode: Int, data: Intent?) {
        if (null != data) {
            reenterBundle = Bundle(data.extras)
            view?.apply {
                postponeTransition()
                layoutRecyclerScrollIfNeededAndContinueTransition(reenterBundle!!.
                        getInt(DetailPresenter.EXTRA_SEARCH_ITEM_POSITION))
            }
        }
    }

    fun onDataSuccess(searchResponse: SearchResponse?) {
        isDataLoading = false

        view?.showProgress(false)

        if (null != searchResponse) {
            val data = searchResponse.searchItems
            hasNextPage = searchResponse.hasNext
            if (!data.isEmpty()) {
                ++searchedPageIndex
                view?.apply {
                    if (adapter!!.isEmpty) {
                        beginTransition(transition)
                        showResults(true)
                        adapter!!.data = data
                    } else adapter!!.addData(data)
                    adapter!!.notifyDataSetChanged()
                }
            } else {
                view?.apply {
                    showNoResultText(View.OnClickListener {
                        setSearchViewQuery("", false)
                        focusSearchView()
                        showKeyboard()
                    }, constructNoInternetString())
                }
                adapter!!.data = data
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    fun onDataFailure(e: Throwable) {
        isDataLoading = false
        clearResults()
        if (!context.isConnected()) view?.showNoInternetMessage(true)
        LogUtils.e(e.message as String)
    }

    fun performSearch(showProgressBar: Boolean = true) {
        val isConnected: Boolean = context.isConnected()
        val query = view?.getQuery()
        if (isConnected && hasNextPage && !TextUtils.isEmpty(query)) {
            view?.apply {
                // if we are currently performing a search - unsubscribe from it
                if (isDataLoading) unsubscribe()
                isDataLoading = true
                addDisposable(dataManager.search(searchedPageIndex + 1, getQuery())
                        .subscribe({ onDataSuccess(it) }, { onDataFailure(it) }))
                showProgress(showProgressBar)
            }
        } else if (!isConnected) view?.showNoInternetMessage(true)
    }

    fun onBackPressed(): Boolean {
        if (isDataLoading) unsubscribe()
        else if (adapter!!.isEmpty) return false

        clearResults()
        view?.apply {
            setSearchViewQuery("", false)
            focusSearchView()
            showKeyboard()
        }
        return true
    }

    fun clearResults() {
        cleanedRecently = true
        searchedPageIndex = ApiContract.SearchApi.INITIAL_SEARCH_PAGE - 1
        hasNextPage = true
        val size = adapter!!.data.size
        adapter!!.clear()
        adapter!!.notifyItemRangeRemoved(0, size)
        previousQuery = ""
        view?.apply {
            beginTransition(transition)
            showResults(false)
            showProgress(false)
            hideNoResultText()
        }
    }

    fun newIntent(intent: Intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (!TextUtils.isEmpty(query)) {
                view?.apply {
                    setSearchViewQuery(query, false)
                    clearResults()
                    performSearch()
                }
            }
        }
    }

    fun dispatchItemClicked(logo: View, searchItem: SearchResponse.SearchItem, position: Int) {
        view?.apply {
            hideKeyboard()
            clearSearchViewFocus()
            launchListing(logo, searchItem, position)
        }
    }

    fun getSharedElementCallback(): SharedElementCallback {
        return object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?,
                                             sharedElements: MutableMap<String, View>?) {

                if (null != reenterBundle
                        && reenterBundle!!.containsKey(DetailPresenter.EXTRA_SEARCH_ITEM_POSITION)
                        && null != view) {
                    val newSharedElement = view!!.findViewByTag(reenterBundle!!.
                            getInt(DetailPresenter.EXTRA_SEARCH_ITEM_POSITION))
                    val newTransitionName = context.getString(R.string.transition_logo)
                    names?.clear()
                    names?.add(newTransitionName)
                    sharedElements?.clear()
                    sharedElements?.put(newTransitionName, newSharedElement)
                    reenterBundle = null
                } else {
                    // The activity is exiting
                }
            }
        }
    }

    override fun destroy(isFinishing: Boolean) {
        if (isFinishing) unsubscribe()
    }

    private fun constructNoInternetString(): SpannableStringBuilder {
        view?.apply {
            val message = String.format(context.getString(R.string.message_no_search_results), getQuery())
            val ssb = SpannableStringBuilder(message)
            ssb.setSpan(StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return ssb
        }
        return SpannableStringBuilder()
    }

}

