package com.incentive.yellowpages.utils

import android.content.Context
import android.support.v4.view.animation.PathInterpolatorCompat
import android.transition.Transition
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator

class AnimUtils private constructor() {

    init {
        throw RuntimeException("Unable to instantiate class " + javaClass.canonicalName)
    }

    open class TransitionListenerAdapter : Transition.TransitionListener {

        override fun onTransitionStart(transition: Transition) {

        }

        override fun onTransitionEnd(transition: Transition) {

        }

        override fun onTransitionCancel(transition: Transition) {

        }

        override fun onTransitionPause(transition: Transition) {

        }

        override fun onTransitionResume(transition: Transition) {

        }
    }

    companion object {

        private var overshoot: Interpolator? = null
        val EASE_OUT_CUBIC = PathInterpolatorCompat.create(0.215f, 0.61f, 0.355f, 1f)!!

        fun getOvershootInterpolator(context: Context): Interpolator {
            if (overshoot == null) {
                overshoot = AnimationUtils.loadInterpolator(context,
                        android.R.interpolator.overshoot)
            }
            return overshoot!!
        }
    }

}
