package com.example.flowtimerlibrary

sealed class PlayState {

    object Stop : PlayState()

    object Playing : PlayState()

    object Pause : PlayState()

}
