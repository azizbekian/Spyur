package com.azizbekian.spyur.activity;

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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.api.ApiInteractor;
import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.listener.AppBarStateChangeListener.AppBarState;
import com.azizbekian.spyur.misc.Constants;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.azizbekian.spyur.utils.AnimUtils;
import com.azizbekian.spyur.utils.LogUtils;
import com.azizbekian.spyur.utils.NetworkUtils;
import com.azizbekian.spyur.utils.TransitionUtils;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.azizbekian.spyur.listener.AppBarStateChangeListener.EXPANDED;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingActivity extends AppCompatActivity
        implements YouTubePlayer.OnInitializedListener {

    private static final String EXTRA_SEARCH_ITEM = "extra_search_item";
    private static final String TAG_YOUTUBE = "tag_youtube";
    private static final int DURATION_REVEAL = 1300;
    private static final int DURATION_SLIDE_PAGE_CHANGE = 4000;
    private static final int YOUTUBE_RECOVERY_DIALOG_REQUEST = 821;

    /**
     * @param activity                     The activity that launches {@link ListingActivity}
     * @param transitionView               The view that is being transitioned
     * @param searchItem                   An instance of  {@link SearchItem}, which comprises the data,
     *                                     that would be populated in {@link ListingActivity}
     * @param isTransitionViewFullyVisible If true - the {@code transitionView} would be transitioned,
     *                                     otherwise it shouldn't.
     */
    public static void launch(Activity activity,
                              View transitionView,
                              SearchItem searchItem,
                              boolean isTransitionViewFullyVisible) {

        Intent intent = new Intent(activity, ListingActivity.class);
        intent.putExtra(EXTRA_SEARCH_ITEM, searchItem);

        final Pair[] pairs = TransitionUtils.createSafeTransitionParticipants(activity, true,
                isTransitionViewFullyVisible ?
                        Pair.create(transitionView, activity.getString(R.string.transition_logo)) :
                        null);

        @SuppressWarnings("unchecked")
        ActivityOptionsCompat sceneTransitionAnimation = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity, pairs);

        ActivityCompat.startActivity(activity, intent, sceneTransitionAnimation.toBundle());
    }

    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.scroll) NestedScrollView nestedScrollView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.back) ImageButton back;
    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;
    @BindView(R.id.logo_card_view) CardView logoCard;
    @BindView(R.id.logo_background) ImageView logo;
    @BindView(R.id.listing_header_background) ImageView logoBackground;
    @BindView(R.id.header_frame) FrameLayout headerLayout;
    @BindView(R.id.listing_content_progress_frame) FrameLayout progressBarLayout;
    @BindView(R.id.listing_content_progress) ProgressBar progressBar;
    @BindView(R.id.logo_map) ImageView mapLogo;
    @BindView(R.id.title_header) TextView titleTextView;

    // Content
    @BindView(R.id.listing_cards) LinearLayout cardRoot;
    @BindView(R.id.executives) TextView executivesTextView;
    @BindView(R.id.executives_container) LinearLayout executivesContainer;
    @BindView(R.id.card_listing_layout) LinearLayout cardListingLayout;
    @BindView(R.id.contact_info) TextView contactInfoTextView;
    @BindView(R.id.contact_info_container) LinearLayout contactInfoContainer;
    @BindView(R.id.website) TextView websiteTextView;
    @BindView(R.id.website_container) LinearLayout websitesContainer;
    @BindView(R.id.listing_in_spyur) TextView listingInSpyurTextView;
    @BindView(R.id.listing_in_spyur_url) TextView listingInSpyurUrlTextView;
    @BindView(R.id.viewstub_images_slider) ViewStub imagesViewStub;
    @BindView(R.id.viewstub_youtube) ViewStub videoViewStub;
    @BindView(R.id.viewstub_empty_listing) ViewStub emptyListingViewStub;

    @BindColor(R.color.colorPrimary) int colorPrimary;
    @BindColor(R.color.colorPrimaryDark) int colorPrimaryDark;
    @BindColor(android.R.color.transparent) int transparentColor;
    @Inject ApiInteractor apiInteractor;
    @Inject RequestManager glide;

    private SliderLayout mSliderLayout;
    private ConnectivityManager mConnectivityManager;
    private @AppBarState int mAppBarState = EXPANDED;
    private Animator mBackgroundRevealAnimator;
    private TransitionInflater mTransitionInflater;
    private Transition mainCardAutoTransition;
    private Transition mImagesAndVideoTransition;
    private SearchItem mSearchItem;
    private Call<ListingResponse> mCall;
    private int mPaletteVibrant;
    private Rect mMapLogoRect;
    private ListingResponse mListingResponse;
    private YouTubePlayer mYoutubePlayer;
    private YouTubePlayerFragment youTubePlayerFragment;

    /**
     * Indicates, whether there has been a connectivity loss while retrieving data. If true - the
     * connection has been lost.
     */
    private boolean mIsMonitoringConnectivity;

    /**
     * Tracks whether the content should be downloaded, which was interrupted because of connection
     * loss. If true - the content should be downloaded.
     */
    private boolean mIsRetrievalPending;

    /**
     * If true - images and video transition animation have been ended.
     */
    private boolean mIsImagesAndVideoTransitionEnded;

    /**
     * Tracks, whether the map icon animation has been shown. If true - the animation hasn't been
     * shown.
     */
    private boolean mIsMapAnimPending;

    /**
     * Prevents back button press when the initial setup transition is running.
     * <p>
     * If true, drops back button press.
     */
    private boolean mIsAutoTransitionRunning;

    /**
     * If true - map's logo coordinates haven't been saved in order to handle clicks.
     */
    private boolean mIsMapIconCoordinatesSavingPending;

    /**
     * If true - user has pressed back button. Is neccessary particularly for tracking shared element
     * return callback. {@link Activity#isFinishing()} can't be used instead, because it's value is
     * being updated after shared element's return callback.
     */
    private boolean mOnBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        ButterKnife.bind(this);

        SpyurApplication.getComponent().inject(this);

        supportPostponeEnterTransition();
        // Using setEnterSharedElementCallback, which is being called for return transition too.
        setEnterSharedElementCallback(mReturnSharedElementCallback);
        getWindow().getEnterTransition().addListener(mOnTransitionEndListener);

        mSearchItem = getIntent().getExtras().getParcelable(EXTRA_SEARCH_ITEM);
        analyzeLogo();

        mTransitionInflater = TransitionInflater.from(this);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        mainCardAutoTransition = mTransitionInflater.inflateTransition(R.transition.main_card);
        mainCardAutoTransition.addListener(new AnimUtils.TransitionListenerAdapter() {

            @Override
            public void onTransitionStart(Transition transition) {
                mIsAutoTransitionRunning = true;
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                mIsAutoTransitionRunning = false;
                setupImagesAndVideo(mListingResponse.images, mListingResponse.videoUrl);
            }
        });

        toolbar.setOnTouchListener(mOnToolbarTouchListener);
        back.setOnClickListener(v -> onBackPressed());
        appBarLayout.addOnOffsetChangedListener(mAppBarStateChangeListener);
        titleTextView.setText(mSearchItem.getTitle());
        collapsingToolbarLayout.setTitle(mSearchItem.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(transparentColor);
    }

    /**
     * Creates {@link Palette} object from the logo, updates necessary views for appropriate
     * colors. After that activity's postponed enter transition is being started.
     */
    private void analyzeLogo() {
        glide
                .load(mSearchItem.getLogo())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target,
                                               boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target,
                                                   boolean isFromMemoryCache, boolean isFirstResource) {
                        Palette.from(resource)
                                .generate(palette -> {
                                    applyPalette(palette);
                                    supportStartPostponedEnterTransition();
                                });
                        return false;
                    }
                })
                .into(logo);
    }

    private void retrieveData(String href) {
        if (NetworkUtils.isNetworkAvailable(this) && null == mCall) {
            showProgressBar(true);
            mCall = apiInteractor.getListing(href);
            mCall.enqueue(new Callback<ListingResponse>() {
                @Override
                public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                    mIsRetrievalPending = false;
                    mListingResponse = response.body();
                    if (null != mListingResponse) {
                        setupContent(mListingResponse);
                    } else {
                        showProgressBar(false);
                        // TODO: show message "error data"
                        LogUtils.e("Null response received when fetching listing.");
                    }
                }

                @Override
                public void onFailure(Call<ListingResponse> call, Throwable t) {
                    showProgressBar(false);
                    // TODO: show message "failure"
                    mIsRetrievalPending = false;
                    LogUtils.e("Failure loading listing: " + t.getMessage());
                }
            });
        } else {
            showProgressBar(true);
            Toast.makeText(ListingActivity.this, R.string.message_no_internet, Toast.LENGTH_SHORT).show();
            mConnectivityManager.registerNetworkCallback(new NetworkRequest
                            .Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(),
                    mConnectivityCallback);
            mIsRetrievalPending = true;
            mIsMonitoringConnectivity = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsRetrievalPending) retrieveData(mSearchItem.getHref());
        setSliderAutoScroll(true);
    }

    @OnClick({R.id.logo_map})
    public void onLogoClicked(View v) {
        MapActivity.launch(this, mListingResponse);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsMonitoringConnectivity) {
            mConnectivityManager.unregisterNetworkCallback(mConnectivityCallback);
            mIsMonitoringConnectivity = false;
        }
        if (null != mCall) {
            mCall.cancel();
            mCall = null;
        }
        if (isFinishing() && null != mYoutubePlayer) {
            mYoutubePlayer.pause();
            mYoutubePlayer.release();
            getFragmentManager().beginTransaction().remove(youTubePlayerFragment).commit();
        }
    }

    @Override
    protected void onStop() {
        setSliderAutoScroll(false);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mOnBackPressed = true;
        ActivityCompat.finishAfterTransition(this);
    }

    @Override
    public void finishAfterTransition() {
        if (mIsAutoTransitionRunning) return;
        if (null != mImagesAndVideoTransition && !mIsImagesAndVideoTransitionEnded) return;
        setResult(RESULT_OK, null);
        super.finishAfterTransition();
    }

    /**
     * @param show If true shows loading mProgress bar, otherwise hides.
     */
    private void showProgressBar(boolean show) {
        if (show) {
            progressBar.getIndeterminateDrawable().setColorFilter(mPaletteVibrant,
                    android.graphics.PorterDuff.Mode.SRC_ATOP);
            progressBarLayout.setVisibility(View.VISIBLE);
        } else progressBarLayout.setVisibility(View.GONE);
    }

    /**
     * Applies the generated {@link Palette} to views.
     */
    private void applyPalette(Palette palette) {
        mPaletteVibrant = palette.getVibrantColor(colorPrimary);
        int paletteLightVibrant = palette.getLightVibrantColor(colorPrimaryDark);

        collapsingToolbarLayout.setContentScrimColor(paletteLightVibrant);
        collapsingToolbarLayout.setStatusBarScrimColor(paletteLightVibrant);

        int[] loc = new int[2];
        logoCard.getLocationOnScreen(loc);
        int revealStartX = loc[0] + logoCard.getWidth() / 2;
        int revealStartY = loc[1] + logoCard.getHeight() / 2;

        int widthMode = makeMeasureSpec(headerLayout.getWidth(), View.MeasureSpec.EXACTLY);
        int heightMode = makeMeasureSpec(headerLayout.getHeight(), View.MeasureSpec.EXACTLY);
        logoBackground.measure(widthMode, heightMode);
        logoBackground.setBackgroundColor(mPaletteVibrant);
        float finalRadius = (float) Math.hypot(logoBackground.getMeasuredWidth(),
                logoBackground.getMeasuredHeight());
        mBackgroundRevealAnimator = ViewAnimationUtils.createCircularReveal(logoBackground,
                revealStartX, revealStartY, 0, finalRadius);
        mBackgroundRevealAnimator.setDuration(DURATION_REVEAL);
        mBackgroundRevealAnimator.setInterpolator(AnimUtils.EASE_OUT_CUBIC);
        mBackgroundRevealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                logoBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBackgroundRevealAnimator.removeListener(this);
            }
        });
    }

    /**
     * Sets up all the content info.
     */
    private void setupContent(ListingResponse listingResponse) {
        if (!isHeaderCardEmpty(listingResponse)) {
            setupExecutives(listingResponse.executives);
            setupContactInfo(listingResponse.contactInfos);
            setupWebsites(listingResponse.websites);
            setupListingInSpyur(listingResponse.listingInSpyur);
            showProgressBar(false);
            TransitionManager.beginDelayedTransition(nestedScrollView, mainCardAutoTransition);
            cardListingLayout.setVisibility(View.VISIBLE);
        } else {
            TransitionManager.beginDelayedTransition(nestedScrollView, mainCardAutoTransition);
            emptyListingViewStub.inflate();
            showProgressBar(false);
        }
        setupMap(listingResponse);
    }

    private void setupExecutives(List<String> executives) {
        int executiveSize = executives.size();
        String executiveString = getResources().getQuantityString(R.plurals.executive, executiveSize);
        executivesTextView.setText(executiveString);

        if (executiveSize == 0) {
            executivesTextView.setVisibility(View.GONE);
            executivesContainer.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < executiveSize; ++i) {
                View row = getLayoutInflater().inflate(R.layout.row_executor, executivesContainer,
                        false);
                ((TextView) row.findViewById(R.id.row_executor)).setText(executives.get(i));
                executivesContainer.addView(row);
            }
        }
    }

    private void setupContactInfo(List<ListingResponse.ContactInfo> contactInfoList) {
        int contactInfoListSize = contactInfoList.size();
        if (contactInfoListSize == 0) {
            contactInfoTextView.setVisibility(View.GONE);
            contactInfoContainer.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < contactInfoListSize; ++i) {
                ListingResponse.ContactInfo contactInfo = contactInfoList.get(i);
                View row = getLayoutInflater().inflate(R.layout.row_contact_info,
                        contactInfoContainer, false);
                TextView address = (TextView) row.findViewById(R.id.row_contact_info_address);
                address.setText(contactInfo.address);
                final LatLng loc = contactInfo.loc;
                address.setOnClickListener(v -> MapActivity.launch(ListingActivity.this, loc));
                if (TextUtils.isEmpty(contactInfo.region)) {
                    (row.findViewById(R.id.row_contact_info_region)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.row_contact_info_region))
                            .setText(contactInfo.region);
                }

                LinearLayout phoneNumbersContainer = (LinearLayout) row
                        .findViewById(R.id.row_contact_info_phone_number_container);

                if (contactInfo.phoneNumber.size() == 0) {
                    phoneNumbersContainer.setVisibility(View.GONE);
                } else {
                    for (String phoneNumber : contactInfo.phoneNumber) {
                        View phoneNumberRow = getLayoutInflater()
                                .inflate(R.layout.row_phone_number, phoneNumbersContainer, false);
                        TextView phoneNumberTextView = (TextView) phoneNumberRow
                                .findViewById(R.id.row_contact_info_phone_number);
                        phoneNumberTextView.setText(phoneNumber);
                        phoneNumbersContainer.addView(phoneNumberRow);
                    }
                }
                contactInfoContainer.addView(row);
                if (i != contactInfoListSize - 1) {
                    contactInfoContainer.addView(getLayoutInflater().inflate(R.layout.divider,
                            contactInfoContainer, false));
                }
            }
        }
    }

    private void setupWebsites(List<String> websiteList) {
        int websiteSize = websiteList.size();
        if (websiteSize == 0) {
            websiteTextView.setVisibility(View.GONE);
            websitesContainer.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < websiteSize; ++i) {
                View row = getLayoutInflater().inflate(R.layout.row_website, websitesContainer, false);
                ((TextView) row.findViewById(R.id.row_website_tv)).setText(websiteList.get(i));
                websitesContainer.addView(row);
            }
        }
    }

    private void setupMap(ListingResponse listingResponse) {
        if (listingResponse.hasMapCoordinates) {
            if (mAppBarState == EXPANDED) animateMapLogo();
            else mIsMapAnimPending = true;
        }
    }

    private void setupListingInSpyur(String listingInSpyur) {
        if (TextUtils.isEmpty(listingInSpyur)) {
            listingInSpyurTextView.setVisibility(View.GONE);
            listingInSpyurUrlTextView.setVisibility(View.GONE);
        } else {
            listingInSpyurUrlTextView.setText(listingInSpyur);
        }
    }

    private void setupImagesAndVideo(List<String> imagesList, String videoId) {
        View imagesCard = null, videoCard = null;
        if (null != imagesList && imagesList.size() > 0) {
            FrameLayout imagesSlideCardRoot = (FrameLayout) imagesViewStub.inflate()
                    .findViewById(R.id.listing_card_image_root);
            imagesCard = imagesSlideCardRoot.findViewById(R.id.listing_card_image);
            mSliderLayout = (SliderLayout) imagesCard.findViewById(R.id.slider);

            for (String name : imagesList) {
                TextSliderView textSliderView = new TextSliderView(this);
                textSliderView
                        .image(name)
                        .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

                mSliderLayout.addSlider(textSliderView);
            }
            mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
            mSliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mSliderLayout.setDuration(DURATION_SLIDE_PAGE_CHANGE);
            mSliderLayout.stopAutoCycle();
        }

        if (!TextUtils.isEmpty(videoId)) {
            FrameLayout videoCardRoot = (FrameLayout) videoViewStub.inflate()
                    .findViewById(R.id.listing_card_youtube_root);
            youTubePlayerFragment = new YouTubePlayerFragment();
            youTubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, this);
            getFragmentManager().beginTransaction().add(R.id.youtube_fragment, youTubePlayerFragment,
                    TAG_YOUTUBE).commit();

            videoCard = videoCardRoot.findViewById(R.id.listing_card_video);
        }

        if (!(null == imagesCard && null == videoCard)) {
            if (null == imagesCard) {
                mImagesAndVideoTransition = mTransitionInflater
                        .inflateTransition(R.transition.video_card);
            } else {
                mImagesAndVideoTransition = mTransitionInflater
                        .inflateTransition(R.transition.image_video_card);
            }
            mImagesAndVideoTransition.addListener(new AnimUtils.TransitionListenerAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    if (!isFinishing() && null != mSliderLayout) mSliderLayout.startAutoCycle();
                    mIsImagesAndVideoTransitionEnded = true;
                }
            });
            mImagesAndVideoTransition.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(ListingActivity.this));
            TransitionManager.beginDelayedTransition(cardRoot, mImagesAndVideoTransition);
            if (null != imagesCard) imagesCard.setVisibility(View.VISIBLE);
            if (null != videoCard) videoCard.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows map icon with animation.
     */
    private void animateMapLogo() {
        mIsMapAnimPending = false;
        mapLogo.setVisibility(View.VISIBLE);
        mapLogo.setScaleX(0);
        mapLogo.setScaleY(0);
        mapLogo.animate()
                .scaleX(1)
                .scaleY(1)
                .setStartDelay(500)
                .setDuration(400)
                .setInterpolator(AnimUtils.getOvershootInterpolator(this))
                .withEndAction(() -> {
                    if (isAppBarExpanded()) saveMapIconCoordinates();
                    else mIsMapIconCoordinatesSavingPending = true;
                });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer,
                                        boolean wasRestored) {
        mYoutubePlayer = youTubePlayer;
        if (!wasRestored) {
            youTubePlayer.cueVideo(mListingResponse.videoUrl);
            youTubePlayer.setShowFullscreenButton(false);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, YOUTUBE_RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_youtube),
                    errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == YOUTUBE_RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            ((YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment))
                    .initialize(Constants.YOUTUBE_API_KEY, this);
        }
    }

    /**
     * Sets the {@code mSliderLayout} auto scroll status.
     *
     * @param autoScroll If true - images would be auto scrolled. False otherwise.
     */
    private void setSliderAutoScroll(boolean autoScroll) {
        if (null == mSliderLayout) return;
        if (autoScroll) mSliderLayout.startAutoCycle();
        else mSliderLayout.stopAutoCycle();
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

    // Callbacks & listeners

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

    /**
     * Is responsible for handling connectivity changes properly.
     */
    private final ConnectivityManager.NetworkCallback mConnectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            runOnUiThread(() -> {
                if (mIsRetrievalPending) retrieveData(mSearchItem.getHref());
            });
        }

        @Override
        public void onLost(Network network) {
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
                if (mIsMapAnimPending) animateMapLogo();
                if (mIsMapIconCoordinatesSavingPending) saveMapIconCoordinates();
            }
        }
    };

    /**
     * Retains map icon's coordinates in order to handle click events, because it's beneath toolbar.
     */
    private void saveMapIconCoordinates() {
        int[] loc = new int[2];
        mapLogo.getLocationOnScreen(loc);
        mMapLogoRect = new Rect(loc[0], loc[1], loc[0] + mapLogo.getWidth(), loc[1] + mapLogo.getHeight());
        mIsMapIconCoordinatesSavingPending = false;
    }

    /**
     * @return True if {@link CollapsingToolbarLayout} is fully expanded. False otherwise.
     */
    private boolean isAppBarExpanded() {
        return mAppBarState == EXPANDED;
    }

    /**
     * Dispatches click event from toolbar to map icon, because map icon is beneath the toolbar.
     */
    private final View.OnTouchListener mOnToolbarTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            if (mMapLogoRect != null && (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL)) {
                if (mMapLogoRect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                    mapLogo.dispatchTouchEvent(motionEvent);
                }
            }
            return false;
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
