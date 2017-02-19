package com.incentive.yellowpages.ui.detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.v7.graphics.Palette
import android.text.TextUtils
import android.transition.Transition
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.DataManager
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.data.model.SearchResponse
import com.incentive.yellowpages.injection.ConfigPersistent
import com.incentive.yellowpages.misc.AppBarStateChangeListener
import com.incentive.yellowpages.misc.AppBarStateChangeListener.AppBarState
import com.incentive.yellowpages.misc.AppBarStateChangeListener.Companion.EXPANDED
import com.incentive.yellowpages.misc.getColorInt
import com.incentive.yellowpages.misc.inflateTransition
import com.incentive.yellowpages.misc.isConnected
import com.incentive.yellowpages.ui.base.BaseApplication.Companion.context
import com.incentive.yellowpages.ui.base.BaseContract
import com.incentive.yellowpages.utils.AnimUtils
import com.incentive.yellowpages.utils.LogUtils
import io.reactivex.functions.Consumer
import javax.inject.Inject

@ConfigPersistent
class DetailPresenter @Inject constructor(val dataManager: DataManager)
    : BaseContract.BasePresenter<DetailView>() {

    companion object {
        val EXTRA_SEARCH_ITEM = "extra_search_item"
        val EXTRA_SEARCH_ITEM_POSITION = "extra_search_item_position"
        private val DURATION_REVEAL = 1300
    }

    private var searchItem: SearchResponse.SearchItem? = null
    private var listing: ListingResponse? = null
    private var adapter: DetailsAdapter? = null
    private var mainCardAutoTransition: Transition? = null
    private var mapLogoRect: Rect? = null
    private var connectivityManager: ConnectivityManager? = null

    @ColorInt private var paletteVibrant: Int = 0
    @ColorInt private var primaryColor: Int = 0
    @ColorInt private var primaryColorDark: Int = 0
    @ColorInt private var transparentColor: Int = 0
    private var backgroundRevealAnimator: Animator? = null
    private var afterOrientationChange = false
    private var positionInAdapter: Int = -1

    /**
     * If true - user has pressed back button. Is neccessary particularly for tracking shared element
     * return callback. [android.app.Activity.isFinishing] can't be used instead, because it's value is
     * being updated after shared element's return callback.
     */
    private var backPressed = false

    /**
     * Tracks whether the content should be downloaded, which was interrupted because of connection
     * loss. If true - the content should be downloaded.
     */
    private var isRetrievalPending = false

    /**
     * Prevents back button press when the initial setup transition is running.
     *
     *
     * If true, drops back button press.
     */
    private var isAutoTransitionRunning = false

    /**
     * Tracks, whether the map icon animation has been shown. If true - the animation hasn't been
     * shown.
     */
    private var isMapAnimPending = false

    /**
     * If true - map's logo coordinates haven't been saved in order to handle clicks.
     */
    private var isMapIconCoordinatesSavingPending = false

    /**
     * Indicates, whether there has been a connectivity loss while retrieving data. If true - the
     * connection has been lost.
     */
    private var isMonitoringConnectivity = false

    private var isHeaderCardEmpty = true

    @AppBarState private var appBarState = AppBarStateChangeListener.EXPANDED

    override fun create(view: DetailView, savedInstanceState: Bundle?, intent: Intent?, arguments: Bundle?,
                        isPortrait: Boolean) {
        super.create(view, savedInstanceState, intent, arguments, isPortrait)

        // if we are here for the first time
        if (null == searchItem && null != intent) {
            searchItem = intent.extras.getParcelable(EXTRA_SEARCH_ITEM)
            positionInAdapter = intent.extras.getInt(EXTRA_SEARCH_ITEM_POSITION, -1)
            this.view?.apply {
                primaryColor = context.getColorInt(R.color.colorPrimary)
                primaryColorDark = context.getColorInt(R.color.colorPrimaryDark)
                transparentColor = context.getColorInt(android.R.color.transparent)
            }
        }

        this.view?.apply {
            setupListeners()
            setToolbarTouchListener(onToolbarTouchListener)
            setTitle(searchItem!!.title)
            setCollapsingToolbarTitle(searchItem!!.title)
            setExpandedTitleColor(transparentColor)
            delegateSetEnterSharedElementCallback(returnSharedElementCallback)
            setAppBarStateChangeListener(appBarStateChangeListener)

            if (null == savedInstanceState) {
                delegatePostponeEnterTransition()
                addListenerToEnterTransition()
                mainCardAutoTransition = context.inflateTransition(R.transition.main_card)
                mainCardAutoTransition!!.addListener(object : AnimUtils.TransitionListenerAdapter() {
                    override fun onTransitionStart(transition: Transition) {
                        isAutoTransitionRunning = true
                    }

                    override fun onTransitionEnd(transition: Transition) {
                        isAutoTransitionRunning = false
                    }
                })
                connectivityManager = provideConnectivityManager()
            } else {
                afterOrientationChange = true
                if (null == adapter) retrieveListingData()
                else updateView(adapter, false)
            }
            analyzeLogo(searchItem!!, onImageSuccess, onImageFailure)
        }

    }

    fun onBackPressed(): Boolean {
        backPressed = true
        view?.finish(isAppBarExpanded())
        return true
    }

    fun allowToFinish() = !isAutoTransitionRunning

    fun imageReady(resource: Bitmap?, logoCardLocationOnScreen: IntArray, logoCardWidth: Int,
                   logoCardHeight: Int, headerLayoutWidth: Int, headerLayoutHeight: Int) {
        Palette.from(resource)
                .generate { palette ->

                    paletteVibrant = palette.getVibrantColor(primaryColor)
                    val revealStartX = logoCardLocationOnScreen[0] + logoCardWidth shr 1
                    val revealStartY = logoCardLocationOnScreen[1] + logoCardHeight shr 1

                    val widthMode = makeMeasureSpec(headerLayoutWidth, View.MeasureSpec.EXACTLY)
                    val heightMode = makeMeasureSpec(headerLayoutHeight, View.MeasureSpec.EXACTLY)

                    view?.apply {
                        setupCollapsingToolbarLayout(palette.getLightVibrantColor(primaryColorDark))
                        val logoBackgroundSizes = measureLogoBackground(widthMode, heightMode)
                        setLogoBackgroundColor(paletteVibrant)
                        val finalRadius = Math.hypot(logoBackgroundSizes[0].toDouble(),
                                logoBackgroundSizes[1].toDouble()).toInt()

                        if (afterOrientationChange) {
                            view?.setLogoBackgroundVisibility(true)
                        } else {
                            backgroundRevealAnimator = createCircularRevealAnimator(revealStartX,
                                    revealStartY, 0, finalRadius, DURATION_REVEAL,
                                    AnimUtils.EASE_OUT_CUBIC, animatorListenerAdapter)
                        }
                        delegateStartPostponedEnterTransition()
                    }

                }

    }

    fun startRevealAnimation() {
        if (null != backgroundRevealAnimator && !backgroundRevealAnimator!!.isStarted) {
            backgroundRevealAnimator!!.start()
        }
    }

    fun onDataSuccess(listing: ListingResponse) {
        this.listing = listing
        isRetrievalPending = false
        val executives = listing.executives ?: emptyList()
        val contactInfos = listing.contactInfos ?: emptyList()
        val websites = listing.websites ?: emptyList()
        val listingInSpyur = listing.listingInSpyur
        isHeaderCardEmpty = isHeaderCardEmpty(executives, contactInfos, websites, listingInSpyur)
        adapter = DetailsAdapter(executives, contactInfos, websites, listingInSpyur,
                Consumer<LatLng?> { view?.launchMapActivity(it) })
        updateView(adapter)
    }

    fun updateView(adapter: DetailsAdapter?, withAnimation: Boolean = true) {
        view?.apply {
            if (null != adapter) {
                showProgressBar(false, paletteVibrant)
                if (!isHeaderCardEmpty) {
                    setupRecycler(adapter)
                    mainCardAutoTransition?.apply {
                        if (withAnimation) beginScrollViewTransition(this)
                    }
                    showCardListingLayout(true)
                } else {
                    mainCardAutoTransition?.apply {
                        if (withAnimation) beginScrollViewTransition(this)
                    }
                    inflateEmptyListing()
                }
                if (listing!!.hasMapCoordinates) {
                    if (isAppBarExpanded()) {
                        isMapAnimPending = false
                        showMapLogo(mapLogoAnimateEndRunnable, withAnimation)
                    } else isMapAnimPending = true
                }
            } else {
                showProgressBar(false, paletteVibrant)
                LogUtils.e("Null response received when fetching listing.")
            }
        }
    }

    fun onDataFailure(e: Throwable) {
        view?.showProgressBar(false, paletteVibrant)
        isRetrievalPending = false
        LogUtils.e("Failure loading listing: " + e.message)
    }

    fun retrieveListingData() {
        if (context.isConnected()) {
            view?.showProgressBar(true, paletteVibrant)
            addDisposable(dataManager.getListing(searchItem!!.href)
                    .subscribe({ onDataSuccess(it) }, { onDataFailure(it) }))

        } else {
            view?.showProgressBar(false, paletteVibrant)
            view?.showToast(R.string.message_no_internet)

            connectivityManager?.registerNetworkCallback(NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                    connectivityCallback)
            isRetrievalPending = true
            isMonitoringConnectivity = true
        }
    }

    override fun destroy(isFinishing: Boolean) {
        afterOrientationChange = false
        mapLogoRect = null
    }

    fun getPositionInAdapter() = positionInAdapter

    /**
     * @return True if the content of the header item is empty. False otherwise.
     */
    private fun isHeaderCardEmpty(executives: List<String>,
                                  contactInfos: List<ListingResponse.ContactInfo>,
                                  websites: List<String>,
                                  listingInSpyur: String?): Boolean = !(executives.isNotEmpty()
            || contactInfos.isNotEmpty()
            || websites.isNotEmpty()
            || !TextUtils.isEmpty(listingInSpyur))

    /**
     * @return True if [android.support.design.widget.CollapsingToolbarLayout] is fully expanded.
     * False otherwise.
     */
    private fun isAppBarExpanded() = appBarState == EXPANDED

    private val mapLogoAnimateEndRunnable = Runnable {
        if (isAppBarExpanded()) {
            mapLogoRect = view?.getMapLogoRect()
            isMapIconCoordinatesSavingPending = false
        } else isMapIconCoordinatesSavingPending = true
    }

    /**
     * Is responsible for handling connectivity changes properly.
     */
    private val connectivityCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            view?.delegateRunOnUiThread(Runnable { if (isRetrievalPending) retrieveListingData() })
        }

        override fun onLost(network: Network) {
            LogUtils.e("Connection lost")
        }
    }

    /**
     * Tracks for collapse state of `collapsingToolbarLayout`. If is expanded animates the
     * logo back to previous activity, otherwise does not perform back shared element transition.
     */
    private val appBarStateChangeListener = object : AppBarStateChangeListener() {
        override fun onStateChanged(appBarLayout: AppBarLayout, @AppBarState appBarState: Long) {
            this@DetailPresenter.appBarState = appBarState
            if (isAppBarExpanded()) {
                if (isMapAnimPending) view?.showMapLogo(mapLogoAnimateEndRunnable)
                if (isMapIconCoordinatesSavingPending) mapLogoRect = view?.getMapLogoRect()
            }
        }
    }

    /**
     * Dispatches click event from toolbar to map icon, because map icon is beneath the toolbar.
     */
    private val onToolbarTouchListener = View.OnTouchListener { view, motionEvent ->
        if (null != mapLogoRect && isAppBarExpanded()) {
            if (mapLogoRect?.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt()) as Boolean) {
                this.view?.dispatchMapLogoTouchEvent(motionEvent)
            }
        }
        false
    }

    private val onImageSuccess = Consumer<Bitmap?> {
        apply {
            view?.apply {
                val logoCardCoords = getLogoCardSizes()
                val headerLayoutCoords = getHeadLayoutSizes()
                imageReady(it, intArrayOf(logoCardCoords[0], logoCardCoords[1]),
                        logoCardCoords[2], logoCardCoords[3],
                        headerLayoutCoords[0], headerLayoutCoords[1])
            }
        }
    }

    private val onImageFailure = Consumer<Exception?> {
        apply {
            LogUtils.i("Error loading image:" + it.toString())
        }
    }

    private val returnSharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
            if (backPressed && appBarState != EXPANDED) {
                // if this callback is fired when leaving activity and if app bar state is not expanded
                // clear the shared view from map, we do not want back transition animation
                names.clear()
                sharedElements.clear()
            }
        }
    }

    private val animatorListenerAdapter = object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            view?.setLogoBackgroundVisibility(true)
        }

        override fun onAnimationEnd(animation: Animator) {
            backgroundRevealAnimator?.removeListener(this)
        }
    }

    fun dispatchLogoClick() {
        view?.launchMapActivity(listing!!)
    }

}
