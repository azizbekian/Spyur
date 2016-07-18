package com.azizbekian.spyur.mvp.listing;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.model.ListingResponse;

import rx.Observable;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingModel implements ListingContract.Model {

    @NonNull ApiInteractor mApiInteractor;

    public ListingModel() {
        mApiInteractor = SpyurApplication.getComponent().getApiInteractor();
    }

    @CheckResult public Observable<ListingResponse> a(String href) {
        return mApiInteractor.getListing(href);
    }

}
