package com.azizbekian.spyur.listener;

import android.view.View;

import com.azizbekian.spyur.model.SearchResponse;

/**
 * Created by CargoMatrix, Inc. on May 02, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public interface ISearchItemClicked {

    /**
     * Called, when the item has been clicked.
     */
    void onItemClicked(View logo, SearchResponse.SearchItem searchItem, int position);
}
