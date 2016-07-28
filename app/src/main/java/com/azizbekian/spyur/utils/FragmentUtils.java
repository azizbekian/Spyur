package com.azizbekian.spyur.utils;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Utility class for fragment transactions.
 * <p>
 * Created by Joomag, Inc., on June 16, 2016.
 *
 * @author Andranik Azizbekian (a.azizbekyan@joomag.com)
 */
@SuppressWarnings("unused")
public class FragmentUtils {

    private FragmentUtils() {
        throw new RuntimeException("Unable to instantiate class " + getClass().getCanonicalName());
    }

    /**
     * This method instantiates provided {@code fragmentClass} with optional {@code fragmentBundle}
     * attached to fragment, and adds instantiated fragment to activity's {@code frameResId} with
     * optional {@code tag}.
     *
     * @param activity       - hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param fragmentClass  - The fragment class, that should be instantiated
     * @param fragmentBundle - This bundle gets supplied to the fragment
     * @param tag            - Fragment transaction's tag
     */
    public static void addRootFragment(FragmentActivity activity, @IdRes int frameResId,
                                       Class fragmentClass, @Nullable Bundle fragmentBundle,
                                       @Nullable String tag) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(frameResId, Fragment.instantiate(activity, fragmentClass.getName(), fragmentBundle), tag);
        ft.commitAllowingStateLoss();
    }

    /**
     * This method instantiates provided {@code fragmentClass} with no bundle attached to fragment,
     * and adds instantiated fragment to activity's {@code frameResId} with optinal {@code tag}.
     *
     * @param activity      - hosting activity
     * @param frameResId    - Layout id where fragment should be placed
     * @param fragmentClass - The fragment class, that should be instantiated
     * @param tag           - Fragment transaction's tag
     */
    public static void addRootFragment(FragmentActivity activity, @IdRes int frameResId,
                                       Class fragmentClass, @Nullable String tag) {

        addRootFragment(activity, frameResId, fragmentClass, null, tag);
    }

    /**
     * This method adds {@code fragment} to activity's {@code frameResId} with optinal {@code tag}.
     *
     * @param activity   - Hosting activity
     * @param frameResId - Layout id where fragment should be placed
     * @param fragment   - Fragment instance
     * @param tag        - Fragment transaction's tag
     */
    public static void addRootFragment(FragmentActivity activity, @IdRes int frameResId,
                                       Fragment fragment, @Nullable String tag) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(frameResId, fragment, tag);
        ft.commitAllowingStateLoss();
    }

    /**
     * Replaces existing fragment with a {@link Fragment}, that gets instantiated from {@code clazz}
     * with optional {@code fragmentBundle} attached.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param clazz          - Fragment's class that should be instantiated
     * @param fragmentBundle - This bundle gets supplied to the fragment
     * @param addToBackStack - If true transaction would be added to back stack.
     * @param tag            - Fragment transaction's tag
     */
    public static void replace(FragmentActivity activity, @IdRes int frameResId, Class clazz,
                               @Nullable Bundle fragmentBundle, boolean addToBackStack, @Nullable String tag) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(frameResId, Fragment.instantiate(activity, clazz.getName(), fragmentBundle), tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    /**
     * Replaces existing fragment with {@code fragment}.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param fragment       - Fragment instance
     * @param addToBackStack - If true transaction would be added to back stack.
     * @param tag            - Fragment transaction's tag
     */
    public static void replace(FragmentActivity activity, @IdRes int frameResId, Fragment fragment,
                               boolean addToBackStack, @Nullable String tag) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(frameResId, fragment, tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    /**
     * Replaces existing fragment with {@code fragment} applying animation.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param fragment       - Fragment instance
     * @param addToBackStack - If true transaction would be added to back stack.
     * @param tag            - Fragment transaction's tag
     */
    public static void replaceWithAnim(FragmentActivity activity, @IdRes int frameResId,
                                       Fragment fragment, boolean addToBackStack, @Nullable String tag,
                                       @AnimRes int animIn, @AnimRes int animOut) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(animIn, animOut);
        ft.replace(frameResId, fragment, tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.commitAllowingStateLoss();
    }

    /**
     * Instantiates a fragment from {@code clazz} with optional {@code bundle} supplied, and adds
     * the fragment to {@code frameResId}.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param clazz          - Fragment's class that should be instantiated
     * @param bundle         - This bundle gets supplied to the fragment
     * @param addToBackStack - If true transaction would be added to back stack.
     * @param tag            - Fragment transaction's tag
     */
    public static void add(FragmentActivity activity, @IdRes int frameResId, Class clazz,
                           @Nullable Bundle bundle, boolean addToBackStack, @Nullable String tag) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(frameResId, Fragment.instantiate(activity, clazz.getName(), bundle), tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    /**
     * Adds provided {@code fragment} to {@code frameResId}.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param fragment       - Fragment instance
     * @param addToBackStack - If true transaction would be added to back stack.
     * @param tag            - Fragment transaction's tag
     */
    public static void add(FragmentActivity activity, @IdRes int frameResId, Fragment fragment,
                           boolean addToBackStack, @Nullable String tag) {

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(frameResId, fragment, tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    /**
     * Adds provided {@code fragment} to {@code frameResId}.
     *
     * @param activity       - Hosting activity
     * @param frameResId     - Layout id where fragment should be placed
     * @param fragment       - Fragment instance
     * @param addToBackStack - If true transaction would be added to back stack.
     */
    public static void add(FragmentActivity activity, @IdRes int frameResId, Fragment fragment,
                           boolean addToBackStack) {

        add(activity, frameResId, fragment, addToBackStack, null);
    }

    /**
     * Adds provided {@code fragment} to {@code frameResId}.
     *
     * @param activity   - Hosting activity
     * @param frameResId - Layout id where fragment should be placed
     * @param fragment   - Fragment instance
     */
    public static void add(FragmentActivity activity, @IdRes int frameResId, Fragment fragment) {
        add(activity, frameResId, fragment, false, null);
    }

        /**
     * Adds provided {@code fragment} to {@code frameResId} without adding to backstack.
     *
     * @param activity-   Hosting activity
     * @param frameResId- Layout id where fragment should be placed
     * @param fragment-   Fragment instance
     * @param tag-        If true transaction would be added to back stack.
     */
    public static void add(FragmentActivity activity, @IdRes int frameResId, Fragment fragment, @Nullable String tag) {
        add(activity, frameResId, fragment, false, tag);
    }

    /**
     * Pops off the last fragment in the provided activity's stack.
     *
     * @param activity The activity which fragment's stack will be popped
     */
    public static void pop(FragmentActivity activity) {
        activity.getSupportFragmentManager().popBackStack();
    }

    /**
     * Add {@code fragment} as a child to {@code rootFragment}.
     */
    public static void addChild(Fragment rootFragment, @IdRes int frameResId, Fragment fragment,
                                boolean addToBackStack, @Nullable String tag) {

        FragmentTransaction ft = rootFragment.getChildFragmentManager().beginTransaction();
        ft.add(frameResId, fragment, tag);
        if (addToBackStack) ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss();
    }

    /**
     * Searches for child fragment in {@code fragment} by provided {@code tag}.
     *
     * @param fragment Fragment to search in.
     * @param tag      The tag, to perform search by.
     * @return The {@link Fragment} if there's a such fragment in {@link android.support.v4.app.FragmentManager}.
     * Null otherwise.
     */
    @SuppressWarnings("unchecked")
    @CheckResult
    @Nullable
    public static <T extends Fragment> T findChildByTag(Fragment fragment, @NonNull String tag) {
        return (T) fragment.getChildFragmentManager().findFragmentByTag(tag);
    }

    /**
     * Add {@code fragment} as a child to {@code rootFragment}.
     */
    public static void addChild(Fragment rootFragment, @IdRes int frameResId, Fragment fragment, @Nullable String tag) {
        addChild(rootFragment, frameResId, fragment, false, tag);
    }

    /**
     * Searches for fragment in {@link android.support.v4.app.FragmentManager} by provided {@code tag}.
     *
     * @param activity Activity to search in.
     * @param tag      The tag, to perform search by.
     * @return The {@link Fragment} if there's a such fragment in {@link android.support.v4.app.FragmentManager}.
     * Null otherwise.
     */
    @SuppressWarnings("unchecked")
    @CheckResult
    @Nullable
    public static <T extends Fragment> T findByTag(FragmentActivity activity, @NonNull String tag) {
        return (T) activity.getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * Searches for fragment in {@link android.support.v4.app.FragmentManager} by provided {@code id}.
     *
     * @param activity Activity to search in.
     * @param id       The id, to perform search by.
     * @return The {@link Fragment} if there's a such fragment in {@link android.support.v4.app.FragmentManager}.
     * Null otherwise.
     */
    @SuppressWarnings("unchecked")
    @CheckResult
    @Nullable
    public static <T extends Fragment> T findById(FragmentActivity activity, @IdRes int id) {
        return (T) activity.getSupportFragmentManager().findFragmentById(id);
    }

    /**
     * Display the dialog, adding the fragment to the given FragmentManager.  This
     * is a convenience for explicitly creating a transaction, adding the
     * fragment to it with the given tag, and committing it.  This does
     * <em>not</em> add the transaction to the back stack.  When the fragment
     * is dismissed, a new transaction will be executed to remove it from
     * the activity.
     *
     * @param activity The Activity where the fragment should be shown
     * @param tag      The tag for this fragment, as per
     *                 {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     */
    public static void showDialog(FragmentActivity activity, DialogFragment dialogFragment, @Nullable String tag) {
        dialogFragment.show(activity.getSupportFragmentManager(), tag);
    }

    /**
     * Display the dialog, adding the fragment to the given FragmentManager.  This
     * is a convenience for explicitly creating a transaction, adding the
     * fragment to it with the given tag, and committing it.  This does
     * <em>not</em> add the transaction to the back stack.  When the fragment
     * is dismissed, a new transaction will be executed to remove it from
     * the activity.
     *
     * @param activity The Activity where the fragment should be shown
     */
    public static void showDialog(FragmentActivity activity, DialogFragment dialogFragment) {
        showDialog(activity, dialogFragment, null);
    }

    /**
     * Removes fragment with tag <b>{@code tag}</b> from {@code activity}'s fragment manager if there
     * is such fragment.
     *
     * @param activity Hosting activity.
     * @param tag      The tag of the fragment.
     * @return True - if fragment is removed. False otherwise.
     */
    public static boolean removeByTag(FragmentActivity activity, @NonNull String tag) {
        Fragment fr = findByTag(activity, tag);
        if (null != fr) {
            activity.getSupportFragmentManager().beginTransaction()
                    .remove(fr)
                    .commit();
            return true;
        }
        return false;

    }

    /**
     * Removes fragment with id <b>{@code id}</b> from {@code activity}'s fragment manager if there
     * is such fragment.
     *
     * @param activity Hosting activity.
     * @param id       The id of the fragment.
     * @return True - if fragment is removed. False otherwise.
     */
    public static boolean removeById(FragmentActivity activity, @IdRes int id) {
        Fragment fr = findById(activity, id);
        if (null != fr) {
            activity.getSupportFragmentManager().beginTransaction()
                    .remove(fr)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Shows the {@code fragment}.
     */
    public static void show(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .show(fragment)
                .commit();
    }

    /**
     * Hides the {@code fragment}.
     */
    public static void hide(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .hide(fragment)
                .commit();
    }

    /**
     * Hides the {@code hide} fragment and shows the {@code show} fragment.
     */
    public static void hideAndShow(FragmentActivity activity, Fragment hide, Fragment show) {
        activity.getSupportFragmentManager().beginTransaction()
                .hide(hide)
                .show(show)
                .commit();
    }

    /**
     * Hides the {@code hide} fragment and shows the {@code show} fragment with animations.
     */
    public static void hideAndShowWithAnimation(FragmentActivity activity, Fragment hide, Fragment show,
                                                @AnimRes int animIn, @AnimRes int animOut) {
        activity.getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(animIn, animOut)
                .hide(hide)
                .show(show)
                .commit();
    }

}
