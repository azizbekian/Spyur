package com.incentive.yellowpages.ui.main

import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.transition.Transition
import android.view.View
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.LoadMoreListener
import com.incentive.yellowpages.ui.base.BaseContract
import com.jakewharton.rxbinding.widget.SearchViewQueryTextEvent
import rx.functions.Action1

interface MainView : BaseContract.View {

    fun showProgress(show: Boolean)

    fun showNoInternetMessage(show: Boolean)

    fun showNoResultText(listener: android.view.View.OnClickListener, ssb: SpannableStringBuilder)

    fun hideNoResultText()

    fun showResults(show: Boolean)

    fun setupSearchView(queryHint: String, inputType: Int, imeOptions: Int,
                        queryTextEventAction: Action1<SearchViewQueryTextEvent>)

    fun setupRecyclerView(adapter: RecyclerView.Adapter<*>,
                          loadMoreListener: LoadMoreListener,
                          threshold: Int)

    fun beginTransition(transition: Transition)

    fun setSearchViewQuery(query: String, submit: Boolean)

    fun focusSearchView()

    fun clearSearchViewFocus()

    fun showKeyboard()

    fun hideKeyboard()

    fun getQuery(): String

    fun launchListing(logo: android.view.View, searchItem: SearchResponse.SearchItem, position: Int)

    fun findViewByTag(tag: Any): View

    fun postponeTransition()

    fun layoutRecyclerScrollIfNeededAndContinueTransition(position: Int)

}