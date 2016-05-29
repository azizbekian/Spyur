package com.azizbekian.spyur.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxSearchView;

import java.util.List;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.adapter.SearchAdapter;
import com.azizbekian.spyur.listener.IRecyclerLoadingListener;
import com.azizbekian.spyur.listener.ISearchItemClicked;
import com.azizbekian.spyur.manager.SpyurManager;
import com.azizbekian.spyur.misc.InfiniteScrollListener;
import com.azizbekian.spyur.misc.NpaLinearLayoutManager;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.rest.SpyurApi;
import com.azizbekian.spyur.utils.ImeUtils;
import com.azizbekian.spyur.utils.NetworkUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchActivity extends AppCompatActivity implements IRecyclerLoadingListener,
        ISearchItemClicked {

    private static final int IME_OPTIONS = EditorInfo.IME_ACTION_SEARCH |
            EditorInfo.IME_FLAG_NO_EXTRACT_UI |
            EditorInfo.IME_FLAG_NO_FULLSCREEN;

    /**
     * The number of items remaining before more info should be loaded
     */
    private static final int LOAD_MORE_THRESHOLD_COUNT = 10;

    @Bind(R.id.container) ViewGroup mContainer;
    @Bind(R.id.scrim) View mScrim;
    @Bind(R.id.search_toolbar) ViewGroup mSearchToolbar;
    @Bind(R.id.search_background) View mSearchBackground;
    @Bind(R.id.search_view) SearchView mSearchView;
    @Bind(R.id.results_container) ViewGroup mResultsContainer;
    @Bind(android.R.id.empty) ProgressBar mProgress;
    @Bind(R.id.stub_no_search_results) ViewStub mNoSearchResultViewStub;
    @Bind(R.id.search_recycler) RecyclerView mResults;

    private TextView mNoResults;
    private LinearLayoutManager mLayoutManager;

    private SpyurManager mManager;
    private SearchAdapter mAdapter;
    private Call<SearchResponse> mCall;

    private Transition mAutoTransition;
    private Toast mNoInternetToast;
    /**
     * If {@link SearchActivity#clearResults()} has been called recently and no other changes have
     * been made, there's no point to perform that function again.
     */
    private boolean mCleanedRecently;
    private boolean mIsLoading;
    private boolean mHasNextPage = true;
    private int mSearchPage = SpyurApi.SearchApi.INITIAL_SEARCH_PAGE - 1;

    @Override
    @SuppressWarnings("all")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Spyur_Search);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mManager = SpyurApplication.getAppComponent().getSpyurManager();
        mAutoTransition = TransitionInflater.from(this).inflateTransition(R.transition.auto);
        mNoInternetToast = Toast.makeText(SearchActivity.this, R.string.message_no_internet, Toast.LENGTH_SHORT);

        setupSearchView();
        setupRecyclerView();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                mSearchView.setQuery(query, false);
                newSearch();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelCall();
    }

    @Override
    public void onBackPressed() {
        if (!mAdapter.isEmpty()) {
            clearResults();
            mSearchView.setQuery("", false);
            mSearchView.requestFocus();
            ImeUtils.showIme(mSearchView);
        } else moveTaskToBack(true);
    }

    private void setupRecyclerView() {
        mAdapter = new SearchAdapter(this, this);
        mLayoutManager = new NpaLinearLayoutManager(this);
        mResults.setHasFixedSize(true);
        mResults.setLayoutManager(mLayoutManager);
        mResults.setAdapter(mAdapter);
        mResults.addOnScrollListener(new InfiniteScrollListener(mLayoutManager, this,
                LOAD_MORE_THRESHOLD_COUNT) {
            @Override
            public void onLoadMore() {
                performSearch();
            }
        });
    }

    @Override
    public boolean isDataLoading() {
        return mIsLoading;
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint(getString(R.string.hint_search));
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mSearchView.setImeOptions(mSearchView.getImeOptions() | IME_OPTIONS);

        RxSearchView
                .queryTextChangeEvents(mSearchView)
                .skip(1) /* skipping the first emission, which is being emitted automatically on subscribing*/
                .subscribe(searchViewQueryTextEvent -> {

                    String currentQuery = searchViewQueryTextEvent.queryText().toString();
                    if (searchViewQueryTextEvent.isSubmitted()) {
                        if (!TextUtils.isEmpty(currentQuery)) {
                            mCleanedRecently = false;
                            SearchActivity.this.newSearch();
                        } else if (!mCleanedRecently) SearchActivity.this.clearResults();
                        ImeUtils.hideIme(mSearchView);
                    } else {
                        if (!mCleanedRecently || TextUtils.isEmpty(currentQuery)) {
                            SearchActivity.this.clearResults();
                        }
                    }
                });

        mSearchView.requestFocus();
        ImeUtils.showIme(mSearchView);
    }

    private void clearResults() {
        mCleanedRecently = true;
        mSearchPage = SpyurApi.SearchApi.INITIAL_SEARCH_PAGE - 1;
        mHasNextPage = true;
        cancelCall();
        mAdapter.clear();
        TransitionManager.beginDelayedTransition(mContainer, mAutoTransition);
        mResults.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }

    private void cancelCall() {
        if (null != mCall) mCall.cancel();
    }

    private String getQuery() {
        return mSearchView.getQuery().toString();
    }

    private void newSearch() {
        clearResults();
        performSearch();
    }

    @SuppressWarnings("StatementWithEmptyBody ")
    private void performSearch() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            if (mHasNextPage) {
                mProgress.setVisibility(View.VISIBLE);
                mIsLoading = true;
                mCall = mManager.search(mSearchPage + 1, getQuery());

                mCall.enqueue(new Callback<SearchResponse>() {
                    @Override
                    public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                        mProgress.setVisibility(View.GONE);

                        mIsLoading = false;
                        SearchResponse searchResponse = response.body();
                        if (null != searchResponse) {
                            List<SearchResponse.SearchItem> data = searchResponse.getSearchItems();
                            mHasNextPage = searchResponse.hasNext();
                            if (!data.isEmpty()) {
                                ++mSearchPage;

                                if (mAdapter.isEmpty()) {
                                    TransitionManager.beginDelayedTransition(mContainer, mAutoTransition);
                                    mResults.setVisibility(View.VISIBLE);
                                    mAdapter.setData(data);
                                } else mAdapter.addData(data);
                                mAdapter.notifyDataSetChanged();
                            } else {
                                setNoResultsVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override

                    public void onFailure(Call<SearchResponse> call, Throwable t) {
                        mIsLoading = false;
                    }
                });
            }
        } else {
            mNoInternetToast.show();
        }
    }

    /**
     * Is responsible for showing "empty" view, when no mResults are fetched with specified query.
     */
    private void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (mNoResults == null) {
                mNoResults = (TextView) (mNoSearchResultViewStub).inflate();
                mNoResults.setOnClickListener(v -> {
                    mSearchView.setQuery("", false);
                    mSearchView.requestFocus();
                    ImeUtils.showIme(mSearchView);
                });
            }
            String message = String.format(getString(R.string.message_no_search_results), mSearchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNoResults.setText(ssb);
        }
        if (mNoResults != null) {
            mNoResults.setVisibility(visibility);
        }
    }

    @Override
    public void onItemClicked(View logo, SearchResponse.SearchItem searchItem, int position) {
        ImeUtils.hideIme(mSearchView);
        mSearchView.clearFocus();
        ListingActivity.launch(this, logo, searchItem, isItemFullyVisible(position));
    }

    /**
     * @return True if the item in position {@code position} is fully visible.
     */
    private boolean isItemFullyVisible(int position) {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition() <= position;
    }
}