package com.incentive.yellowpages.misc

interface LoadMoreListener {

    /**
     * @return True - if the data is being loaded. False otherwise.
     */
    fun isDataLoading(): Boolean
}
