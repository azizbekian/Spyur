package com.azizbekian.spyur.manager;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.misc.LanguageType;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.rest.SpyurApi;

import javax.inject.Inject;

import retrofit2.Call;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SpyurManager implements ApiInteractor {

    private SpyurApi.SearchApi mSearchApi;
    private SpyurApi.ListingApi mListingApi;
    private @LanguageType String mLang;

    @Inject
    public SpyurManager(SpyurApi.SearchApi searchApi, SpyurApi.ListingApi listingApi) {

        mSearchApi = searchApi;
        mListingApi = listingApi;

        switch (Locale.getDefault().getLanguage()) {
            case "en":
                mLang = SpyurApi.EN;
                break;
            case "ru":
                mLang = SpyurApi.RU;
                break;
            case "hy":
                mLang = SpyurApi.AM;
                break;
            default:
                mLang = SpyurApi.EN;
                break;
        }

    }

    @Override
    public Call<SearchResponse> search(@IntRange(from = 1, to = Integer.MAX_VALUE) int page,
                                       @NonNull String query) {

        return mSearchApi.search(mLang, page, query);
    }

    @Override
    public Call<ListingResponse> getListing(@NonNull String href) {
        return mListingApi.getListing(href);
    }

}
