package com.example.flowtimer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.coroutines.CoroutineContext

/**
 * 倒計時的timer，當倒計時為0時，會取消計時行為
 * @param shouldAddTimeAfterOnPause 是否需要計算 pause APP的時間
 */
@ObsoleteCoroutinesApi
class CountDownTimeTicker(
    coroutineContext: CoroutineContext = Dispatchers.Main, countTimeInterval: Long? = null, countDownTimeStart: Long
) : Counter(coroutineContext, countTimeInterval, countDownTimeStart), LifecycleEventObserver {

    private var systemTimeOnPause: Long? = null
    private var shouldAddTimeAfterOnPause: Boolean = false

    fun addLifecycleObserve(lifecycleOwner: LifecycleOwner, shouldAddTimeAfterOnPause: Boolean) {
        lifecycleOwner.lifecycle.addObserver(this)
        this.shouldAddTimeAfterOnPause = shouldAddTimeAfterOnPause
    }

    override fun startCount() {
        systemTimeOnPause = null
        super.startCount()
    }

    override fun getNewNowTime(oloNowTime: Long): Long? {

        val currentTime = when {
            shouldAddTimeAfterOnPause && systemTimeOnPause != null -> {
                val firstValueWhenBackResume = oloNowTime.minus(getTimeNeedAdd()).minus(countTimeInterval).coerceAtLeast(0)
                systemTimeOnPause = null
                firstValueWhenBackResume
            }

            else -> oloNowTime.minus(countTimeInterval)
        }

        return if (currentTime >= 0) currentTime else null
    }

    private fun getTimeNeedAdd(): Long {

        val systemTimeNow = System.currentTimeMillis()

        return systemTimeNow - systemTimeOnPause!!
    }

    private fun onLifeResume() {
        if (!isCancelByUser) countAction()
    }

    private fun onLifePause() {

        if (isCancelByUser) return

        if (shouldAddTimeAfterOnPause) systemTimeOnPause = System.currentTimeMillis()

        cancelCount(false)
    }

    private fun onLifeDestroy() {
        cancelCount(false)
        resetCount()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> {}
            Lifecycle.Event.ON_RESUME -> {
                onLifeResume()
            }

            Lifecycle.Event.ON_PAUSE -> {
                onLifePause()
            }

            Lifecycle.Event.ON_STOP -> {

            }

            Lifecycle.Event.ON_DESTROY -> {
                source.lifecycle.removeObserver(this)
                onLifeDestroy()
            }

            Lifecycle.Event.ON_ANY -> {}
        }
    }
}