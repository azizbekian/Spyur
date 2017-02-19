package com.incentive.yellowpages.misc

import android.support.annotation.IntDef
import android.support.design.widget.AppBarLayout

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(EXPANDED, COLLAPSED, IDLE)
    annotation class AppBarState

    @AppBarState private var mCurrentAppBarState = IDLE

    override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
        if (i == 0) {
            if (mCurrentAppBarState != EXPANDED) onStateChanged(appBarLayout, EXPANDED)
            mCurrentAppBarState = EXPANDED
        } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
            if (mCurrentAppBarState != COLLAPSED) onStateChanged(appBarLayout, COLLAPSED)
            mCurrentAppBarState = COLLAPSED
        } else {
            if (mCurrentAppBarState != IDLE) onStateChanged(appBarLayout, IDLE)
            mCurrentAppBarState = IDLE
        }
    }

    abstract fun onStateChanged(appBarLayout: AppBarLayout, @AppBarState appBarState: Long)

    companion object {
        const val EXPANDED: Long = 0
        const val COLLAPSED: Long = 1
        const val IDLE: Long = 2
    }

}