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
class ObserveLifeCycleTimeTicker(
    lifecycleOwner: LifecycleOwner,
    private val shouldAddTimeAfterOnPause: Boolean,
    coroutineContext: CoroutineContext = Dispatchers.Main,
    countTimeInterval: Long? = null,
    countDownTimeStart: Long? = null
) :
    TimeTicker(coroutineContext, countTimeInterval, countDownTimeStart), LifecycleEventObserver {


    var isAlreadyStart = false

    private var systemTimeNow: Long? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun startCount() {
        systemTimeNow = null
        super.startCount()
    }

    override fun countAction() {

        if (shouldAddTimeAfterOnPause) {

            val saveTime = systemTimeNow

            if (saveTime != null) {
                val systemTimeNow = System.currentTimeMillis()
                nowTime -= (systemTimeNow - saveTime)
            }
        }

        super.countAction()
    }

    private fun onLifeResume() {
        //代表時間到
        if (countDownTimeStart != null && nowTime == 0L) return
        if (isAlreadyStart) countAction()
    }

    private fun onLifePause() {

        if (countJob?.isActive == false) return

        if (shouldAddTimeAfterOnPause) systemTimeNow = System.currentTimeMillis()

        if (countJob?.isActive == true) {
            isAlreadyStart = true
        }

        cancelCount()
    }

    private fun onLifeDestroy() {
        cancelCount()
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
            Lifecycle.Event.ON_STOP -> {}
            Lifecycle.Event.ON_DESTROY -> {
                onLifeDestroy()
            }
            Lifecycle.Event.ON_ANY -> {}
        }
    }
}