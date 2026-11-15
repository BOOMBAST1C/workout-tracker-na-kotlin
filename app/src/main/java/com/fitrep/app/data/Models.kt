package com.fitrep.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class Movement {
    SQUAT,
    BENCH,
    ROW,
    DEADLIFT,
    PRESS,
    PULLUP,
    DIP,
    CURL,
    TRICEPS,
    LUNGE,
    LEG_PRESS,
    RDL,
    CALF,
    LATERAL,
    PLANK,
    CRUNCH,
    BURPEE,
    ROPE,
}

data class Exercise(
    val id: String,
    val name: String,
    val muscle: String,
    val emoji: String,
    val movement: Movement = Movement.PRESS,
)

data class TemplateExercise(
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
)

data class WorkoutTemplate(
    val id: String,
    val name: String,
    val subtitle: String,
    val emoji: String,
    val accentStart: Long,
    val accentEnd: Long,
    val exercises: List<TemplateExercise>,
)

data class SetEntry(
    val id: String,
    val weight: Double,
    val reps: Int,
    val done: Boolean = false,
)

data class ExerciseEntry(
    val id: String,
    val exerciseId: String,
    val sets: List<SetEntry>,
)

data class WorkoutSession(
    val id: String,
    val name: String,
    val emoji: String,
    val startedAt: Long,
    val finishedAt: Long?,
    val exercises: List<ExerciseEntry>,
) {
    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }

    val doneSets: Int
        get() = exercises.sumOf { entry -> entry.sets.count { it.done } }

    val volume: Double
        get() = exercises.sumOf { entry ->
            entry.sets.filter { it.done }.sumOf { it.weight * it.reps }
        }

    val durationMs: Long
        get() = ((finishedAt ?: System.currentTimeMillis()) - startedAt).coerceAtLeast(0L)

    val date: LocalDate
        get() = Instant.ofEpochMilli(startedAt).atZone(ZoneId.systemDefault()).toLocalDate()
}

object Catalog {

    val exercises: List<Exercise> = listOf(
        Exercise("squat", "Приседания со штангой", "Ноги", "\uD83E\uDDB5", Movement.SQUAT),
        Exercise("bench", "Жим лёжа", "Грудь", "\uD83C\uDFCB\uFE0F", Movement.BENCH),
        Exercise("row", "Тяга штанги в наклоне", "Спина", "\uD83D\uDEA3", Movement.ROW),
        Exercise("deadlift", "Становая тяга", "Спина", "\u26A1", Movement.DEADLIFT),
        Exercise("ohp", "Жим стоя", "Плечи", "\uD83D\uDCAA", Movement.PRESS),
        Exercise("pullup", "Подтягивания", "Спина", "\uD83E\uDDD7", Movement.PULLUP),
        Exercise("dip", "Брусья", "Грудь", "\uD83E\uDD38", Movement.DIP),
        Exercise("curl", "Подъём на бицепс", "Руки", "\uD83E\uDDBE", Movement.CURL),
        Exercise("triceps", "Разгибание на трицепс", "Руки", "\uD83E\uDD1B", Movement.TRICEPS),
        Exercise("lunge", "Выпады с гантелями", "Ноги", "\uD83D\uDEB6", Movement.LUNGE),
        Exercise("legpress", "Жим ногами", "Ноги", "\uD83E\uDDBF", Movement.LEG_PRESS),
        Exercise("rdl", "Румынская тяга", "Ноги", "\uD83E\uDDBA", Movement.RDL),
        Exercise("calf", "Подъёмы на икры", "Ноги", "\uD83E\uDDB6", Movement.CALF),
        Exercise("lateral", "Махи гантелями в стороны", "Плечи", "\uD83D\uDD4A\uFE0F", Movement.LATERAL),
        Exercise("plank", "Планка", "Кор", "\uD83E\uDDD8", Movement.PLANK),
        Exercise("crunch", "Скручивания", "Кор", "\uD83D\uDD25", Movement.CRUNCH),
        Exercise("burpee", "Бёрпи", "Кардио", "\uD83D\uDE80", Movement.BURPEE),
        Exercise("rope", "Скакалка", "Кардио", "\uD83E\uDEA2", Movement.ROPE),
    )

    val byId: Map<String, Exercise> = exercises.associateBy { it.id }

    fun exercise(id: String): Exercise = byId[id] ?: Exercise(id, id, "Другое", "\uD83C\uDFCB\uFE0F")

    val templates: List<WorkoutTemplate> = listOf(
        WorkoutTemplate(
            id = "fullbody",
            name = "Фулл-боди",
            subtitle = "Всё тело · 45 мин",
            emoji = "\uD83D\uDCAA",
            accentStart = 0xFFB6FF3B,
            accentEnd = 0xFF3BD98C,
            exercises = listOf(
                TemplateExercise("squat", 4, 8, 60.0),
                TemplateExercise("bench", 4, 8, 50.0),
                TemplateExercise("row", 3, 10, 40.0),
                TemplateExercise("ohp", 3, 10, 25.0),
                TemplateExercise("crunch", 3, 18, 0.0),
            ),
        ),
        WorkoutTemplate(
            id = "upper",
            name = "Верх тела",
            subtitle = "Грудь · спина · плечи",
            emoji = "\uD83C\uDFCB\uFE0F",
            accentStart = 0xFF7BD8FF,
            accentEnd = 0xFF6E7BFF,
            exercises = listOf(
                TemplateExercise("bench", 4, 8, 50.0),
                TemplateExercise("row", 4, 10, 40.0),
                TemplateExercise("pullup", 3, 8, 0.0),
                TemplateExercise("ohp", 3, 10, 25.0),
                TemplateExercise("curl", 3, 12, 12.0),
            ),
        ),
        WorkoutTemplate(
            id = "lower",
            name = "Низ тела",
            subtitle = "Ноги · ягодицы",
            emoji = "\uD83E\uDDB5",
            accentStart = 0xFFFFB86B,
            accentEnd = 0xFFFF5E7E,
            exercises = listOf(
                TemplateExercise("squat", 4, 8, 60.0),
                TemplateExercise("rdl", 3, 10, 50.0),
                TemplateExercise("lunge", 3, 12, 20.0),
                TemplateExercise("legpress", 3, 12, 80.0),
                TemplateExercise("calf", 4, 15, 40.0),
            ),
        ),
        WorkoutTemplate(
            id = "cardio",
            name = "Кардио + кор",
            subtitle = "Жиросжигание · 25 мин",
            emoji = "\uD83D\uDD25",
            accentStart = 0xFFFF7AD9,
            accentEnd = 0xFF9B6BFF,
            exercises = listOf(
                TemplateExercise("burpee", 4, 12, 0.0),
                TemplateExercise("rope", 4, 60, 0.0),
                TemplateExercise("crunch", 3, 20, 0.0),
                TemplateExercise("plank", 3, 60, 0.0),
            ),
        ),
    )
}
