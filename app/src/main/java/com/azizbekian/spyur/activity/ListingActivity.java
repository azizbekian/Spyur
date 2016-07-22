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
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.TransitionRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
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
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.SpyurApplication;
import com.azizbekian.spyur.activity.base.RxBaseActivity;
import com.azizbekian.spyur.listener.AppBarStateChangeListener;
import com.azizbekian.spyur.misc.Constants;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.model.SearchResponse.SearchItem;
import com.azizbekian.spyur.mvp.listing.ListingContract;
import com.azizbekian.spyur.mvp.listing.ListingPresenter;
import com.azizbekian.spyur.utils.AnimUtils;
import com.azizbekian.spyur.utils.FragmentUtils;
import com.azizbekian.spyur.utils.TransitionUtils;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class ListingActivity extends RxBaseActivity implements ListingContract.View {

    private static final String TAG_YOUTUBE = "tag_youtube";
    private static final int DURATION_SLIDE_PAGE_CHANGE = 4000;

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
        intent.putExtra(ListingPresenter.EXTRA_SEARCH_ITEM, searchItem);

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

    private RequestManager mGlide;
    private SliderLayout mSliderLayout;
    private ListingContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        ButterKnife.bind(this);

        checkInputAndThrow(ListingPresenter.EXTRA_SEARCH_ITEM);
        mGlide = SpyurApplication.getComponent().getGlide();
        mPresenter = new ListingPresenter(this, getIntent().getExtras());
        mPresenter.create();
    }

    @Override
    public void addListenerToEnterTransition() {
        getWindow().getEnterTransition().addListener(new AnimUtils.TransitionListenerAdapter() {
            @Override public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);

                getWindow().getEnterTransition().removeListener(this);
                mPresenter.startRevealAnimation();
                mPresenter.retrieveListingData();
            }
        });
    }

    @Override public void delegatePostponeEnterTransition() {
        supportPostponeEnterTransition();
    }

    @Override public void delegateSetEnterSharedElementCallback(SharedElementCallback callback) {
        // Using setEnterSharedElementCallback, which is being called for return transition too.
        setEnterSharedElementCallback(callback);
    }

    @Override public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override public void setCollapsingToolbarTitle(String title) {
        collapsingToolbarLayout.setTitle(title);
    }

    @Override public void setExpandedTitleColor(@ColorInt int color) {
        collapsingToolbarLayout.setExpandedTitleColor(color);
    }

    @Override
    public void setAppBarStateChangeListener(AppBarStateChangeListener appBarStateChangeListener) {
        appBarLayout.addOnOffsetChangedListener(appBarStateChangeListener);
    }

    @OnClick({R.id.back}) @SuppressWarnings("unused")
    public void onBackClicked() {
        onBackPressed();
    }

    @Override
    public void analyzeLogo(SearchItem searchItem, RequestListener<String, Bitmap> glideListener) {
        mGlide
                .load(searchItem.getLogo())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .priority(Priority.IMMEDIATE)
                .listener(glideListener)
                .into(logo);
    }

    @Override public int[] getLogoCardSizes() {
        int[] loc = new int[2];
        logo.getLocationOnScreen(loc);
        return new int[]{loc[0], loc[1], logoCard.getWidth(), logoCard.getHeight()};
    }

    @Override public int[] getHeadLayoutSizes() {
        return new int[]{logoCard.getHeight(), headerLayout.getWidth()};
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.resume();
    }

    @OnClick({R.id.logo_map})
    public void onLogoClicked(View v) {
        mPresenter.dispatchLogoClick();
    }

    @Override public void launchMapActivity(ListingResponse listingResponse) {
        MapActivity.launch(this, listingResponse);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPresenter.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mPresenter.stop();
    }

    @Override
    public void onBackPressed() {
        mPresenter.onBackPressed();
    }

    @Override public void finish(boolean afterTransition) {
        if (afterTransition) ActivityCompat.finishAfterTransition(this);
        else finish();
    }

    @Override
    public void finishAfterTransition() {
        if (!mPresenter.allowToFinish()) return;
        setResult(RESULT_OK, null);
        super.finishAfterTransition();
    }

    @Override
    public void showProgressBar(boolean show, int color) {
        if (show) {
            progressBar.getIndeterminateDrawable().setColorFilter(color,
                    android.graphics.PorterDuff.Mode.SRC_ATOP);
            progressBarLayout.setVisibility(View.VISIBLE);
        } else progressBarLayout.setVisibility(View.GONE);
    }

    @Override public void setupExecutives(List<String> executives) {
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

    @Override public void setupContactInfo(List<ListingResponse.ContactInfo> contactInfoList) {
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

    @Override public void setupWebsites(List<String> websiteList) {
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

    @Override public void setupListingInSpyur(String listingInSpyur) {
        if (TextUtils.isEmpty(listingInSpyur)) {
            listingInSpyurTextView.setVisibility(View.GONE);
            listingInSpyurUrlTextView.setVisibility(View.GONE);
        } else listingInSpyurUrlTextView.setText(listingInSpyur);
    }

    @Override
    public void setupImagesAndVideo(List<String> imagesList, String videoId,
                                    @Nullable Transition transition,
                                    YouTubePlayer.OnInitializedListener youtubeInitializedListener) {

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
            YouTubePlayerFragment youTubePlayerFragment = new YouTubePlayerFragment();
            youTubePlayerFragment.initialize(Constants.YOUTUBE_API_KEY, youtubeInitializedListener);
            getFragmentManager().beginTransaction().add(R.id.youtube_fragment, youTubePlayerFragment,
                    TAG_YOUTUBE).commit();

            videoCard = videoCardRoot.findViewById(R.id.listing_card_video);
        }

        if (!(null == imagesCard && null == videoCard)) {
            TransitionManager.beginDelayedTransition(cardRoot, transition);
            if (null != imagesCard) imagesCard.setVisibility(View.VISIBLE);
            if (null != videoCard) videoCard.setVisibility(View.VISIBLE);
        }
    }

    @Override public void removeYoutubeFragment() {
        Fragment fr = FragmentUtils.findByTag(this, TAG_YOUTUBE);
        if (null != fr) {
            getSupportFragmentManager().beginTransaction().remove(fr).commit();
        }
    }

    @Override public boolean isActivityFinishing() {
        return isFinishing();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mPresenter.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void initYoutubeFragment(String youtubeApiKey, YouTubePlayer.OnInitializedListener listener) {
        // Retry initialization if user performed a recovery action
        ((YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment))
                .initialize(youtubeApiKey, listener);
    }

    @Override
    public void showYoutubeErrorReason(YouTubeInitializationResult errorReason, int requestCode) {
        errorReason.getErrorDialog(this, requestCode).show();
    }

    @Override public void setSliderAutoScroll(boolean autoScroll) {
        if (null == mSliderLayout) return;
        if (autoScroll) mSliderLayout.startAutoCycle();
        else mSliderLayout.stopAutoCycle();
    }

    @Override public void delegateAddSubscription(Subscription s) {
        addSubscription(s);
    }

    @Override public void delegateUnsubscribe() {
        unsubscribe();
    }

    @Override public int[] measureLogoBackground(int widthMode, int heightMode) {
        logoBackground.measure(widthMode, heightMode);
        return new int[]{logoBackground.getMeasuredWidth(), logoBackground.getMeasuredHeight()};
    }

    @Override
    public Animator createCircularRevealAnimator(int revealStartX, int revealStartY, int startRadius,
                                                 int finalRadius, int duration, Interpolator interpolator,
                                                 AnimatorListenerAdapter listenerAdapter) {

        Animator animator = ViewAnimationUtils.createCircularReveal(logoBackground,
                revealStartX, revealStartY, startRadius, finalRadius);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addListener(listenerAdapter);
        return animator;
    }

    @Override public void setupCollapsingToolbarLayout(int color) {
        collapsingToolbarLayout.setContentScrimColor(color);
        collapsingToolbarLayout.setStatusBarScrimColor(color);
    }

    @Override public void setLogoBackgroundColor(int color) {
        logoBackground.setBackgroundColor(color);
    }

    @Override public void delegateStartPosponedEnterTransition() {
        supportStartPostponedEnterTransition();
    }

    @Override public void setLogoBackgroundVisibility(boolean show) {
        logoBackground.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public boolean delegateHasSubscriptions() {
        return hasSubscriptions();
    }

    @Override public void showToast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override public void beginScrollViewTransition(Transition transition) {
        TransitionManager.beginDelayedTransition(nestedScrollView, transition);
    }

    @Override public Transition inflateTransition(@TransitionRes int transitionId) {
        return TransitionInflater.from(this).inflateTransition(transitionId);
    }

    @Override public void showCardListingLayout(boolean show) {
        cardListingLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override public void inflateEmptyListing() {
        emptyListingViewStub.inflate();
    }

    @Override public void animateMapLogo(Runnable endAction) {
        mapLogo.setVisibility(View.VISIBLE);
        mapLogo.setScaleX(0);
        mapLogo.setScaleY(0);
        mapLogo.animate()
                .scaleX(1)
                .scaleY(1)
                .setStartDelay(500)
                .setDuration(400)
                .setInterpolator(AnimUtils.getOvershootInterpolator(this))
                .withEndAction(endAction);
    }

    @Override public Rect getMapLogoRect() {
        int[] loc = new int[2];
        mapLogo.getLocationOnScreen(loc);
        return new Rect(loc[0], loc[1], loc[0] + mapLogo.getWidth(), loc[1] + mapLogo.getHeight());
    }

    @Override public void delegateRunOnUiThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    @Override public ConnectivityManager provideConnectivityManager() {
        return (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override public void dispatchMapLogoTouchEvent(MotionEvent motionEvent) {
        mapLogo.dispatchTouchEvent(motionEvent);
    }

    @Override public void setToolbarTouchListener(View.OnTouchListener onTouchListener) {
        toolbar.setOnTouchListener(onTouchListener);
    }

}
