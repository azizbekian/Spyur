package com.incentive.yellowpages.data

import android.support.annotation.CheckResult
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.data.remote.ApiInteractor
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager
@Inject
constructor(private val apiInteractor: ApiInteractor) {

    @CheckResult
    fun search(page: Int, query: String): Single<SearchResponse> {
        return apiInteractor.search(page, query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    @CheckResult
    fun getListing(href: String): Single<ListingResponse> {
        return apiInteractor.getListing(href)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}
