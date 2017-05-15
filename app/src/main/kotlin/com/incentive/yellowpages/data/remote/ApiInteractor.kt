package com.incentive.yellowpages.data.remote

import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.LanguageType
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiInteractor @Inject constructor(val searchApi: ApiContract.SearchApi,
                                        val listingApi: ApiContract.ListingApi) : ApiContract.Api {

    @LanguageType private var lang: String

    init {
        when (Locale.getDefault().language) {
            "en" -> lang = ApiContract.EN
            "ru" -> lang = ApiContract.RU
            "hy" -> lang = ApiContract.AM
            else -> lang = ApiContract.EN
        }
    }

    override fun search(page: Int, query: String): Single<SearchResponse> {
        return searchApi.search(lang, page, query)
    }

    override fun getListing(href: String): Single<ListingResponse> {
        return listingApi.getListing(href)
    }
}
