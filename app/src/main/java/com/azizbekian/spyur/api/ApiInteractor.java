package com.azizbekian.spyur.api;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;

import retrofit2.Call;
import rx.Observable;

/**
 * Created on Jul 13, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public interface ApiInteractor {

    /**
     * Performs a search and return {@link SearchResponse}.
     *
     * @param page A value starting from 1 (inclusive).
     */
    Observable<SearchResponse> search(@IntRange(from = 1, to = Integer.MAX_VALUE) int page,
                                      @NonNull String query);

    /**
     * Fetches the detail info of a listing with URL {@code href}.
     *
     * @param href The url, where the listing is located.
     */
    Observable<ListingResponse> getListing(@NonNull String href);

}
