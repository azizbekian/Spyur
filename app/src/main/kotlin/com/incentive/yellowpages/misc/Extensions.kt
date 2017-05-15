package com.incentive.yellowpages.misc

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.ResultReceiver
import android.support.annotation.CheckResult
import android.support.annotation.ColorRes
import android.support.annotation.LayoutRes
import android.support.annotation.TransitionRes
import android.support.v4.content.ContextCompat
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import com.incentive.yellowpages.utils.LogUtils

inline fun <reified T : Parcelable> createParcel(
        crossinline createFromParcel: (Parcel) -> T?): Parcelable.Creator<T> =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T? = createFromParcel(source)
            override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
        }

fun isPortrait(activity: Activity) = activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun hasVersionM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

inline fun View.waitForPreDraw(crossinline f: () -> Unit) = with(viewTreeObserver) {
    addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            f()
            return true
        }
    })
}

fun Context.getColorInt(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Context.isConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun View.showIme() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    try {
        val showSoftInputUnchecked = InputMethodManager::class.java.getMethod(
                "showSoftInputUnchecked", Int::class.javaPrimitiveType, ResultReceiver::class.java)
        showSoftInputUnchecked.isAccessible = true
        showSoftInputUnchecked.invoke(imm, 0, null)
    } catch (e: Exception) {
        LogUtils.e(e.message)
    }
}

fun View.hideIme() {
    val imm = context.getSystemService(Context
            .INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP) @CheckResult
fun Context.inflateTransition(@TransitionRes transitionId: Int) = TransitionInflater.from(this).inflateTransition(transitionId)

fun View.inflate(@LayoutRes layoutId: Int, root : ViewGroup? = null, attachToRoot: Boolean = false) = LayoutInflater.from(context).inflate(layoutId, root, attachToRoot)

