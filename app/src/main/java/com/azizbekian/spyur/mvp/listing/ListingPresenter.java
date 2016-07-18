package com.azizbekian.spyur.mvp.listing;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.view.View;

import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.model.SearchResponse;
import com.azizbekian.spyur.mvp.SimplePresenter;
import com.azizbekian.spyur.utils.AnimUtils;

import java.util.List;
import java.util.Map;

import static com.azizbekian.spyur.listener.AppBarStateChangeListener.EXPANDED;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingPresenter extends SimplePresenter implements ListingContract.Presenter {

    public static final String EXTRA_SEARCH_ITEM = "extra_search_item";

    @Nullable ListingContract.View mView;
    private ListingContract.Model mModel = new ListingModel();
    private SearchResponse.SearchItem mSearchItem;

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
    }

    @Override public SharedElementCallback provideSharedElementCallback() {
        return mReturnSharedElementCallback;
    }

    @Override public AnimUtils.TransitionListenerAdapter provideTransitionListenerAdapter() {
        return mOnTransitionEndListener;
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

    private final AnimUtils.TransitionListenerAdapter mOnTransitionEndListener
            = new AnimUtils.TransitionListenerAdapter() {
        @Override
        public void onTransitionEnd(Transition transition) {
            getWindow().getEnterTransition().removeListener(this);
            if (null != mBackgroundRevealAnimator && !mBackgroundRevealAnimator.isStarted()) {
                mBackgroundRevealAnimator.start();
            }
            retrieveData(mSearchItem.getHref());
        }
    };
}
