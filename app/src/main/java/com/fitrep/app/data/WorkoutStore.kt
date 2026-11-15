package com.fitrep.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class WorkoutStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _history = MutableStateFlow(readHistory())
    val history: StateFlow<List<WorkoutSession>> = _history.asStateFlow()

    private val _active = MutableStateFlow(readActive())
    val active: StateFlow<WorkoutSession?> = _active.asStateFlow()

    fun start(template: WorkoutTemplate) {
        _active.value = WorkoutSession(
            id = newId(),
            name = template.name,
            emoji = template.emoji,
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            exercises = template.exercises.map { planned ->
                ExerciseEntry(
                    id = newId(),
                    exerciseId = planned.exerciseId,
                    sets = List(planned.sets) {
                        SetEntry(id = newId(), weight = planned.weight, reps = planned.reps)
                    },
                )
            },
        )
        persistActive()
    }

    fun toggleSet(exerciseId: String, setId: String): Boolean {
        var becameDone = false
        updateActive { session ->
            session.copy(
                exercises = session.exercises.map { entry ->
                    if (entry.id != exerciseId) {
                        entry
                    } else {
                        entry.copy(
                            sets = entry.sets.map { set ->
                                if (set.id != setId) {
                                    set
                                } else {
                                    val done = !set.done
                                    if (done) becameDone = true
                                    set.copy(done = done)
                                }
                            },
                        )
                    }
                },
            )
        }
        return becameDone
    }

    fun setWeight(exerciseId: String, setId: String, weight: Double) {
        editSet(exerciseId, setId) { it.copy(weight = weight.coerceIn(0.0, 500.0)) }
    }

    fun setReps(exerciseId: String, setId: String, reps: Int) {
        editSet(exerciseId, setId) { it.copy(reps = reps.coerceIn(1, 200)) }
    }

    fun addSet(exerciseId: String) {
        updateActive { session ->
            session.copy(
                exercises = session.exercises.map { entry ->
                    if (entry.id != exerciseId) return@map entry
                    val last = entry.sets.lastOrNull()
                    entry.copy(
                        sets = entry.sets + SetEntry(
                            id = newId(),
                            weight = last?.weight ?: 20.0,
                            reps = last?.reps ?: 10,
                        ),
                    )
                },
            )
        }
    }

    fun deleteSet(exerciseId: String, setId: String) {
        updateActive { session ->
            session.copy(
                exercises = session.exercises.map { entry ->
                    if (entry.id != exerciseId) {
                        entry
                    } else {
                        entry.copy(sets = entry.sets.filterNot { it.id == setId })
                    }
                },
            )
        }
    }

    fun addExercise(exerciseId: String) {
        updateActive { session ->
            if (session.exercises.any { it.exerciseId == exerciseId }) return@updateActive session
            val weight = suggestedWeight(exerciseId)
            val entry = ExerciseEntry(
                id = newId(),
                exerciseId = exerciseId,
                sets = List(3) { SetEntry(id = newId(), weight = weight, reps = 10) },
            )
            session.copy(exercises = session.exercises + entry)
        }
    }

    fun deleteExercise(entryId: String) {
        updateActive { session ->
            session.copy(exercises = session.exercises.filterNot { it.id == entryId })
        }
    }

    fun finish(): WorkoutSession? {
        val current = _active.value ?: return null
        val finished = current.copy(finishedAt = System.currentTimeMillis())
        _history.value = listOf(finished) + _history.value
        _active.value = null
        persistHistory()
        prefs.edit().remove(KEY_ACTIVE).apply()
        return finished
    }

    fun discard() {
        _active.value = null
        prefs.edit().remove(KEY_ACTIVE).apply()
    }

    fun suggestedWeight(exerciseId: String): Double {
        history.value.forEach { session ->
            session.exercises
                .filter { it.exerciseId == exerciseId }
                .forEach { entry ->
                    entry.sets.lastOrNull { it.done }?.let { return it.weight }
                }
        }
        return 20.0
    }

    private fun editSet(exerciseId: String, setId: String, transform: (SetEntry) -> SetEntry) {
        updateActive { session ->
            session.copy(
                exercises = session.exercises.map { entry ->
                    if (entry.id != exerciseId) {
                        entry
                    } else {
                        entry.copy(
                            sets = entry.sets.map { set -> if (set.id == setId) transform(set) else set },
                        )
                    }
                },
            )
        }
    }

    private fun updateActive(transform: (WorkoutSession) -> WorkoutSession) {
        val current = _active.value ?: return
        _active.value = transform(current)
        persistActive()
    }

    private fun persistActive() {
        val session = _active.value
        if (session == null) {
            prefs.edit().remove(KEY_ACTIVE).apply()
        } else {
            prefs.edit().putString(KEY_ACTIVE, session.toJson().toString()).apply()
        }
    }

    private fun persistHistory() {
        val array = JSONArray()
        _history.value.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    private fun readHistory(): List<WorkoutSession> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.getJSONObject(index).toSession() }
        }.getOrDefault(emptyList())
    }

    private fun readActive(): WorkoutSession? {
        val raw = prefs.getString(KEY_ACTIVE, null) ?: return null
        return runCatching { JSONObject(raw).toSession() }.getOrNull()
    }

    private fun WorkoutSession.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("emoji", emoji)
        put("startedAt", startedAt)
        put("finishedAt", finishedAt ?: JSONObject.NULL)
        put(
            "exercises",
            JSONArray().apply {
                exercises.forEach { entry ->
                    put(
                        JSONObject().apply {
                            put("id", entry.id)
                            put("exerciseId", entry.exerciseId)
                            put(
                                "sets",
                                JSONArray().apply {
                                    entry.sets.forEach { set ->
                                        put(
                                            JSONObject().apply {
                                                put("id", set.id)
                                                put("weight", set.weight)
                                                put("reps", set.reps)
                                                put("done", set.done)
                                            },
                                        )
                                    }
                                },
                            )
                        },
                    )
                }
            },
        )
    }

    private fun JSONObject.toSession(): WorkoutSession {
        val exercisesJson = optJSONArray("exercises") ?: JSONArray()
        val exercises = List(exercisesJson.length()) { index ->
            val entryJson = exercisesJson.getJSONObject(index)
            val setsJson = entryJson.optJSONArray("sets") ?: JSONArray()
            ExerciseEntry(
                id = entryJson.getString("id"),
                exerciseId = entryJson.getString("exerciseId"),
                sets = List(setsJson.length()) { setIndex ->
                    val setJson = setsJson.getJSONObject(setIndex)
                    SetEntry(
                        id = setJson.getString("id"),
                        weight = setJson.optDouble("weight", 0.0),
                        reps = setJson.optInt("reps", 10),
                        done = setJson.optBoolean("done", false),
                    )
                },
            )
        }
        return WorkoutSession(
            id = getString("id"),
            name = getString("name"),
            emoji = optString("emoji", "\uD83D\uDCAA"),
            startedAt = optLong("startedAt", System.currentTimeMillis()),
            finishedAt = if (isNull("finishedAt")) null else optLong("finishedAt"),
            exercises = exercises,
        )
    }

    private fun newId(): String = UUID.randomUUID().toString()

    private companion object {
        const val PREFS = "rep_store"
        const val KEY_HISTORY = "history"
        const val KEY_ACTIVE = "active_session"
    }
}
