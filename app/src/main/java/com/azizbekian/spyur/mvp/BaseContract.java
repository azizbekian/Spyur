package com.azizbekian.spyur.mvp;

import rx.Subscription;

/**
 * Created on Jul 17, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
@SuppressWarnings("unused")
public interface BaseContract {

    interface View {

    }

    interface Presenter {

        /**
         * Called, when the hosting activity's or fragment's {@code onCreate()} has been called.
         */
        void create();

        /**
         * Called, when the hosting activity's or fragment's {@code onResume()} has been called.
         */
        void resume();

        /**
         * Called, when the hosting activity's or fragment's {@code onPause()} has been called.
         */
        void pause();

        /**
         * Called, when the hosting activity's or fragment's {@code onDestroy()} has been called.
         */
        void destroy();

        /**
         * Called, when the back button has been pressed.
         *
         * @return True - if the presenter has consumed this event. False otherwise.
         */
        boolean onBackPressed();

    }

    interface RxView extends View {

        /**
         * Adds {@link Subscription} {@code s} to the list of subscriptions.
         */
        void delegateAddSubscription(Subscription s);

        /**
         * Delegates unsubscription request to the hosting fragment/activity.
         */
        void delegateUnsubscribe();
    }

    interface Model {

    }

}
