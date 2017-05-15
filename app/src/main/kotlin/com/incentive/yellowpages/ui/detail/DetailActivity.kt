package com.incentive.yellowpages.ui.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.transition.Transition
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Interpolator
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.misc.AppBarStateChangeListener
import com.incentive.yellowpages.misc.ImageLoader
import com.incentive.yellowpages.ui.base.BaseActivity
import com.incentive.yellowpages.ui.detail.maps.MapsActivity
import com.incentive.yellowpages.utils.AnimUtils
import com.incentive.yellowpages.utils.TransitionUtils
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.card_listing.*
import javax.inject.Inject

class DetailActivity : BaseActivity(), DetailView {

    companion object {

        /**
         * @param activity                     The activity that launches {@link ListingActivity}
         * @param transitionView               The view that is being transitioned
         * @param searchItem                   An instance of  {@link SearchItem}, which comprises the data,
         *                                     that would be populated in {@link ListingActivity}
         */
        fun launch(activity: Activity, transitionView: View, searchItem: SearchResponse.SearchItem,
                   position: Int) {
            val intent = Intent(activity, DetailActivity::class.java)
            intent.putExtra(DetailPresenter.EXTRA_SEARCH_ITEM, searchItem)
            intent.putExtra(DetailPresenter.EXTRA_SEARCH_ITEM_POSITION, position)
            val pairs = TransitionUtils.createSafeTransitionParticipants(activity, true,
                    Pair.create(transitionView, activity.getString(R.string.transition_logo)))

            val sceneTransitionAnimation = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs)
            ActivityCompat.startActivity(activity, intent, sceneTransitionAnimation.toBundle())
        }

    }

    @Inject lateinit var presenter: DetailPresenter
    @Inject lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        activityComponent.injectActivityComponent(this)
        presenter.create(this, savedInstanceState = savedInstanceState, intent = intent)
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (!presenter.activityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    override fun finish(afterTransition: Boolean) {
        if (afterTransition) ActivityCompat.finishAfterTransition(this)
        else finish()
    }

    override fun finishAfterTransition() {
        if (!presenter.allowToFinish()) return
        val data = Intent()
        data.putExtra(DetailPresenter.EXTRA_SEARCH_ITEM_POSITION, presenter.getPositionInAdapter())
        setResult(RESULT_OK, data)
        super.finishAfterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy(isFinishing)
    }

    override fun setupRecycler(adapter: DetailsAdapter) {
        mainRecycler.apply {
            layoutManager.isAutoMeasureEnabled = true
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            this.adapter = adapter
        }
    }

    override fun setupListeners() {
        backImageButton.setOnClickListener { onBackPressed() }
        mapLogoImageView.setOnClickListener { presenter.dispatchLogoClick() }
    }

    override fun addListenerToEnterTransition() {
        window.enterTransition.addListener(object : AnimUtils.TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                window.enterTransition.removeListener(this)
                presenter.startRevealAnimation()
                presenter.retrieveListingData()
            }
        })
    }

    override fun delegatePostponeEnterTransition() {
        supportPostponeEnterTransition()
    }

    override fun delegateSetEnterSharedElementCallback(callback: SharedElementCallback) {
        // Using setEnterSharedElementCallback, which is being called for return transition too.
        setEnterSharedElementCallback(callback)
    }

    override fun setTitle(title: String) {
        headerTitle.text = title
    }

    override fun setCollapsingToolbarTitle(title: String) {
        collapsingToolbarLayout.title = title
    }

    override fun setExpandedTitleColor(@ColorInt color: Int) {
        collapsingToolbarLayout.setExpandedTitleColor(color)
    }

    override fun setAppBarStateChangeListener(appBarStateChangeListener: AppBarStateChangeListener) {
        appbarLayout.addOnOffsetChangedListener(appBarStateChangeListener)
    }

    override fun analyzeLogo(searchItem: SearchResponse.SearchItem, imageSuccess: Consumer<Bitmap?>,
                             imageFailure: Consumer<Exception?>) {
        imageLoader.load(searchItem.logo, logoBgImageView, imageSuccess, imageFailure)
    }

    override fun getLogoCardSizes(): IntArray {
        val loc = IntArray(2)
        logoBgImageView.getLocationOnScreen(loc)
        return intArrayOf(loc[0], loc[1], logoCard.width, logoCard.height)
    }

    override fun getHeadLayoutSizes(): IntArray {
        return intArrayOf(logoCard.height, headerFrameLayout.width)
    }

    override fun launchMapActivity(listingResponse: ListingResponse) {
        MapsActivity.launch(this, listingResponse)
    }

    override fun launchMapActivity(location: LatLng?) {
        MapsActivity.launch(this, location)
    }

    override fun showProgressBar(show: Boolean, color: Int) {
        if (show) {
            listingContentProgress.indeterminateDrawable
                    .setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP)
            progressBarWrapper.visibility = View.VISIBLE
        } else progressBarWrapper.visibility = View.GONE
    }

    override fun isActivityFinishing(): Boolean {
        return isFinishing
    }

    override fun measureLogoBackground(widthMode: Int, heightMode: Int): IntArray {
        headerBackgroundImageView.measure(widthMode, heightMode)
        return intArrayOf(headerBackgroundImageView.measuredWidth, headerBackgroundImageView.measuredHeight)
    }

    override fun createCircularRevealAnimator(revealStartX: Int, revealStartY: Int, startRadius: Int,
                                              finalRadius: Int, duration: Int, interpolator: Interpolator,
                                              listenerAdapter: AnimatorListenerAdapter): Animator {

        val animator = ViewAnimationUtils.createCircularReveal(headerBackgroundImageView,
                revealStartX, revealStartY, startRadius.toFloat(), finalRadius.toFloat())
        animator.duration = duration.toLong()
        animator.interpolator = interpolator
        animator.addListener(listenerAdapter)
        return animator
    }

    override fun setupCollapsingToolbarLayout(color: Int) {
        collapsingToolbarLayout.setContentScrimColor(color)
        collapsingToolbarLayout.setStatusBarScrimColor(color)
    }

    override fun setLogoBackgroundColor(color: Int) {
        headerBackgroundImageView.setBackgroundColor(color)
    }

    override fun delegateStartPostponedEnterTransition() {
        supportStartPostponedEnterTransition()
    }

    override fun setLogoBackgroundVisibility(show: Boolean) {
        headerBackgroundImageView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showToast(@StringRes message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun beginScrollViewTransition(transition: Transition) {
        TransitionManager.beginDelayedTransition(nestedScrollView, transition)
    }

    override fun showCardListingLayout(show: Boolean) {
        mainRecycler.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun inflateEmptyListing() {
        emptyListingViewStub.inflate()
    }

    override fun showMapLogo(endAction: Runnable, withAnimation: Boolean) {
        mapLogoImageView.apply {
            visibility = View.VISIBLE
            if (withAnimation) {
                scaleX = 0f
                scaleY = 0f
                animate().scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(500)
                        .setDuration(400)
                        .setInterpolator(AnimUtils.getOvershootInterpolator(this@DetailActivity))
                        .withEndAction(endAction)
            } else post { endAction.run() }
        }
    }

    override fun getMapLogoRect(): Rect {
        val visibleRect = Rect()
        mapLogoImageView.getGlobalVisibleRect(visibleRect)
        return visibleRect
    }

    override fun delegateRunOnUiThread(runnable: Runnable) {
        runOnUiThread(runnable)
    }

    override fun provideConnectivityManager(): ConnectivityManager {
        return getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun dispatchMapLogoTouchEvent(motionEvent: MotionEvent) {
        mapLogoImageView.dispatchTouchEvent(motionEvent)
    }

    override fun setToolbarTouchListener(onTouchListener: View.OnTouchListener) {
        toolbar.setOnTouchListener(onTouchListener)
    }

}
