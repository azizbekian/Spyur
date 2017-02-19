package com.incentive.yellowpages.utils

import com.incentive.yellowpages.BuildConfig
import timber.log.Timber

@Suppress("NOTHING_TO_INLINE")
class LogUtils private constructor() {

    init {
        throw RuntimeException("Unable to instantiate class " + javaClass.canonicalName)
    }

    companion object {

        val PREFIX = "vvv: "

        val IS_DEBUGGABLE = BuildConfig.DEBUG

        inline fun wtf(msg: String?) {
            if (IS_DEBUGGABLE) Timber.wtf(PREFIX + msg)
        }

        inline fun i(msg: String?) {
            if (IS_DEBUGGABLE) Timber.i(PREFIX + msg)
        }

        inline fun e(msg: String?) {
            if (IS_DEBUGGABLE) Timber.e(PREFIX + msg)
        }

        inline fun wtf(msg: Int?) {
            wtf("" + msg)
        }

        inline fun i(msg: Int?) {
            i("" + msg)
        }

        inline fun e(msg: Int?) {
            e("" + msg)
        }

        inline fun wtf(msg: Boolean?) {
            wtf("" + msg)
        }

        inline fun i(msg: Boolean?) {
            i("" + msg)
        }

        inline fun e(msg: Boolean?) {
            e("" + msg)
        }

        inline fun wtf(msg: Float?) {
            wtf("" + msg)
        }

        inline fun i(msg: Float?) {
            i("" + msg)
        }

        inline fun e(msg: Float?) {
            e("" + msg)
        }
    }
}
