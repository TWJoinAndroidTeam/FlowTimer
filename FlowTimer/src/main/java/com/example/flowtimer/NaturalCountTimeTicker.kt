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
    lifecycleOwner: LifecycleOwner,
    private val shouldAddTimeAfterOnPause: Boolean,
    coroutineContext: CoroutineContext = Dispatchers.Main,
    countTimeInterval: Long? = null
) : TimeTicker(coroutineContext, countTimeInterval), LifecycleEventObserver {

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
                val firstValueWhenBackResume = nowTime.plus(getTimeNeedAdd()).plus(countTimeInterval)
                systemTimeOnPause = null
                firstValueWhenBackResume
            }
            else -> nowTime.plus(countTimeInterval)
        }

        return currentTime
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