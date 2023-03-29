package com.example.flowtimer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.TickerMode
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * @property countTimeInterval 每一次間格的計算單位，預設為1秒
 * @property countDownTimeStart 從多少時間開始算，假如不設置則為自然計數
 */
@ObsoleteCoroutinesApi
abstract class TimeTicker(private val coroutineContext: CoroutineContext = Dispatchers.Main, countTimeInterval: Long? = null, countDownTimeStart: Long? = null) {

    private var countTimeInterval = 1000L
    protected var countDownTimeStart: Long? = null

    val flowTimer = MutableSharedFlow<Long>()
    var countJob: Job? = null

    protected var nowTime = 0L

    private var receiveChannel: ReceiveChannel<Unit>? = null


    init {
        setCountDownTimer(countTimeInterval, countDownTimeStart)
    }

    /**
     * @param countTimeInterval 每一次間格的計算單位，預設為1秒
     * @param countDownTimeStart 從多少時間開始算，假如不設置則為自然計數
     */
    private fun setCountDownTimer(countTimeInterval: Long? = null, countDownTimeStart: Long? = null) {
        if (countTimeInterval != null) {
            this.countTimeInterval = countTimeInterval
        }
        this.countDownTimeStart = countDownTimeStart
    }

    open fun startCount() {
        cancelCount()
        nowTime = countDownTimeStart ?: 0
        countAction()
    }

    protected open fun countAction() {
        controlCount()
    }

    fun cancelCount() {
        countJob?.cancel()
        receiveChannel?.cancel()
    }

    @ObsoleteCoroutinesApi
    private fun tickerFlow(l: Long, context: CoroutineContext): Flow<Unit> {
        receiveChannel?.cancel()

        receiveChannel = ticker(l, 0, context, TickerMode.FIXED_DELAY)

        return receiveChannel!!.receiveAsFlow()
    }


    protected fun controlCount() {
        val coroutineScope = CoroutineScope(coroutineContext)
        countJob = coroutineScope.launch {
            tickerFlow(countTimeInterval, this.coroutineContext).onEach {
                when {
                    countDownTimeStart != null -> {
                        nowTime = nowTime.minus(countTimeInterval).coerceAtLeast(0)
                        if (nowTime >= 0) {
                            flowTimer.emit(nowTime)
                            if (nowTime == 0L) cancelCount()
                        } else cancelCount()
                    }
                    else -> {
                        nowTime = nowTime.plus(countTimeInterval)
                        flowTimer.emit(nowTime)
                    }
                }
            }.launchIn(this)
        }
        if (countJob?.isCancelled == true) countJob?.start()
    }

}