package com.azizbekian.spyur.mvp.listing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SharedElementCallback;
import android.graphics.Bitmap;
import android.support.annotation.CheckResult;
import android.support.annotation.StringRes;
import android.view.animation.Interpolator;

import com.azizbekian.spyur.mvp.BaseContract;
import com.azizbekian.spyur.utils.AnimUtils;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingContract {

    public interface View extends BaseContract.RxView {

        @CheckResult
        int[] measureLogoBackground(int widthMode, int heightMode);

        void setLogoBackgroundColor(int color);

        @CheckResult
        Animator createCircularRevealAnimator(int revealStartX, int revealStartY,
                                              int startRadius, int finalRadius,
                                              int duration, Interpolator interpolator,
                                              AnimatorListenerAdapter listenerAdapter);

        void setupCollapsingToolbarLayout(int color);

        void delegateStartPosponedEnterTransition();

        void setLogoBackgroundVisibility(boolean show);

        /**
         * @param show If true shows loading progress bar, otherwise hides.
         */
        void showProgressBar(boolean show);

        void showToast(@StringRes int message);

    }

    public interface Presenter extends BaseContract.Presenter {

        SharedElementCallback provideSharedElementCallback();

        void onResourceReady(Bitmap resource, int[] logoCardLocationOnScreen,
                             int logoCardWidth, int logoCardHeight,
                             int headerLayoutWidth, int headerLayoutHeight);

        void startRevealAnimation();

        void retrieveListingData();
    }

    public interface Model extends BaseContract.Model {

    }

}
