package com.fitrep.app.data

import java.time.LocalDate

data class DayVolume(
    val date: LocalDate,
    val label: String,
    val volume: Double,
)

data class StatsSummary(
    val totalWorkouts: Int,
    val weekWorkouts: Int,
    val weekVolume: Double,
    val totalVolume: Double,
    val totalSets: Int,
    val averageMinutes: Int,
    val streak: Int,
    val week: List<DayVolume>,
)

data class PersonalRecord(
    val exercise: Exercise,
    val weight: Double,
    val reps: Int,
)

fun computeStats(history: List<WorkoutSession>): StatsSummary {
    val today = LocalDate.now()
    val finished = history.filter { it.finishedAt != null }
    val weekStart = today.minusDays(6)
    val weekSessions = finished.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(today) }

    val week = (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        DayVolume(
            date = date,
            label = weekdayShort(date),
            volume = weekSessions.filter { it.date == date }.sumOf { it.volume },
        )
    }

    val dates = finished.map { it.date }.toSet()
    var streak = 0
    var cursor = if (dates.contains(today)) today else today.minusDays(1)
    while (dates.contains(cursor)) {
        streak++
        cursor = cursor.minusDays(1)
    }

    val totalMinutes = finished.sumOf { it.durationMs / 60_000 }

    return StatsSummary(
        totalWorkouts = finished.size,
        weekWorkouts = weekSessions.size,
        weekVolume = weekSessions.sumOf { it.volume },
        totalVolume = finished.sumOf { it.volume },
        totalSets = finished.sumOf { it.doneSets },
        averageMinutes = if (finished.isEmpty()) 0 else (totalMinutes / finished.size).toInt().coerceAtLeast(1),
        streak = streak,
        week = week,
    )
}

fun personalRecords(history: List<WorkoutSession>, limit: Int = 4): List<PersonalRecord> {
    val best = mutableMapOf<String, PersonalRecord>()
    history.filter { it.finishedAt != null }.forEach { session ->
        session.exercises.forEach { entry ->
            val exercise = Catalog.exercise(entry.exerciseId)
            entry.sets.filter { it.done && it.weight > 0 }.forEach { set ->
                val current = best[entry.exerciseId]
                val better = current == null ||
                    set.weight > current.weight ||
                    (set.weight == current.weight && set.reps > current.reps)
                if (better) {
                    best[entry.exerciseId] = PersonalRecord(exercise, set.weight, set.reps)
                }
            }
        }
    }
    return best.values.sortedByDescending { it.weight }.take(limit)
}
