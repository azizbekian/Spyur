package com.incentive.yellowpages.ui.base

import android.content.Intent
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.CheckResult
import android.view.MenuItem
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class BaseContract {

    interface Presenter<in V : View> {

        fun attachView(view: V)

        fun detachView(isChangingConfigurations: Boolean)

        fun create(view: V, savedInstanceState: Bundle? = null, intent: Intent? = null, arguments: Bundle? = null, isPortrait: Boolean = false)

        fun viewCreated(savedInstanceState: Bundle? = null)

        fun start()

        fun resume()

        fun pause()

        fun stop()

        fun destroy(isFinishing: Boolean)

        fun activityReenter(resultCode: Int, data: Intent?)

        fun destroyView()

        fun saveInstanceState(outState: Bundle?, isFinishing: Boolean = false, isChangingConfigurations: Boolean = false, isPortrait: Boolean = false)

        fun restoreInstanceState(savedInstanceState: Bundle)

        fun addDisposable(disposable: Disposable)

        fun unsubscribe()

        fun backPressed()

        fun activityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean

        fun optionsItemSelected(menuItem: MenuItem): Boolean

        fun activityCreated(savedInstanceState: Bundle?)

        fun permissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) : Boolean

    }

    open class BasePresenter<T : View> : Presenter<T> {

        var view: T? = null
            private set
        private var compositeDisposable = CompositeDisposable()

        override fun attachView(view: T) {
            this.view = view
        }

        override fun detachView(isChangingConfigurations: Boolean) {
            view = null
        }

        val isViewAttached: Boolean
            @CheckResult
            get() = view != null

        @CallSuper
        override fun create(view: T, savedInstanceState: Bundle?, intent: Intent?, arguments: Bundle?, isPortrait: Boolean) {
            attachView(view)
        }

        override fun start() {

        }

        override fun resume() {

        }

        override fun pause() {

        }

        override fun stop() {

        }

        override fun destroy(isFinishing: Boolean) {

        }

        override fun activityReenter(resultCode: Int, data: Intent?) {
            
        }

        override fun viewCreated(savedInstanceState: Bundle?) {

        }

        override fun destroyView() {

        }

        override fun saveInstanceState(outState: Bundle?, isFinishing: Boolean, isChangingConfigurations: Boolean, isPortrait: Boolean) {
        }

        override fun restoreInstanceState(savedInstanceState: Bundle) {

        }

        override fun addDisposable(disposable: Disposable) {
            compositeDisposable.add(disposable)
        }

        override fun unsubscribe() {
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
        }

        override fun backPressed() {

        }

        override fun activityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
            return false
        }

        override fun activityCreated(savedInstanceState: Bundle?) {

        }

        override fun optionsItemSelected(menuItem: MenuItem): Boolean {
            return false
        }

        override fun permissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) : Boolean {
            return false
        }
    }

    interface View

}
