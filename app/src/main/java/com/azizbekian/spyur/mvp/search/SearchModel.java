package com.azizbekian.spyur.mvp.search;

import android.support.annotation.NonNull;

import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.utils.RxUtils;

import rx.Subscriber;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchModel implements SearchContract.Model {

    @NonNull private SearchContract.Presenter mPresenter;
    @NonNull private ApiInteractor mApiInteractor;

    public SearchModel(@NonNull SearchContract.Presenter presenter) {
        mPresenter = presenter;
        mApiInteractor = SpyurApplication.getComponent().getApiInteractor();
    }

    @Override
    public void search(int page, String query) {
        mPresenter.addSubscription(mApiInteractor.search(page, query)
                .compose(RxUtils.applyIOtoMainThreadSchedulers())
                .subscribe(new Subscriber<SearchResponse>() {
                    @Override public void onCompleted() {

                    }

                    @Override public void onError(Throwable e) {
                        mPresenter.onDataFailure(e);
                    }

                    @Override public void onNext(SearchResponse searchResponse) {
                        mPresenter.onDataSuccess(searchResponse);
                    }
                }));
    }

}
