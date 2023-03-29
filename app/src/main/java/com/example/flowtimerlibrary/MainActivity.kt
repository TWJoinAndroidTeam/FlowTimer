package com.example.flowtimerlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flowtimer.ObserveLifeCycleTimeTicker
import com.example.flowtimerlibrary.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ObsoleteCoroutinesApi::class)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isPlaying = false

    private val timerTicker = ObserveLifeCycleTimeTicker(this, true, Dispatchers.Main, 1L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                timerTicker.flowTimer.collect {
                    binding.txtTime.text = turnMillisSecondToSecond(it)
                }
            }
        }


        binding.imgPlayOrStop.setOnClickListener {
            isPlaying = !isPlaying
            binding.imgPlayOrStop.setImageResource(if (isPlaying) R.drawable.ic_stop else R.drawable.ic_play_circle)

            if (isPlaying) {
                timerTicker.startCount()
            } else {
                timerTicker.cancelCount()
            }
        }

        binding.imgAdd.setOnClickListener {

        }
    }


    fun turnMillisSecondToSecond(millisSecond: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(millisSecond))
    }
}