package com.incentive.yellowpages.data.remote

import android.support.annotation.IntRange
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.LanguageType
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

class ApiContract {

    interface Api {

        /**
         * Performs a search and return [SearchResponse].

         * @param page A value starting from 1 (inclusive).
         */
        fun search(@IntRange(from = 1, to = Integer.MAX_VALUE.toLong()) page: Int,
                   query: String): Single<SearchResponse>

        /**
         * Fetches the detail info of a listing with URL `href`.

         * @param href The url, where the listing is located.
         */
        fun getListing(href: String): Single<ListingResponse>

    }

    interface SearchApi {

        @GET("{lang}/home/search-{page}/") fun search(@Path("lang") @LanguageType language: String,
                                                      @Path("page") page: Int,
                                                      @Query("company_name") query: String): Single<SearchResponse>

        companion object {

            val INITIAL_SEARCH_PAGE = 1
        }

    }

    interface ListingApi {

        @GET fun getListing(@Url listing: String): Single<ListingResponse>
    }

    companion object {
        @JvmField val BASE = "http://www.spyur.am"
        @JvmField val AM = "am"
        @JvmField val RU = "ru"
        @JvmField val EN = "en"
    }

}
