package com.azizbekian.spyur.mvp.search;

import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.transition.Transition;

import com.azizbekian.spyur.listener.IRecyclerLoadingListener;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.jakewharton.rxbinding.widget.SearchViewQueryTextEvent;

import com.azizbekian.spyur.mvp.BaseContract;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public interface SearchContract {

    interface View extends BaseContract.RxView {

        void showProgress(boolean show);

        void showNoInternetMessage(boolean show);

        void showNoResultText(android.view.View.OnClickListener listener, SpannableStringBuilder ssb);

        void hideNoResultText();

        void showResults(boolean show);

        void setupSearchView(String queryHint, int inputType, int imeOptions,
                             Action1<SearchViewQueryTextEvent> queryTextEventAction1);

        void setupRecyclerView(LinearLayoutManager layoutManager,
                               RecyclerView.Adapter adapter,
                               IRecyclerLoadingListener recyclerLoadingListener,
                               int threshold);

        @CheckResult LinearLayoutManager provideLayoutManager();

        void beginTransition(Transition transition);

        void setSearchViewQuery(String query, boolean submit);

        /**
         * @param request If true - request the focus. If false - clears the focus.
         */
        void searchViewFocus(boolean request);

        void searchViewShowIme(boolean show);

        String getQuery();

        void launchListing(android.view.View logo, SearchItem searchItem, int position);
    }

    interface Presenter extends BaseContract.Presenter {

        void performSearch();

        @StringRes int provideNoInternetMessageRes();

        void clearResults();

        void newIntent(Intent intent);

        void dispatchItemClicked(android.view.View logo, SearchItem searchItem, int position);
    }

    interface Model extends BaseContract.Model {

        @CheckResult Observable<SearchResponse> search(int page, String query);
    }
}
