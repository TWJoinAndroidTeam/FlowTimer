package com.example.flowtimer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.coroutines.CoroutineContext


/**
 * 用於正向記時的ticker，沒有關掉的話，會一直記時下去
 * @param shouldAddTimeAfterOnPause 是否需要計算 pause APP的時間
 */
@OptIn(ObsoleteCoroutinesApi::class)
class NaturalCountTimeTicker(
    coroutineContext: CoroutineContext = Dispatchers.Main,
    countTimeInterval: Long? = null
) : Counter(coroutineContext, countTimeInterval), LifecycleEventObserver {

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

    override fun getNewNowTime(oloNowTime: Long): Long {

        val currentTime = when {
            shouldAddTimeAfterOnPause && systemTimeOnPause != null -> {
                val firstValueWhenBackResume = oloNowTime.plus(getTimeNeedAdd()).plus(countTimeInterval)
                systemTimeOnPause = null
                firstValueWhenBackResume
            }

            else -> oloNowTime.plus(countTimeInterval)
        }

        return currentTime
    }

    private fun getTimeNeedAdd(): Long {

        val systemTimeNow = System.currentTimeMillis()

        return systemTimeNow - systemTimeOnPause!!
    }

    private fun onLifeResume() {
        if (isCancelByUser == false) countAction()
    }

    private fun onLifePause() {

        if (isCancelByUser == true) return

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