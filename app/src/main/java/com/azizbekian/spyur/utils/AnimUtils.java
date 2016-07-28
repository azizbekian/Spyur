package com.azizbekian.spyur.utils;

import android.content.Context;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.transition.Transition;
import android.util.Property;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Utility class for making animations.
 * <p>
 * Created on April 02, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class AnimUtils {

    private AnimUtils() {
        throw new RuntimeException("Unable to instantiate class " + getClass().getCanonicalName());
    }

    private static Interpolator fastOutSlowIn;
    private static Interpolator overshoot;
    public static final Interpolator EASE_OUT_CUBIC = PathInterpolatorCompat.create(0.215f, 0.61f, 0.355f, 1f);

    public static Interpolator getFastOutSlowInInterpolator(Context context) {
        if (fastOutSlowIn == null) {
            fastOutSlowIn = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.fast_out_slow_in);
        }
        return fastOutSlowIn;
    }

    public static Interpolator getOvershootInterpolator(Context context) {
        if (overshoot == null) {
            overshoot = AnimationUtils.loadInterpolator(context,
                    android.R.interpolator.overshoot);
        }
        return overshoot;
    }

    public static class TransitionListenerAdapter implements Transition.TransitionListener {

        @Override
        public void onTransitionStart(Transition transition) {

        }

        @Override
        public void onTransitionEnd(Transition transition) {

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    }

}
