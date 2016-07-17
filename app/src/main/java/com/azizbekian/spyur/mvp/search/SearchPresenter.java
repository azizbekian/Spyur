package com.azizbekian.spyur.mvp.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.adapter.SearchAdapter;
import com.azizbekian.spyur.adapter.SearchAdapter.ISearchItemClicked;
import com.azizbekian.spyur.listener.IRecyclerLoadingListener;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.azizbekian.spyur.rest.SpyurApi;
import com.azizbekian.spyur.utils.LogUtils;
import com.azizbekian.spyur.utils.NetworkUtils;

import java.util.List;

import com.azizbekian.spyur.mvp.SimplePresenter;
import com.azizbekian.spyur.utils.RxUtils;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
@SuppressWarnings("ConstantConditions")
public class SearchPresenter extends SimplePresenter implements SearchContract.Presenter {

    private static final int IME_OPTIONS = EditorInfo.IME_ACTION_SEARCH |
            EditorInfo.IME_FLAG_NO_EXTRACT_UI |
            EditorInfo.IME_FLAG_NO_FULLSCREEN;

    /**
     * The number of items remaining before more info should be loaded
     */
    private static final int THRESHOLD_LOAD_MORE = 10;

    @Nullable SearchContract.View mView;
    @NonNull SearchContract.Model mModel;

    private int mSearchPage = SpyurApi.SearchApi.INITIAL_SEARCH_PAGE - 1;
    private boolean mIsLoading;
    private boolean mHasNextPage = true;
    private SpannableStringBuilder mNoInternetString;
    private SearchAdapter mAdapter;
    private Transition mTransition = new AutoTransition();
    /**
     * If {@link #clearResults()} has been called recently and no other changes have
     * been made, there's no point to perform that function again.
     */
    private boolean mCleanedRecently;

    public SearchPresenter(@NonNull SearchContract.View view) {
        this.mView = view;

        mModel = new SearchModel();
    }

    @Override public void create() {
        super.create();

        if (verifyViewNotNull()) {
            Context appContext = SpyurApplication.getContext();
            mView.setupSearchView(appContext.getString(R.string.hint_search),
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS, IME_OPTIONS, searchViewQueryTextEvent -> {

                        String currentQuery = searchViewQueryTextEvent.queryText().toString();
                        if (searchViewQueryTextEvent.isSubmitted()) {
                            if (!TextUtils.isEmpty(currentQuery)) {
                                mCleanedRecently = false;
                                clearResults();
                                performSearch();
                            } else if (!mCleanedRecently) clearResults();
                            if (verifyViewNotNull()) mView.searchViewShowIme(false);
                        } else {
                            if (!mCleanedRecently || TextUtils.isEmpty(currentQuery)) {
                                clearResults();
                            }
                        }
                    });

            mView.searchViewFocus(true);
            mView.searchViewShowIme(true);

            IRecyclerLoadingListener recyclerLoadingListener = () -> mIsLoading;
            mAdapter = new SearchAdapter(recyclerLoadingListener,
                    mView instanceof ISearchItemClicked ? ((ISearchItemClicked) mView) : null);

            mView.setupRecyclerView(mView.provideLayoutManager(), mAdapter, recyclerLoadingListener,
                    THRESHOLD_LOAD_MORE);
        }
    }

    @Override public void performSearch() {
        if (NetworkUtils.isConnected(SpyurApplication.getContext()) && verifyViewNotNull()) {
            if (mHasNextPage) {
                mIsLoading = true;


                Subscription s = mModel.search(mSearchPage + 1, mView.getQuery())
                        .compose(RxUtils.applyIOtoMainThreadSchedulers())
                        .subscribe(new Subscriber<SearchResponse>() {
                            @Override public void onCompleted() {

                            }

                            @Override public void onError(Throwable e) {
                                mIsLoading = false;
                                LogUtils.e("#125623 " + e.getMessage());
                            }

                            @Override public void onNext(SearchResponse searchResponse) {

                                mIsLoading = false;

                                if (verifyViewNotNull()) mView.showProgress(false);

                                if (null != searchResponse) {
                                    List<SearchItem> data = searchResponse.getSearchItems();
                                    mHasNextPage = searchResponse.hasNext();
                                    if (!data.isEmpty()) {
                                        ++mSearchPage;

                                        if (verifyViewNotNull()) {
                                            if (mAdapter.isEmpty()) {
                                                mView.beginTransition(mTransition);
                                                mView.showResults(true);
                                                mAdapter.setData(data);
                                            } else mAdapter.addData(data);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        if (verifyViewNotNull()) {
                                            mView.showNoResultText(v -> {
                                                mView.setSearchViewQuery("", false);
                                                mView.searchViewFocus(true);
                                                mView.searchViewShowIme(true);
                                            }, getNoInternetString());
                                        }
                                    }
                                }
                            }
                        });
                mView.showProgress(true);
                mView.delegateAddSubscription(s);
            }
        } else {
            if (verifyViewNotNull()) mView.showNoInternetMessage(true);
        }
    }

    @Override public int provideNoInternetMessageRes() {
        return R.string.message_no_internet;
    }

    @Override public boolean onBackPressed() {
        if (mAdapter.isEmpty()) return false;

        clearResults();
        if (verifyViewNotNull()) {
            mView.setSearchViewQuery("", false);
            mView.searchViewFocus(true);
            mView.searchViewShowIme(true);
        }
        return true;

    }

    @Override public void clearResults() {
        mCleanedRecently = true;
        mSearchPage = SpyurApi.SearchApi.INITIAL_SEARCH_PAGE - 1;
        mHasNextPage = true;
        mAdapter.clear();
        if (verifyViewNotNull()) {
            mView.delegateUnsubscribe();
            mView.beginTransition(mTransition);
            mView.showResults(false);
            mView.showProgress(false);
            mView.hideNoResultText();
        }
    }

    @Override public void newIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query) && verifyViewNotNull()) {
                mView.setSearchViewQuery(query, false);
                clearResults();
                performSearch();
            }
        }
    }

    @Override
    public void dispatchItemClicked(View logo, SearchItem searchItem, int position) {
        if (!verifyViewNotNull()) return;

        mView.searchViewShowIme(false);
        mView.searchViewFocus(false);
        mView.launchListing(logo, searchItem, position);
    }

    private SpannableStringBuilder getNoInternetString() {
        if (!verifyViewNotNull()) return new SpannableStringBuilder();
        if (null == mNoInternetString) {
            String message = String.format(SpyurApplication.getContext()
                    .getString(R.string.message_no_search_results), mView.getQuery());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNoInternetString = ssb;
        }
        return mNoInternetString;
    }

    /**
     * Verifies, that the view, that this presenter is attached to, is not null.
     *
     * @return True, if the view is not null. False otherwise.
     */
    private boolean verifyViewNotNull() {
        return null != mView;
    }

}
