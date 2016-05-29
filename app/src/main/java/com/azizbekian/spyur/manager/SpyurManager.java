package com.azizbekian.spyur.manager;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import java.util.Locale;

import com.azizbekian.spyur.misc.LanguageType;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.rest.SpyurApi;
import com.azizbekian.spyur.rest.SpyurApi.ListingApi;
import com.azizbekian.spyur.rest.SpyurApi.SearchApi;

import retrofit2.Call;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SpyurManager {

    private SearchApi mSearchApi;
    private ListingApi mListingApi;
    private @LanguageType String mLang;

    public SpyurManager(SearchApi searchApi, ListingApi listingApi) {
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

    public Call<SearchResponse> search(@IntRange(from = 1, to = Integer.MAX_VALUE) int page,
                                       @NonNull String query) {
        return mSearchApi.search(mLang, page, query);
    }

    public Call<ListingResponse> getListing(String href) {
        return mListingApi.getListing(href);
    }

}
