package ru.otus.eggtimer.ui.main

sealed interface TimerState {
    data class Default(val time: String) : TimerState
    data class Running(val time: String) : TimerState
    object Done : TimerState
}