package com.example.flowtimerlibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {


    val flowPlayState = MutableStateFlow<PlayState>(PlayState.Stop)

    val flowShowCalendar = MutableSharedFlow<Boolean>()

    val flowNewRecord = MutableSharedFlow<String>()

    fun stop() {
        flowPlayState.update {
            PlayState.Stop
        }
    }

    fun addRecord(newRecord: String) {
        if (flowPlayState.value is PlayState.Playing) {
            viewModelScope.launch {
                flowNewRecord.emit(newRecord)
            }
        }
    }

    fun changeTime() {
        if (flowPlayState.value is PlayState.Stop) {
            viewModelScope.launch {
                flowShowCalendar.emit(true)
            }
        }
    }

    fun playOrPause() {
        flowPlayState.update {
            when (it) {
                is PlayState.Pause -> PlayState.Playing
                is PlayState.Playing -> PlayState.Pause
                is PlayState.Stop -> PlayState.Playing
            }
        }
    }

}
