package com.surfing.inthe.wavepark.data.model

import java.time.LocalDate

data class Reservation(
    val number: String,
    val date: LocalDate,
    val applyDate: LocalDate,
    val product: String,
    val count: String,
    val status: String
) 