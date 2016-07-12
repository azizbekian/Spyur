package com.azizbekian.spyur.misc;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.azizbekian.spyur.listener.IRecyclerLoadingListener;

/**
 * A scroll listener for {@link RecyclerView} to load more items as you approach the end.
 * <p>
 * Adapted from <a href="https://gist.github.com/ssinss/e06f12ef66c51252563e">WoongBi Kim</a>
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    /**
     * The minimum number of items remaining before some action should be performed.
     */
    private static final int VISIBLE_THRESHOLD = 5;

    private final LinearLayoutManager layoutManager;
    private final IRecyclerLoadingListener isLoading;
    private final int visibleThreshold;

    public InfiniteScrollListener(@NonNull LinearLayoutManager layoutManager,
                                  @NonNull IRecyclerLoadingListener isLoading,
                                  int visibleThreshold) {

        this.layoutManager = layoutManager;
        this.isLoading = isLoading;
        this.visibleThreshold = visibleThreshold;
    }

    @SuppressWarnings("unused")
    public InfiniteScrollListener(@NonNull LinearLayoutManager layoutManager,
                                  @NonNull IRecyclerLoadingListener isLoading) {

        this(layoutManager, isLoading, VISIBLE_THRESHOLD);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        // bail out if scrolling upward or already loading data
        if (dy < 0 || isLoading.isDataLoading()) return;

        final int visibleItemCount = recyclerView.getChildCount();
        final int totalItemCount = layoutManager.getItemCount();
        final int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            onLoadMore();
        }
    }

    /**
     * Is called, when the {@code threshold} has been passed, meaning that a new data should be
     * loaded.
     */
    public abstract void onLoadMore();

}
