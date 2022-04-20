package com.dipl.camerax.utils

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Used to get viewBinding object by exposing layoutInflater used as an argument for method ViewBinding.inflate() calling.
 * */
typealias bindViewBy<T> = (LayoutInflater) -> T

/**
 * Wrapper around main thread [android.os.Handler]
 * */
object MainHandler {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun post(runnable: () -> Unit) {
        mainHandler.post(runnable)
    }
}

/**
 * LifecycleObserver implementation which tracks onDestroy method of lifecycle
 * */
class OnDestroyViewObserver(private val onDestroyAction: () -> Unit) : LifecycleObserver {
    @OnLifecycleEvent(ON_DESTROY)
    fun onDestroy() = onDestroyAction()
}
