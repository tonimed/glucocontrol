package com.glucocontrol.presentation.screen.readingdetail

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class DetailPeriod(val label: String) {
    THREE_DAYS("3 días"),
    WEEKLY("Semana"),
    MONTHLY("Mes"),
}

/** Calcula el rango [desde, hasta] para cada periodo anclado en [anchor]. */
fun DetailPeriod.dateRange(anchor: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
    DetailPeriod.THREE_DAYS -> anchor.minusDays(2) to anchor

    DetailPeriod.WEEKLY -> {
        val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        monday to monday.plusDays(6)
    }

    DetailPeriod.MONTHLY -> {
        val first = LocalDate.of(anchor.year, anchor.monthValue, 1)
        first to first.withDayOfMonth(anchor.month.length(anchor.isLeapYear))
    }
}
