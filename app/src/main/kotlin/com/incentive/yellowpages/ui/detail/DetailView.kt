package com.incentive.yellowpages.ui.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SharedElementCallback
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.support.annotation.CheckResult
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.transition.Transition
import android.view.MotionEvent
import android.view.animation.Interpolator
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.AppBarStateChangeListener
import com.incentive.yellowpages.ui.base.BaseContract
import io.reactivex.functions.Consumer

interface DetailView : BaseContract.View {

    @CheckResult fun measureLogoBackground(widthMode: Int, heightMode: Int): IntArray

    fun setLogoBackgroundColor(color: Int)

    @CheckResult fun createCircularRevealAnimator(revealStartX: Int, revealStartY: Int,
                                                  startRadius: Int, finalRadius: Int,
                                                  duration: Int, interpolator: Interpolator,
                                                  listenerAdapter: AnimatorListenerAdapter): Animator

    fun setupCollapsingToolbarLayout(color: Int)

    fun delegateStartPostponedEnterTransition()

    fun setLogoBackgroundVisibility(show: Boolean)

    /**
     * @param show If true shows loading progress bar, otherwise hides.
     */
    fun showProgressBar(show: Boolean, color: Int)

    fun showToast(@StringRes message: Int)

    fun showCardListingLayout(show: Boolean)

    fun setupListeners()

    fun setupRecycler(adapter: DetailsAdapter)

    fun beginScrollViewTransition(transition: Transition)

    fun inflateEmptyListing()

    /**
     * Creates [Palette] object from the logo, updates necessary views for appropriate
     * colors. After that activity's postponed enter transition is being started.
     */
    fun analyzeLogo(searchItem: SearchResponse.SearchItem, imageSuccess: Consumer<Bitmap?>, imageFailure: Consumer<Exception?>)

    /**
     * Shows map icon with animation.
     */
    fun showMapLogo(endAction: Runnable, withAnimation: Boolean = true)

    /**
     * Retains map icon's coordinates in order to handle click events, because it's beneath toolbar.
     */
    @CheckResult fun getMapLogoRect(): Rect

    fun delegateRunOnUiThread(runnable: Runnable)

    @CheckResult fun provideConnectivityManager(): ConnectivityManager

    fun dispatchMapLogoTouchEvent(motionEvent: MotionEvent)

    fun setToolbarTouchListener(onTouchListener: android.view.View.OnTouchListener)

    fun setAppBarStateChangeListener(appBarStateChangeListener: AppBarStateChangeListener)

    fun setTitle(title: String)

    fun setCollapsingToolbarTitle(title: String)

    fun setExpandedTitleColor(@ColorInt color: Int)

    fun getLogoCardSizes(): IntArray

    fun getHeadLayoutSizes(): IntArray

    fun finish(afterTransition: Boolean)

    fun isActivityFinishing(): Boolean

    fun launchMapActivity(listingResponse: ListingResponse)

    fun launchMapActivity(location: LatLng?)

    fun delegatePostponeEnterTransition()

    fun delegateSetEnterSharedElementCallback(callback: SharedElementCallback)

    fun addListenerToEnterTransition()

}
