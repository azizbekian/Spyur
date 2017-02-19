package com.incentive.yellowpages.ui.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.*
import com.incentive.yellowpages.ui.base.BaseActivity
import com.incentive.yellowpages.ui.detail.DetailActivity
import com.jakewharton.rxbinding.widget.RxSearchView
import com.jakewharton.rxbinding.widget.SearchViewQueryTextEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.no_search_results.*
import rx.functions.Action1
import javax.inject.Inject

class MainActivity : BaseActivity(), MainView, MainAdapter.ISearchItemClicked {

    @Inject lateinit var presenter: MainPresenter
    lateinit private var linearLayoutManager: LinearLayoutManager
    private var noInternetToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Search)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityComponent.injectActivityComponent(this)
        presenter.create(this, savedInstanceState, isPortrait = isPortrait(this))
        setExitSharedElementCallback(presenter.getSharedElementCallback())
        presenter.newIntent(intent)
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        presenter.newIntent(intent)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        presenter.activityReenter(resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        presenter.pause()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onBackPressed() {
        if (!presenter.onBackPressed()) moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy(isFinishing)
    }

    override fun setupRecyclerView(adapter: RecyclerView.Adapter<*>,
                                   loadMoreListener: LoadMoreListener, threshold: Int) {

        linearLayoutManager = NpaLinearLayoutManager(this)
        searchRecycler.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            setAdapter(adapter)
            addOnScrollListener(object : InfiniteScrollListener(linearLayoutManager, loadMoreListener, threshold) {
                override fun onLoadMore() {
                    presenter.performSearch(false)
                }
            })
        }
    }

    override fun setupSearchView(queryHint: String, inputType: Int, imeOptions: Int,
                                 queryTextEventAction: Action1<SearchViewQueryTextEvent>) {

        searchView.apply {
            val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setQueryHint(queryHint)
            setInputType(inputType)
            setImeOptions(searchView.imeOptions or imeOptions)
        }

        RxSearchView
                .queryTextChangeEvents(searchView)
                .skip(1) /* skipping the first emission, which happens upon subscribing */
                .subscribe(queryTextEventAction)
    }

    override fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    @SuppressWarnings("all")
    override fun showNoInternetMessage(show: Boolean) {
        if (null == noInternetToast) {
            noInternetToast = Toast.makeText(this, R.string.message_no_internet, Toast.LENGTH_SHORT)
        }
        noInternetToast?.show()
    }

    override fun showNoResultText(listener: View.OnClickListener, ssb: SpannableStringBuilder) {
        if (noSearchResults == null) {
            noSearchResultsStub.inflate()
            noSearchResults.setOnClickListener(listener)
        }
        noSearchResults.text = ssb
        noSearchResults.visibility = View.VISIBLE
    }

    override fun hideNoResultText() {
        if (null == noSearchResults) return
        noSearchResults.visibility = View.GONE
    }

    override fun onItemClicked(logo: View, searchItem: SearchResponse.SearchItem, position: Int) {
        presenter.dispatchItemClicked(logo, searchItem, position)
    }

    override fun showResults(show: Boolean) {
        searchRecycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun beginTransition(transition: Transition) {
        TransitionManager.beginDelayedTransition(container, transition)
    }

    override fun setSearchViewQuery(query: String, submit: Boolean) {
        searchView.setQuery(query, submit)
    }

    override fun focusSearchView() {
        searchView.requestFocus()
    }

    override fun clearSearchViewFocus() {
        searchView.clearFocus()
    }

    override fun showKeyboard() {
        searchView.showIme()
    }

    override fun hideKeyboard() {
        searchView.hideIme()
    }

    override fun launchListing(logo: View, searchItem: SearchResponse.SearchItem, position: Int) {
        DetailActivity.launch(this, logo, searchItem, position)
    }

    override fun getQuery(): String = searchView.query.toString()

    override fun findViewByTag(tag: Any): View = searchRecycler.findViewWithTag(tag)

    override fun postponeTransition() {
        postponeEnterTransition()
    }

    override fun layoutRecyclerScrollIfNeededAndContinueTransition(position: Int) {
        searchRecycler.waitForPreDraw {
            searchRecycler.requestLayout()
            scrollToItemIfNeeded(position)
            startPostponedEnterTransition()
        }
    }

    private fun scrollToItemIfNeeded(position: Int) {
        if (!isItemFullyVisible(position)) searchRecycler.scrollToPosition(position)
    }

    private fun isItemFullyVisible(position: Int): Boolean {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition() <= position &&
                linearLayoutManager.findLastCompletelyVisibleItemPosition() >= position
    }
}