package com.azizbekian.spyur.mvp.search;

import android.support.annotation.NonNull;

import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.model.SearchResponse;

import rx.Observable;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SearchModel implements SearchContract.Model {

    @NonNull private ApiInteractor mApiInteractor;

    public SearchModel() {
        mApiInteractor = SpyurApplication.getComponent().getApiInteractor();
    }

    @Override
    public Observable<SearchResponse> search(int page, String query) {
        return mApiInteractor.search(page, query);
    }

}
