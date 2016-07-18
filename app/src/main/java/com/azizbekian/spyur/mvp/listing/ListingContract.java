package com.azizbekian.spyur.mvp.listing;

import android.app.SharedElementCallback;

import com.azizbekian.spyur.mvp.BaseContract;
import com.azizbekian.spyur.utils.AnimUtils;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingContract {

    public interface View extends BaseContract.RxView {

    }

    public interface Presenter extends BaseContract.Presenter {

        SharedElementCallback provideSharedElementCallback();

        AnimUtils.TransitionListenerAdapter provideTransitionListenerAdapter();
    }

    public interface Model extends BaseContract.Model {

    }

}
