package com.azizbekian.spyur.listener;

import android.support.annotation.IntDef;
import android.support.design.widget.AppBarLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CargoMatrix, Inc. on May 04, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public static final int EXPANDED = 0;
    public static final int COLLAPSED = 1;
    public static final int IDLE = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EXPANDED, COLLAPSED, IDLE})
    public @interface AppBarState {
    }

    private @AppBarState int mCurrentAppBarState = IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            if (mCurrentAppBarState != EXPANDED) {
                onStateChanged(appBarLayout, EXPANDED);
            }
            mCurrentAppBarState = EXPANDED;
        } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
            if (mCurrentAppBarState != COLLAPSED) {
                onStateChanged(appBarLayout, COLLAPSED);
            }
            mCurrentAppBarState = COLLAPSED;
        } else {
            if (mCurrentAppBarState != IDLE) {
                onStateChanged(appBarLayout, IDLE);
            }
            mCurrentAppBarState = IDLE;
        }
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, @AppBarState int appBarState);
}