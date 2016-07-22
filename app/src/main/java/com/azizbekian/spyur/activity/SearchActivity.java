package com.azizbekian.spyur.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.activity.base.RxBaseActivity;
import com.azizbekian.spyur.adapter.SearchAdapter.ISearchItemClicked;
import com.azizbekian.spyur.listener.IRecyclerLoadingListener;
import com.azizbekian.spyur.misc.InfiniteScrollListener;
import com.azizbekian.spyur.misc.NpaLinearLayoutManager;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.azizbekian.spyur.mvp.search.SearchContract;
import com.azizbekian.spyur.mvp.search.SearchPresenter;
import com.azizbekian.spyur.utils.ImeUtils;
import com.jakewharton.rxbinding.widget.RxSearchView;
import com.jakewharton.rxbinding.widget.SearchViewQueryTextEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchActivity extends RxBaseActivity implements SearchContract.View,
        ISearchItemClicked {

    @BindView(R.id.container) ViewGroup mContainer;
    @BindView(R.id.search_view) SearchView mSearchView;
    @BindView(android.R.id.empty) ProgressBar mProgress;
    @BindView(R.id.stub_no_search_results) ViewStub mNoSearchResultViewStub;
    @BindView(R.id.search_recycler) RecyclerView mResults;

    private TextView mNoResults;
    private Toast mNoInternetToast;
    private SearchContract.Presenter mPresenter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Spyur_Search);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mPresenter = new SearchPresenter(this);
        mPresenter.create();

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mPresenter.newIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (!mPresenter.onBackPressed()) moveTaskToBack(true);
    }

    @Override
    public void setupRecyclerView(LinearLayoutManager layoutManager, RecyclerView.Adapter adapter,
                                  IRecyclerLoadingListener recyclerLoadingListener, int threshold) {

        mResults.setHasFixedSize(true);
        mResults.setLayoutManager(layoutManager);
        mResults.setAdapter(adapter);
        mResults.addOnScrollListener(new InfiniteScrollListener(layoutManager, recyclerLoadingListener,
                threshold) {
            @Override public void onLoadMore() {
                mPresenter.performSearch();
            }
        });
    }

    @Override public LinearLayoutManager provideLayoutManager() {
        if (null == mLayoutManager) mLayoutManager = new NpaLinearLayoutManager(this);
        return mLayoutManager;
    }

    @Override
    public void setupSearchView(String queryHint, int inputType, int imeOptions,
                                Action1<SearchViewQueryTextEvent> queryTextEventAction1) {

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setQueryHint(queryHint);
        mSearchView.setInputType(inputType);
        mSearchView.setImeOptions(mSearchView.getImeOptions() | imeOptions);

        RxSearchView
                .queryTextChangeEvents(mSearchView)
                .skip(1) /* skipping the first emission, which happens upon subscribing */
                .subscribe(queryTextEventAction1);
    }

    @Override
    public String getQuery() {
        return mSearchView.getQuery().toString();
    }

    @Override public void showProgress(boolean show) {
        mProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void delegateAddSubscription(Subscription s) {
        addSubscription(s);
    }

    @Override public void showNoInternetMessage(boolean show) {
        if (null == mNoInternetToast) {
            mNoInternetToast = Toast.makeText(this, mPresenter.provideNoInternetMessageRes(),
                    Toast.LENGTH_SHORT);
        }
        mNoInternetToast.show();
    }

    @Override public void showNoResultText(View.OnClickListener listener, SpannableStringBuilder ssb) {
        if (mNoResults == null) {
            mNoResults = (TextView) (mNoSearchResultViewStub).inflate();
            mNoResults.setOnClickListener(listener);
        }
        mNoResults.setText(ssb);
        mNoResults.setVisibility(View.VISIBLE);
    }

    @Override public void hideNoResultText() {
        if (null == mNoResults) return;
        mNoResults.setVisibility(View.GONE);
    }

    @Override
    public void onItemClicked(View logo, SearchItem searchItem, int position) {
        mPresenter.dispatchItemClicked(logo, searchItem, position);
    }

    @Override public void showResults(boolean show) {
        mResults.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void beginTransition(Transition transition) {
        TransitionManager.beginDelayedTransition(mContainer, transition);
    }

    @Override public void delegateUnsubscribe() {
        unsubscribe();
    }

    @Override public void setSearchViewQuery(String query, boolean submit) {
        mSearchView.setQuery(query, submit);
    }

    @Override public void searchViewShowIme(boolean show) {
        if (show) ImeUtils.showIme(mSearchView);
        else ImeUtils.hideIme(mSearchView);
    }

    @Override public void searchViewFocus(boolean request) {
        mSearchView.requestFocus();
    }

    @Override
    public void launchListing(View logo, SearchItem searchItem, int position) {
        ListingActivity.launch(this, logo, searchItem, isItemFullyVisible(position));
    }

    /**
     * @return True if the item in position {@code position} is fully visible.
     */
    private boolean isItemFullyVisible(int position) {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition() <= position;
    }

    @Override public boolean delegateHasSubscriptions() {
        return hasSubscriptions();
    }

}
