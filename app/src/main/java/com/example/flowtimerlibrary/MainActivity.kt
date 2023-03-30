package com.example.flowtimerlibrary

import android.os.Bundle
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
import com.example.flowtimer.TimeTicker
import com.example.flowtimerlibrary.databinding.ActivityMainBinding
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ObsoleteCoroutinesApi::class)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isPlaying = false

    private var timerTicker: TimeTicker? = null

    private val pattern = "HH:mm:ss".toTimePattern()

    var timeInfo = TimeInfo(0, 0)

    private var timeRecordAdapter: TimeRecordAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.rv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            timeRecordAdapter = TimeRecordAdapter()
            adapter = timeRecordAdapter
        }

        binding.imgPlayOrPause.setOnClickListener {
            isPlaying = !isPlaying
            binding.imgPlayOrPause.setImageResource(if (isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle)


            if (timerTicker == null) {
                if (timeInfo.hourOfDay == 0 && timeInfo.minute == 0) {
                    timerTicker = NaturalCountTimeTicker(this, true, countTimeInterval = 1000L)
                } else {
                    timerTicker = CountDownTimeTicker(this, false, countTimeInterval = 1000L, countDownTimeStart = pattern.turnDateTimeStringToDate(binding.txtTime.text.toString())?.time ?: 0L)
                }
                startObserve()
            }

            if (isPlaying) {
                timerTicker?.startCount()
            } else {
                timerTicker?.pauseCount()
            }
        }

        binding.imgAdd.setOnClickListener {
            timeRecordAdapter?.addData(binding.txtTime.text.toString())
            binding.rv.smoothScrollToPosition(timeRecordAdapter?.dataList?.indices?.last ?: 0)
        }

        binding.imgStop.setOnClickListener {
            isPlaying = false
            binding.imgPlayOrPause.setImageResource(R.drawable.ic_play_circle)
            timerTicker?.stopCount()
            timerTicker = null

            timeRecordAdapter?.dataList?.clear()
            timeRecordAdapter?.notifyDataSetChanged()
        }

        binding.txtTime.setOnClickListener {
            if (isPlaying) return@setOnClickListener

            CalendarDialogUtil.showCalendarTimeDialog(
                this, TimePickerType.NormalTimePicker(true, R.style.CustomTimePickerDialog)
            ) { view, hourOfDay, minute ->


                binding.imgStop.performClick()

                lifecycleScope.launch {
                    delay(50)
                    this@MainActivity.timeInfo = TimeInfo(hourOfDay, minute)
                    val calendar = Calendar.getInstance()
                    calendar.timeZone = pattern.timeZone
                    calendar.set(0, 0, 0, hourOfDay, minute, 0)
                    binding.txtTime.text = pattern.getCalenderString(calendar)
                }
            }
        }
    }


    private fun startObserve() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                timerTicker?.flowTimer?.collect {
                    binding.txtTime.text = pattern.turnMillisSecondIntoString(it)
                }
            }
        }
    }
}