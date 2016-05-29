package com.azizbekian.spyur.listener;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public interface IRecyclerLoadingListener {

    /**
     * @return True - if the data is being loaded. False otherwise.
     */
    boolean isDataLoading();
}
