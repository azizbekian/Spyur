package com.azizbekian.spyur.activity.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.azizbekian.spyur.utils.ExceptionUtils;

import rx.subscriptions.CompositeSubscription;

/**
 * An abstract activity, that instantiates {@link CompositeSubscription} upon creation and performs
 * {@link CompositeSubscription#unsubscribe()} when the activity is being finished.
 * <p>
 * Created on Jun 18, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Checks, whether the {@link Bundle} of the current activity contains values with specified
     * {@code keys}.
     *
     * @param keys The keys to look for in the bundle.
     * @throws RuntimeException If any of the keys is not provided in the bundle.
     */
    @SuppressWarnings("unused")
    protected void checkInputAndThrow(String... keys) {
        if (!verifyInput(keys)) ExceptionUtils.throwWrongInput(getClass());
    }

    /**
     * Checks, whether the {@link Bundle} of the current activity contains values with specified
     * {@code keys}.
     *
     * @param keys The keys to look for in the bundle.
     * @return True, if the bundle contains all values with specified {@code keys}. False - if
     * there is at least one key, that bundle doesn't contain.
     */
    protected boolean checkInput(String... keys) {
        return verifyInput(keys);
    }

    private boolean verifyInput(String... keys) {
        Bundle input = getIntent().getExtras();
        if (null == input) return false;
        for (String key : keys) {
            if (!input.containsKey(key)) return false;
        }
        return true;
    }

}
