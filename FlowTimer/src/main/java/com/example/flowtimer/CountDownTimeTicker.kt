package com.example.flowtimer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.coroutines.CoroutineContext

/**
 * @param shouldAddTimeAfterOnPause 是否需要計算 pause APP的時間
 */
@ObsoleteCoroutinesApi
class CountDownTimeTicker(
    lifecycleOwner: LifecycleOwner, private val shouldAddTimeAfterOnPause: Boolean, coroutineContext: CoroutineContext = Dispatchers.Main, countTimeInterval: Long? = null, countDownTimeStart: Long
) : TimeTicker(coroutineContext, countTimeInterval, countDownTimeStart), LifecycleEventObserver {

    private var systemTimeOnPause: Long? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun startCount() {
        systemTimeOnPause = null
        super.startCount()
    }

    override fun getNowTime(countTimeInterval: Long, nowTime: Long): Long? {

        val currentTime = when {
            shouldAddTimeAfterOnPause && systemTimeOnPause != null -> {
                val firstValueWhenBackResume = nowTime.minus(getTimeNeedAdd()).minus(countTimeInterval).coerceAtLeast(0)
                systemTimeOnPause = null
                firstValueWhenBackResume
            }
            else -> nowTime.minus(countTimeInterval)
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
                onLifeDestroy()
            }
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}