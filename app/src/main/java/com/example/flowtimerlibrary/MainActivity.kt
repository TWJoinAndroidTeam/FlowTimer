package com.example.flowtimerlibrary

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.caledardialogcommander.model.TimeInfo
import com.example.caledardialogcommander.model.TimePickerType
import com.example.caledardialogcommander.ui.CalendarDialogUtil
import com.example.flowtimer.CountDownTimeTicker
import com.example.flowtimer.NaturalCountTimeTicker
import com.example.flowtimer.Counter
import com.example.flowtimerlibrary.databinding.ActivityMainBinding
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ObsoleteCoroutinesApi::class)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timerTicker: Counter? = null

    private val pattern = "HH:mm:ss".toTimePattern()

    var timeInfo = TimeInfo(0, 0)

    private var timeRecordAdapter: TimeRecordAdapter? = null

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initObserve()

        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            timeRecordAdapter = TimeRecordAdapter()
            adapter = timeRecordAdapter
        }

        initListener()
    }

    private fun initListener() {
        binding.imgPlayOrPause.setOnClickListener {
            mainViewModel.playOrPause()
        }

        binding.imgAdd.setOnClickListener {
            mainViewModel.addRecord(binding.txtTime.text.toString())
        }

        binding.imgStop.setOnClickListener {
            mainViewModel.stop()
        }

        binding.txtTime.setOnClickListener {
            mainViewModel.changeTime()
        }
    }


    private fun initObserve() {
        mainViewModel.flowNewRecord.launchWhenCreatedFlow(this) {
            timeRecordAdapter?.addData(it)
            binding.rv.smoothScrollToPosition(timeRecordAdapter?.dataList?.indices?.last ?: 0)
        }

        mainViewModel.flowShowCalendar.launchWhenCreatedFlow(this) {
            timeInfo = CalendarDialogUtil.waitCalendarTimeDialogResponse(
                this, TimePickerType.NormalTimePicker(true, R.style.CustomTimePickerDialog)
            ) { }
            val calendar = Calendar.getInstance()
            calendar.timeZone = pattern.timeZone
            calendar.set(0, 0, 0, timeInfo.hourOfDay, timeInfo.minute, 0)
            binding.txtTime.text = pattern.getCalenderString(calendar)
        }

        mainViewModel.flowPlayState.launchWhenCreatedFlow(this) {

            when (it) {
                is PlayState.Pause -> {
                    timerTicker?.pauseCount()

                    binding.imgPlayOrPause.setImageResource(R.drawable.ic_play_circle)
                }

                is PlayState.Playing -> {

                    if (timerTicker == null) {
                        timerTicker = if (timeInfo.hourOfDay == 0 && timeInfo.minute == 0) {
                            NaturalCountTimeTicker(countTimeInterval = 1000L).let {
                                it.addLifecycleObserve(this, false)
                                it
                            }
                        } else {
                            CountDownTimeTicker(countTimeInterval = 1000L, countDownTimeStart = pattern.turnDateTimeStringToDate(binding.txtTime.text.toString())?.time ?: 0L).let {
                                it.addLifecycleObserve(this, false)
                                it
                            }
                        }
                        startTimerObserve()
                    }
                    timerTicker?.resumeCount()


                    binding.imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle)
                }

                is PlayState.Stop -> {

                    timerTicker?.stopCount()
                    timerTicker = null

                    binding.imgPlayOrPause.setImageResource(R.drawable.ic_play_circle)

                    timeRecordAdapter?.dataList?.clear()
                    timeRecordAdapter?.notifyDataSetChanged()
                }
            }

        }

    }

    private fun startTimerObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                timerTicker?.flowTimer?.collect {
                    binding.txtTime.text = pattern.turnMillisSecondIntoString(it)
                }
            }
        }
    }
}