package com.incentive.yellowpages.misc

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * A scroll listener for [RecyclerView] to load more items as you approach the end.
 *
 *
 * Adapted from [WoongBi Kim](https://gist.github.com/ssinss/e06f12ef66c51252563e)
 */
abstract class InfiniteScrollListener(private val layoutManager: LinearLayoutManager,
                                      private val loadMoreListener: LoadMoreListener,
                                      private val visibleThreshold: Int) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        // bail out if scrolling upward or already loading data
        if (dy < 0 || loadMoreListener.isDataLoading()) return

        val visibleItemCount = recyclerView!!.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        if (totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
            onLoadMore()
        }
    }

    /**
     * Is called, when the `threshold` has been passed, meaning that a new data should be
     * loaded.
     */
    abstract fun onLoadMore()

}
