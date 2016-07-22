package com.azizbekian.spyur.mvp.listing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.MotionEvent;
import android.view.View;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.listener.AppBarStateChangeListener.AppBarState;
import com.azizbekian.spyur.misc.Constants;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.azizbekian.spyur.mvp.SimplePresenter;
import com.azizbekian.spyur.utils.AnimUtils;
import com.azizbekian.spyur.utils.LogUtils;
import com.azizbekian.spyur.utils.NetworkUtils;
import com.azizbekian.spyur.utils.RxUtils;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

import java.util.List;
import java.util.Map;

import rx.Subscriber;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.azizbekian.spyur.listener.AppBarStateChangeListener.EXPANDED;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
@SuppressWarnings("ConstantConditions")
public class ListingPresenter extends SimplePresenter implements ListingContract.Presenter {

    public static final String EXTRA_SEARCH_ITEM = "extra_search_item";
    private static final int DURATION_REVEAL = 1300;
    private static final int YOUTUBE_RECOVERY_DIALOG_REQUEST = 821;

    @Nullable ListingContract.View mView;
    private ListingContract.Model mModel = new ListingModel();
    private SearchItem mSearchItem;
    private ListingResponse mListingResponse;
    private Transition mMainCardAutoTransition;
    private Transition mImagesAndVideoTransition;
    private Rect mMapLogoRect;
    private ConnectivityManager mConnectivityManager;
    private YouTubePlayer mYoutubePlayer;

    private int mPaletteVibrant;
    private int mPrimaryColor;
    private int mPrimaryDarkColor;
    private int mTransparentColor;
    private Animator mBackgroundRevealAnimator;

    /**
     * If true - images and video transition animation have been ended.
     */
    private boolean mIsImagesAndVideoTransitionEnded;

    /**
     * If true - user has pressed back button. Is neccessary particularly for tracking shared element
     * return callback. {@link Activity#isFinishing()} can't be used instead, because it's value is
     * being updated after shared element's return callback.
     */
    private boolean mOnBackPressed;

    /**
     * Tracks whether the content should be downloaded, which was interrupted because of connection
     * loss. If true - the content should be downloaded.
     */
    private boolean mIsRetrievalPending;

    /**
     * Prevents back button press when the initial setup transition is running.
     * <p>
     * If true, drops back button press.
     */
    private boolean mIsAutoTransitionRunning;

    /**
     * Tracks, whether the map icon animation has been shown. If true - the animation hasn't been
     * shown.
     */
    private boolean mIsMapAnimPending;

    /**
     * If true - map's logo coordinates haven't been saved in order to handle clicks.
     */
    private boolean mIsMapIconCoordinatesSavingPending;

    /**
     * Indicates, whether there has been a connectivity loss while retrieving data. If true - the
     * connection has been lost.
     */
    private boolean mIsMonitoringConnectivity;

    private @AppBarState int mAppBarState = EXPANDED;

    public ListingPresenter(@NonNull ListingContract.View view, Bundle extras) {
        this.mView = view;

        mSearchItem = extras.getParcelable(EXTRA_SEARCH_ITEM);

        Context context = SpyurApplication.getContext();
        mPrimaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
        mPrimaryDarkColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        mTransparentColor = ContextCompat.getColor(context, android.R.color.transparent);
    }

    @Override public void create() {
        super.create();

        if (verifyViewNotNull()) {

            mView.delegatePostponeEnterTransition();
            mView.delegateSetEnterSharedElementCallback(mReturnSharedElementCallback);
            mView.addListenerToEnterTransition();

            mView.analyzeLogo(mSearchItem, mGlideListener);
            mMainCardAutoTransition = mView.inflateTransition(R.transition.main_card);
            mMainCardAutoTransition.addListener(new AnimUtils.TransitionListenerAdapter() {

                @Override
                public void onTransitionStart(Transition transition) {
                    mIsAutoTransitionRunning = true;
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    mIsAutoTransitionRunning = false;
                    if (verifyViewNotNull()) {

                        if (null != mListingResponse.images && mListingResponse.images.size() > 0) {
                            mImagesAndVideoTransition = mView.inflateTransition(R.transition.video_card);
                        } else if (!TextUtils.isEmpty(mListingResponse.videoUrl)) {
                            mImagesAndVideoTransition = mView.inflateTransition(R.transition.image_video_card);
                        }
                        if (null != mImagesAndVideoTransition) {
                            mImagesAndVideoTransition.addListener(mImageVideoTransitionListener);
                            mImagesAndVideoTransition.setInterpolator(AnimUtils.
                                    getFastOutSlowInInterpolator(SpyurApplication.getContext()));
                        }

                        mView.setupImagesAndVideo(mListingResponse.images, mListingResponse.videoUrl,
                                mImagesAndVideoTransition, mYoutubeInitializedListener);
                    }
                }
            });
            mConnectivityManager = mView.provideConnectivityManager();
            mView.setToolbarTouchListener(mOnToolbarTouchListener);
            mView.setAppBarStateChangeListener(mAppBarStateChangeListener);
            mView.setTitle(mSearchItem.getTitle());
            mView.setCollapsingToolbarTitle(mSearchItem.getTitle());
            mView.setExpandedTitleColor(mTransparentColor);
        }
    }

    @Override public void resume() {
        super.resume();

        if (mIsRetrievalPending) retrieveListingData();
        if (verifyViewNotNull()) mView.setSliderAutoScroll(true);
    }

    @Override public void pause() {
        super.pause();

        if (mIsMonitoringConnectivity) {
            mConnectivityManager.unregisterNetworkCallback(mConnectivityCallback);
            mIsMonitoringConnectivity = false;
        }

        if (verifyViewNotNull()) {
            mView.delegateUnsubscribe();

            if (mView.isActivityFinishing() && null != mYoutubePlayer) {
                mYoutubePlayer.pause();
                mYoutubePlayer.release();
                mView.removeYoutubeFragment();
            }
        }
    }

    @Override public void stop() {
        super.stop();

        if (verifyViewNotNull()) mView.setSliderAutoScroll(false);
    }

    @Override public boolean onBackPressed() {
        mOnBackPressed = true;
        if (verifyViewNotNull()) mView.finish(true);
        return true;
    }

    @Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == YOUTUBE_RECOVERY_DIALOG_REQUEST && verifyViewNotNull()) {
            mView.initYoutubeFragment(Constants.YOUTUBE_API_KEY, mYoutubeInitializedListener);
            return true;
        }

        return false;
    }

    @Override public boolean allowToFinish() {
        return !mIsAutoTransitionRunning &&
                !(null != mImagesAndVideoTransition && !mIsImagesAndVideoTransitionEnded);
    }

    @Override
    public void imageReady(Bitmap resource, int[] logoCardLocationOnScreen, int logoCardWidth,
                           int logoCardHeight, int headerLayoutWidth, int headerLayoutHeight) {
        Palette.from(resource)
                .generate(palette -> {

                    mPaletteVibrant = palette.getVibrantColor(mPrimaryColor);
                    int revealStartX = logoCardLocationOnScreen[0] + logoCardWidth >> 1;
                    int revealStartY = logoCardLocationOnScreen[1] + logoCardHeight >> 1;

                    int widthMode = makeMeasureSpec(headerLayoutWidth, View.MeasureSpec.EXACTLY);
                    int heightMode = makeMeasureSpec(headerLayoutHeight, View.MeasureSpec.EXACTLY);

                    if (verifyViewNotNull()) {
                        mView.setupCollapsingToolbarLayout(palette.getLightVibrantColor(mPrimaryDarkColor));

                        int[] logoBackgroundSizes = mView.measureLogoBackground(widthMode, heightMode);
                        mView.setLogoBackgroundColor(mPaletteVibrant);
                        int finalRadius = (int) Math.hypot(logoBackgroundSizes[0],
                                logoBackgroundSizes[1]);

                        mBackgroundRevealAnimator = mView.createCircularRevealAnimator(revealStartX,
                                revealStartY, 0, finalRadius, DURATION_REVEAL,
                                AnimUtils.EASE_OUT_CUBIC, mAnimatorListenerAdapter);

                        mView.delegateStartPosponedEnterTransition();
                    }

                });

    }

    @Override public void startRevealAnimation() {
        if (null != mBackgroundRevealAnimator && !mBackgroundRevealAnimator.isStarted()) {
            mBackgroundRevealAnimator.start();
        }
    }

    @Override public void retrieveListingData() {
        if (!verifyViewNotNull()) return;
        if (NetworkUtils.isConnected(SpyurApplication.getContext()) && !mView.delegateHasSubscriptions()) {
            mView.showProgressBar(true, mPaletteVibrant);

            mView.delegateAddSubscription(mModel.getListing(mSearchItem.getHref())
                    .compose(RxUtils.applyIOtoMainThreadSchedulers())
                    .subscribe(new Subscriber<ListingResponse>() {
                        @Override public void onCompleted() {

                        }

                        @Override public void onError(Throwable e) {
                            if (verifyViewNotNull()) mView.showProgressBar(false, mPaletteVibrant);
                            mIsRetrievalPending = false;
                            LogUtils.e("Failure loading listing: " + e.getMessage());
                        }

                        @Override public void onNext(ListingResponse listingResponse) {

                            mIsRetrievalPending = false;
                            mListingResponse = listingResponse;
                            if (verifyViewNotNull()) {
                                if (null != mListingResponse) {
                                    mView.showProgressBar(false, mPaletteVibrant);

                                    if (!isHeaderCardEmpty(listingResponse)) {
                                        mView.setupExecutives(listingResponse.executives);
                                        mView.setupContactInfo(listingResponse.contactInfos);
                                        mView.setupWebsites(listingResponse.websites);
                                        mView.setupListingInSpyur(listingResponse.listingInSpyur);
                                        mView.beginScrollViewTransition(mMainCardAutoTransition);
                                        mView.showCardListingLayout(true);
                                    } else {
                                        mView.beginScrollViewTransition(mMainCardAutoTransition);
                                        mView.inflateEmptyListing();
                                    }
                                    if (listingResponse.hasMapCoordinates) {
                                        if (mAppBarState == EXPANDED) {
                                            mIsMapAnimPending = false;
                                            mView.animateMapLogo(mMapLogoAnimateEndRunnable);
                                        } else mIsMapAnimPending = true;
                                    }
                                } else {
                                    mView.showProgressBar(false, mPaletteVibrant);
                                    LogUtils.e("Null response received when fetching listing.");
                                }
                            }
                        }
                    }));
        } else {
            mView.showProgressBar(false, mPaletteVibrant);
            mView.showToast(R.string.message_no_internet);

            mConnectivityManager.registerNetworkCallback(new NetworkRequest
                            .Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(),
                    mConnectivityCallback);
            mIsRetrievalPending = true;
            mIsMonitoringConnectivity = true;
        }
    }

    /**
     * Verifies, that the view, that this presenter is attached to, is not null.
     *
     * @return True, if the view is not null. False otherwise.
     */
    private boolean verifyViewNotNull() {
        return null != mView;
    }

    /**
     * @return True if the content of the header item is empty. False otherwise.
     */
    private boolean isHeaderCardEmpty(ListingResponse listingResponse) {
        return !(listingResponse.executives.size() > 0
                || listingResponse.contactInfos.size() > 0
                || listingResponse.websites.size() > 0
                || !TextUtils.isEmpty(listingResponse.listingInSpyur));
    }

    /**
     * @return True if {@link CollapsingToolbarLayout} is fully expanded. False otherwise.
     */
    private boolean isAppBarExpanded() {
        return mAppBarState == EXPANDED;
    }

    private final Runnable mMapLogoAnimateEndRunnable = new Runnable() {
        @Override public void run() {
            if (isAppBarExpanded() && verifyViewNotNull()) {
                mMapLogoRect = mView.getMapLogoRect();
                mIsMapIconCoordinatesSavingPending = false;
            } else mIsMapIconCoordinatesSavingPending = true;
        }
    };

    /**
     * Is responsible for handling connectivity changes properly.
     */
    private final ConnectivityManager.NetworkCallback mConnectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            if (verifyViewNotNull()) {
                mView.delegateRunOnUiThread(() -> {
                    if (mIsRetrievalPending) retrieveListingData();
                });
            }
        }

        @Override
        public void onLost(Network network) {
            LogUtils.e("Connection lost");
        }
    };

    /**
     * Tracks for collapse state of {@code collapsingToolbarLayout}. If is expanded animates the
     * logo back to previous activity, otherwise does not perform back shared element transition.
     */
    private final AppBarStateChangeListener mAppBarStateChangeListener
            = new AppBarStateChangeListener() {
        @Override
        public void onStateChanged(AppBarLayout appBarLayout, @AppBarState int
                appBarState) {
            mAppBarState = appBarState;
            if (isAppBarExpanded()) {
                if (verifyViewNotNull()) {
                    if (mIsMapAnimPending) mView.animateMapLogo(mMapLogoAnimateEndRunnable);
                    if (mIsMapIconCoordinatesSavingPending) {
                        mMapLogoRect = mView.getMapLogoRect();
                    }
                }
            }
        }
    };

    /**
     * Dispatches click event from toolbar to map icon, because map icon is beneath the toolbar.
     */
    private final View.OnTouchListener mOnToolbarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (null != mMapLogoRect && (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL)) {
                if (mMapLogoRect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                    if (verifyViewNotNull()) mView.dispatchMapLogoTouchEvent(motionEvent);
                }
            }
            return false;
        }
    };

    private final RequestListener<String, Bitmap> mGlideListener = new RequestListener<String, Bitmap>() {
        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target,
                                   boolean isFirstResource) {
            if (verifyViewNotNull()) mView.delegateStartPosponedEnterTransition();
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {

            if (verifyViewNotNull()) {
                int[] logoCardCoords = mView.getLogoCardSizes();
                int[] headerLayoutCoords = mView.getHeadLayoutSizes();
                imageReady(resource, new int[]{logoCardCoords[0], logoCardCoords[1]},
                        logoCardCoords[2], logoCardCoords[3],
                        headerLayoutCoords[0], headerLayoutCoords[1]);
            }
            return false;
        }
    };

    private final SharedElementCallback mReturnSharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mOnBackPressed && mAppBarState != EXPANDED) {
                // if this callback is fired when leaving activity and if app bar state is not expanded
                // clear the shared view from map, we do not want back transition animation
                names.clear();
                sharedElements.clear();
            }
        }
    };

    private final AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (verifyViewNotNull()) mView.setLogoBackgroundVisibility(true);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mBackgroundRevealAnimator.removeListener(this);
        }
    };

    private final AnimUtils.TransitionListenerAdapter mImageVideoTransitionListener =
            new AnimUtils.TransitionListenerAdapter() {

                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);

                    if (verifyViewNotNull() && !mView.isActivityFinishing()) {
                        mView.setSliderAutoScroll(true);
                    }
                    mIsImagesAndVideoTransitionEnded = true;
                }
            };

    private final YouTubePlayer.OnInitializedListener mYoutubeInitializedListener =
            new YouTubePlayer.OnInitializedListener() {

                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                    YouTubePlayer youTubePlayer, boolean wasRestored) {
                    mYoutubePlayer = youTubePlayer;
                    if (!wasRestored) {
                        youTubePlayer.cueVideo(mListingResponse.videoUrl);
                        youTubePlayer.setShowFullscreenButton(false);
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                    YouTubeInitializationResult errorReason) {

                    if (errorReason.isUserRecoverableError() && verifyViewNotNull()) {
                        mView.showYoutubeErrorReason(errorReason, YOUTUBE_RECOVERY_DIALOG_REQUEST);
                    } else {
                        if (verifyViewNotNull()) mView.showToast(R.string.error_youtube);
                    }
                }
            };

    @Override public void dispatchLogoClick() {
        if (verifyViewNotNull()) mView.launchMapActivity(mListingResponse);
    }

}
