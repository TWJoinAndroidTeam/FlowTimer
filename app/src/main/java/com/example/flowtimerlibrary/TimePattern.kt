package com.example.flowtimerlibrary

import java.util.*

interface TimePattern {

    val pattern: String

    val timeZone: TimeZone

    val locale: Locale
}
