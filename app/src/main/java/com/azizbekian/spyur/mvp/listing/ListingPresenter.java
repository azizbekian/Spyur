package com.azizbekian.spyur.mvp.listing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.view.View;
import android.widget.Toast;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.mvp.SimplePresenter;
import com.azizbekian.spyur.utils.AnimUtils;
import com.azizbekian.spyur.utils.LogUtils;
import com.azizbekian.spyur.utils.NetworkUtils;
import com.azizbekian.spyur.utils.RxUtils;

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

    @Nullable ListingContract.View mView;
    private ListingContract.Model mModel = new ListingModel();
    private SearchResponse.SearchItem mSearchItem;

    private int mPaletteVibrant;
    private int mPrimaryColor;
    private int mPrimaryDarkColor;
    private int mTransparentColor;
    private Animator mBackgroundRevealAnimator;

    /**
     * If true - user has pressed back button. Is neccessary particularly for tracking shared element
     * return callback. {@link Activity#isFinishing()} can't be used instead, because it's value is
     * being updated after shared element's return callback.
     */
    private boolean mOnBackPressed;

    private @AppBarStateChangeListener.AppBarState int mAppBarState = EXPANDED;

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

        if (verifyViewNotNull()) mView.analyzeLogo(mSearchItem);
    }

    @Override
    public void onResourceReady(Bitmap resource, int[] logoCardLocationOnScreen, int logoCardWidth,
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

    @Override public SharedElementCallback provideSharedElementCallback() {
        return mReturnSharedElementCallback;
    }

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

    @Override public void startRevealAnimation() {
        if (null != mBackgroundRevealAnimator && !mBackgroundRevealAnimator.isStarted()) {
            mBackgroundRevealAnimator.start();
        }
    }

    private final AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationStart(Animator animation) {
            if(verifyViewNotNull()) mView.setLogoBackgroundVisibility(true);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mBackgroundRevealAnimator.removeListener(this);
        }
    };

    @Override public void retrieveListingData() {
        if(!verifyViewNotNull()) return;
        if (NetworkUtils.isConnected(SpyurApplication.getContext()) && !mView.delegateHasSubscriptions()) {
            mView.showProgressBar(true);

            addSubscription(apiInteractor.getListing(href)
                    .compose(RxUtils.applyIOtoMainThreadSchedulers())
                    .subscribe(new Subscriber<ListingResponse>() {
                        @Override public void onCompleted() {

                        }

                        @Override public void onError(Throwable e) {
                            showProgressBar(false);
                            // TODO: show message "failure"
                            mIsRetrievalPending = false;
                            LogUtils.e("Failure loading listing: " + e.getMessage());
                        }

                        @Override public void onNext(ListingResponse listingResponse) {
                            mIsRetrievalPending = false;
                            mListingResponse = listingResponse;
                            if (null != mListingResponse) {
                                setupContent(mListingResponse);
                            } else {
                                showProgressBar(false);
                                // TODO: show message "error data"
                                LogUtils.e("Null response received when fetching listing.");
                            }
                        }
                    }));
        } else {
            mView.showProgressBar(false);
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
}
