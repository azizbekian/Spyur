package com.azizbekian.spyur.mvp.listing;

import android.support.annotation.NonNull;

import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.utils.RxUtils;

import rx.Subscriber;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingModel implements ListingContract.Model {

    @NonNull ListingContract.Presenter mPresenter;
    @NonNull ApiInteractor mApiInteractor;

    public ListingModel(@NonNull ListingContract.Presenter presenter) {
        mPresenter = presenter;
        mApiInteractor = SpyurApplication.getComponent().getApiInteractor();
    }

    @Override public void getListing(String href) {
        mPresenter.addSubscription(mApiInteractor.getListing(href)
                .compose(RxUtils.applyIOtoMainThreadSchedulers())
                .subscribe(new Subscriber<ListingResponse>() {
                    @Override public void onCompleted() {

                    }

                    @Override public void onError(Throwable e) {
                        mPresenter.onDataFailure(e);
                    }

                    @Override public void onNext(ListingResponse listingResponse) {
                        mPresenter.onDataSuccess(listingResponse);
                    }
                }));
    }

}
