package com.azizbekian.spyur.rest;

import com.azizbekian.spyur.misc.LanguageType;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created on May 01, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public interface SpyurApi {

    String ENDPOINT = "http://www.spyur.am";
    String AM = "am";
    String RU = "ru";
    String EN = "en";

    interface SearchApi {

        int INITIAL_SEARCH_PAGE = 1;

        @GET("{lang}/home/search-{page}/") Observable<SearchResponse> search(@Path("lang") @LanguageType String language,
                                                                             @Path("page") int page,
                                                                             @Query("company_name") String query);

    }

    interface ListingApi {

        @GET Observable<ListingResponse> getListing(@Url String listing);
    }

}
