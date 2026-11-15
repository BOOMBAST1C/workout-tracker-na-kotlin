package com.fitrep.app.data

import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

private val monthsGenitive = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

private val weekdaysFull = listOf(
    "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье",
)

private val weekdaysShort = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

fun formatWeight(weight: Double): String =
    if (weight % 1.0 == 0.0) weight.roundToInt().toString()
    else String.format(Locale.US, "%.1f", weight)

fun formatNumber(value: Int): String {
    val digits = value.toString()
    val builder = StringBuilder()
    digits.forEachIndexed { index, char ->
        if (index > 0 && (digits.length - index) % 3 == 0) builder.append(' ')
        builder.append(char)
    }
    return builder.toString()
}

fun formatVolume(volume: Double): String = formatNumber(volume.roundToInt())

fun formatTimer(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

fun formatDuration(ms: Long): String {
    val totalMinutes = (ms / 60_000).coerceAtLeast(1)
    return if (totalMinutes < 60) {
        "$totalMinutes мин"
    } else {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        if (minutes == 0L) "$hours ч" else "$hours ч $minutes мин"
    }
}

fun formatRelativeDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> "${date.dayOfMonth} ${monthsGenitive[date.monthValue - 1].removeSuffix(".")}"
    }
}

fun formatLongDate(date: LocalDate): String =
    "${weekdaysFull[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${monthsGenitive[date.monthValue - 1]}"

fun weekdayShort(date: LocalDate): String = weekdaysShort[date.dayOfWeek.value - 1]

fun formatDateShort(date: LocalDate): String =
    "${date.dayOfMonth} ${monthsGenitive[date.monthValue - 1].take(3)}"

fun plural(count: Int, one: String, few: String, many: String): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

fun workoutsWord(count: Int): String = plural(count, "тренировка", "тренировки", "тренировок")

fun exercisesWord(count: Int): String = plural(count, "упражнение", "упражнения", "упражнений")

fun setsWord(count: Int): String = plural(count, "подход", "подхода", "подходов")

fun daysWord(count: Int): String = plural(count, "день", "дня", "дней")
