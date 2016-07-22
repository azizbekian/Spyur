package com.azizbekian.spyur.mvp.listing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SharedElementCallback;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.TransitionRes;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.mvp.BaseContract;
import com.azizbekian.spyur.utils.AnimUtils;
import com.bumptech.glide.request.RequestListener;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

import java.util.List;

import rx.Observable;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingContract {

    public interface View extends BaseContract.RxView {

        @CheckResult int[] measureLogoBackground(int widthMode, int heightMode);

        void setLogoBackgroundColor(int color);

        @CheckResult Animator createCircularRevealAnimator(int revealStartX, int revealStartY,
                                                           int startRadius, int finalRadius,
                                                           int duration, Interpolator interpolator,
                                                           AnimatorListenerAdapter listenerAdapter);

        void setupCollapsingToolbarLayout(int color);

        void delegateStartPosponedEnterTransition();

        void setLogoBackgroundVisibility(boolean show);

        /**
         * @param show If true shows loading progress bar, otherwise hides.
         */
        void showProgressBar(boolean show, int color);

        void showToast(@StringRes int message);

        void showCardListingLayout(boolean show);

        void setupExecutives(List<String> executives);

        void setupContactInfo(List<ListingResponse.ContactInfo> contactInfoList);

        void setupWebsites(List<String> websiteList);

        void setupListingInSpyur(String listingInSpyur);

        void setupImagesAndVideo(List<String> imagesList, String videoId,
                                 @Nullable Transition transition,
                                 YouTubePlayer.OnInitializedListener youtubeInitializedListener);

        void beginScrollViewTransition(Transition transition);

        @CheckResult Transition inflateTransition(@TransitionRes int transitionId);

        void inflateEmptyListing();

        /**
         * Creates {@link Palette} object from the logo, updates necessary views for appropriate
         * colors. After that activity's postponed enter transition is being started.
         */
        void analyzeLogo(SearchResponse.SearchItem searchItem, RequestListener<String, Bitmap> glideListener);

        /**
         * Shows map icon with animation.
         */
        void animateMapLogo(Runnable endAction);

        /**
         * Retains map icon's coordinates in order to handle click events, because it's beneath toolbar.
         */
        @CheckResult Rect getMapLogoRect();

        void delegateRunOnUiThread(Runnable runnable);

        @CheckResult ConnectivityManager provideConnectivityManager();

        void dispatchMapLogoTouchEvent(MotionEvent motionEvent);

        void setToolbarTouchListener(android.view.View.OnTouchListener onTouchListener);

        void setAppBarStateChangeListener(AppBarStateChangeListener appBarStateChangeListener);

        void setTitle(String title);

        void setCollapsingToolbarTitle(String title);

        void setExpandedTitleColor(@ColorInt int color);

        int[] getLogoCardSizes();

        int[] getHeadLayoutSizes();

        /**
         * Sets the {@code mSliderLayout} auto scroll status.
         *
         * @param autoScroll If true - images would be auto scrolled. False otherwise.
         */
        void setSliderAutoScroll(boolean autoScroll);

        void finish(boolean afterTransition);

        boolean isActivityFinishing();

        void launchMapActivity(ListingResponse listingResponse);

        void showYoutubeErrorReason(YouTubeInitializationResult errorReason, int requestCode);

        void initYoutubeFragment(String youtubeApiKey, YouTubePlayer.OnInitializedListener listener);

        void removeYoutubeFragment();

        void delegatePostponeEnterTransition();

        void delegateSetEnterSharedElementCallback(SharedElementCallback callback);

        void addListenerToEnterTransition();
    }

    public interface Presenter extends BaseContract.Presenter {

        void imageReady(Bitmap resource, int[] logoCardLocationOnScreen,
                        int logoCardWidth, int logoCardHeight,
                        int headerLayoutWidth, int headerLayoutHeight);

        void startRevealAnimation();

        void retrieveListingData();

        boolean allowToFinish();

        void dispatchLogoClick();
    }

    public interface Model extends BaseContract.Model {
        Observable<ListingResponse> getListing(String href);
    }

}
