package ru.otus.eggtimer.ui.main

enum class EggState(val time: Long) {
    Soft(3 * 60 * 1000),
    Medium(7 * 60 * 1000),
    Hard(9 * 60 * 1000)
}