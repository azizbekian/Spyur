package com.azizbekian.spyur.activity.base;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * An abstract activity, that instantiates {@link CompositeSubscription} upon creation and performs
 * {@link CompositeSubscription#unsubscribe()} when the activity is being finished.
 * <p>
 * Created on Jun 18, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public abstract class RxBaseActivity extends BaseActivity {

    protected CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    protected void onPause() {
        if (isFinishing()) unsubscribe();
        super.onPause();
    }

    /**
     * Adds {@link Subscription} {@code s} to the {@link CompositeSubscription}.
     */
    @SuppressWarnings("unused")
    protected void addSubscription(Subscription s) {
        mSubscriptions.add(s);
    }

    /**
     * Unsubscribes from all current subscriptions.
     */
    protected void unsubscribe() {
        mSubscriptions.unsubscribe();
        // Once CompositeSubscription.unsubscribe() is called the object is unusable,
        // as it will automatically unsubscribe anything that is added to it afterwards!
        // A new CompositeSubscription must be created as a replacement.
        mSubscriptions = new CompositeSubscription();
    }

    /**
     * Returns true if this composite is not unsubscribed and contains subscriptions.
     *
     * @return {@code true} if this composite is not unsubscribed and contains subscriptions.
     */
    protected boolean hasSubscriptions() {
        return mSubscriptions.hasSubscriptions();
    }

}
