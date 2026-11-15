package com.fitrep.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitrep.app.data.Catalog
import com.fitrep.app.data.ExerciseEntry
import com.fitrep.app.data.SetEntry
import com.fitrep.app.data.WorkoutSession
import com.fitrep.app.data.WorkoutStore
import com.fitrep.app.data.formatNumber
import com.fitrep.app.data.formatTimer
import com.fitrep.app.data.formatVolume
import com.fitrep.app.data.formatWeight
import com.fitrep.app.ui.components.ExerciseFigure
import com.fitrep.app.ui.components.GhostButton
import com.fitrep.app.ui.components.GradientButton
import com.fitrep.app.ui.components.ProgressRing
import com.fitrep.app.ui.components.ValueStepper
import com.fitrep.app.ui.components.exerciseAccent
import com.fitrep.app.ui.components.techniqueTip
import com.fitrep.app.ui.theme.RepColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    session: WorkoutSession,
    store: WorkoutStore,
    onFinish: () -> Unit,
    onMinimize: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var restLeft by rememberSaveable { mutableIntStateOf(0) }
    var restTotal by rememberSaveable { mutableIntStateOf(90) }
    var expanded by remember { mutableStateOf(setOf(session.exercises.firstOrNull()?.id.orEmpty())) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(session.id) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(restLeft) {
        if (restLeft > 0) {
            delay(1000)
            restLeft -= 1
        }
    }

    BackHandler { showExitDialog = true }

    fun startRest(seconds: Int = 90) {
        restTotal = seconds
        restLeft = seconds
    }

    val progress = if (session.totalSets == 0) 0f else session.doneSets.toFloat() / session.totalSets

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            WorkoutTopBar(
                sessionName = session.name,
                elapsedMs = now - session.startedAt,
                onClose = { showExitDialog = true },
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 6.dp,
                    bottom = 210.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    WorkoutHero(
                        progress = progress,
                        doneSets = session.doneSets,
                        totalSets = session.totalSets,
                        volume = session.volume,
                        elapsedMs = now - session.startedAt,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "УПРАЖНЕНИЯ",
                            style = MaterialTheme.typography.labelMedium,
                            color = RepColors.TextMuted,
                        )
                        GhostButton(
                            text = "Добавить",
                            icon = Icons.Rounded.Add,
                            tint = RepColors.Lime,
                            onClick = { showExercisePicker = true },
                        )
                    }
                }

                items(session.exercises, key = { it.id }) { entry ->
                    Box(modifier = Modifier.animateItem()) {
                        ExerciseCard(
                            entry = entry,
                            expanded = expanded.contains(entry.id),
                            onToggleExpand = {
                                expanded = if (expanded.contains(entry.id)) {
                                    expanded - entry.id
                                } else {
                                    expanded + entry.id
                                }
                            },
                            onWeightChange = { setId, weight -> store.setWeight(entry.id, setId, weight) },
                            onRepsChange = { setId, reps -> store.setReps(entry.id, setId, reps) },
                            onToggleSet = { setId ->
                                val becameDone = store.toggleSet(entry.id, setId)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (becameDone) startRest()
                            },
                            onAddSet = { store.addSet(entry.id) },
                            onDeleteSet = { setId -> store.deleteSet(entry.id, setId) },
                            onDeleteExercise = {
                                store.deleteExercise(entry.id)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = restLeft > 0,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                ) + fadeIn(tween(220)),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(200),
                ) + fadeOut(tween(160)),
            ) {
                RestTimerCard(
                    secondsLeft = restLeft,
                    total = restTotal,
                    onAddTime = { restLeft += 30; restTotal += 30 },
                    onSkip = {
                        restLeft = 0
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(RepColors.Surface.copy(alpha = 0.92f), RepColors.SurfaceHigh),
                        ),
                        RoundedCornerShape(24.dp),
                    )
                    .border(1.dp, RepColors.Outline, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ОБЪЁМ",
                        style = MaterialTheme.typography.labelSmall,
                        color = RepColors.TextMuted,
                    )
                    Text(
                        text = "${formatVolume(session.volume)} кг",
                        style = MaterialTheme.typography.titleMedium,
                        color = RepColors.TextPrimary,
                    )
                }
                GradientButton(
                    text = "Завершить",
                    icon = Icons.Rounded.Check,
                    enabled = session.doneSets > 0,
                    onClick = onFinish,
                )
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerSheet(
            onDismiss = { showExercisePicker = false },
            onPick = { exerciseId ->
                store.addExercise(exerciseId)
                showExercisePicker = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = RepColors.SurfaceHigh,
            titleContentColor = RepColors.TextPrimary,
            textContentColor = RepColors.TextSecondary,
            title = { Text("Свернуть тренировку?") },
            text = {
                Text("Прогресс сохранится — можно вернуться к ней с главного экрана.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onMinimize()
                }) {
                    Text("Свернуть", color = RepColors.Lime)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Отмена", color = RepColors.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun WorkoutTopBar(sessionName: String, elapsedMs: Long, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Свернуть тренировку",
                tint = RepColors.TextSecondary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ТРЕНИРОВКА ИДЁТ",
                style = MaterialTheme.typography.labelSmall,
                color = RepColors.Lime,
            )
            Text(
                text = sessionName,
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .background(RepColors.SurfaceHigh, CircleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Rounded.Timer,
                contentDescription = null,
                tint = RepColors.Cyan,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = formatTimer(elapsedMs),
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun WorkoutHero(
    progress: Float,
    doneSets: Int,
    totalSets: Int,
    volume: Double,
    elapsedMs: Long,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(RepColors.SurfaceHigh, RepColors.Surface)),
                RoundedCornerShape(28.dp),
            )
            .border(1.dp, RepColors.Lime.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(104.dp),
                strokeWidth = 9.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$doneSets/$totalSets",
                        style = MaterialTheme.typography.titleLarge,
                        color = RepColors.TextPrimary,
                    )
                    Text(
                        text = "подходов",
                        style = MaterialTheme.typography.labelSmall,
                        color = RepColors.TextMuted,
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ВРЕМЯ",
                    style = MaterialTheme.typography.labelSmall,
                    color = RepColors.TextMuted,
                )
                Text(
                    text = formatTimer(elapsedMs),
                    style = MaterialTheme.typography.displayMedium,
                    color = RepColors.TextPrimary,
                    maxLines = 1,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ОБЪЁМ",
                            style = MaterialTheme.typography.labelSmall,
                            color = RepColors.TextMuted,
                        )
                        Text(
                            text = "${formatVolume(volume)} кг",
                            style = MaterialTheme.typography.titleLarge,
                            color = RepColors.Cyan,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ПРОГРЕСС",
                            style = MaterialTheme.typography.labelSmall,
                            color = RepColors.TextMuted,
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = RepColors.Lime,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    entry: ExerciseEntry,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onWeightChange: (String, Double) -> Unit,
    onRepsChange: (String, Int) -> Unit,
    onToggleSet: (String) -> Unit,
    onAddSet: () -> Unit,
    onDeleteSet: (String) -> Unit,
    onDeleteExercise: () -> Unit,
) {
    val exercise = Catalog.exercise(entry.exerciseId)
    val done = entry.sets.count { it.done }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "chevron",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(280, easing = FastOutSlowInEasing))
            .background(RepColors.Surface, RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = if (done == entry.sets.size && entry.sets.isNotEmpty()) {
                    RepColors.Lime.copy(alpha = 0.35f)
                } else {
                    RepColors.Outline.copy(alpha = 0.8f)
                },
                shape = RoundedCornerShape(24.dp),
            )
            .clip(RoundedCornerShape(24.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = RepColors.TextPrimary, bounded = false),
                    onClick = onToggleExpand,
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(RepColors.SurfaceHigh, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center,
            ) {
                ExerciseFigure(
                    movement = exercise.movement,
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                    accent = exerciseAccent(exercise.muscle),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = RepColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${exercise.muscle} · $done из ${entry.sets.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RepColors.TextMuted,
                )
            }
            if (entry.sets.isNotEmpty() && done == entry.sets.size) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = RepColors.Lime,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Свернуть" else "Развернуть",
                tint = RepColors.TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(rotation),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + androidx.compose.animation.expandVertically(tween(260)),
            exit = fadeOut(tween(120)) + androidx.compose.animation.shrinkVertically(tween(220)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp)
                        .background(RepColors.SurfaceHigh, RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ExerciseFigure(
                        movement = exercise.movement,
                        modifier = Modifier.size(84.dp),
                        accent = exerciseAccent(exercise.muscle),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ТЕХНИКА",
                            style = MaterialTheme.typography.labelSmall,
                            color = exerciseAccent(exercise.muscle),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = techniqueTip(exercise.movement),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RepColors.TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "№",
                        style = MaterialTheme.typography.labelSmall,
                        color = RepColors.TextMuted,
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "ВЕС, КГ",
                        style = MaterialTheme.typography.labelSmall,
                        color = RepColors.TextMuted,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "ПОВТОРЫ",
                        style = MaterialTheme.typography.labelSmall,
                        color = RepColors.TextMuted,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(46.dp))
                }
                entry.sets.forEachIndexed { index, set ->
                    key(set.id) {
                        SetRow(
                            index = index + 1,
                            set = set,
                            onWeightChange = { onWeightChange(set.id, it) },
                            onRepsChange = { onRepsChange(set.id, it) },
                            onToggle = { onToggleSet(set.id) },
                            onDelete = { onDeleteSet(set.id) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GhostButton(
                        text = "Добавить подход",
                        icon = Icons.Rounded.Add,
                        tint = RepColors.TextSecondary,
                        onClick = onAddSet,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDeleteExercise) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Удалить упражнение",
                            tint = RepColors.TextMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetRow(
    index: Int,
    set: SetEntry,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    val rowColor by animateColorAsState(
        targetValue = if (set.done) RepColors.Lime.copy(alpha = 0.07f) else Color.Transparent,
        animationSpec = tween(250),
        label = "setRowColor",
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RepColors.Danger.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Удалить подход",
                    tint = RepColors.Danger,
                )
            }
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .background(rowColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 2.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (set.done) RepColors.Lime else RepColors.SurfaceHigh,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (set.done) Color(0xFF0B1400) else RepColors.TextMuted,
                )
            }
            Spacer(Modifier.width(4.dp))
            ValueStepper(
                value = formatWeight(set.weight),
                label = "",
                onDecrease = { onWeightChange((set.weight - 2.5).coerceAtLeast(0.0)) },
                onIncrease = { onWeightChange(set.weight + 2.5) },
                modifier = Modifier.weight(1f),
                enabled = !set.done,
            )
            ValueStepper(
                value = set.reps.toString(),
                label = "",
                onDecrease = { onRepsChange(set.reps - 1) },
                onIncrease = { onRepsChange(set.reps + 1) },
                modifier = Modifier.weight(1f),
                enabled = !set.done,
            )
            Spacer(Modifier.width(4.dp))
            val checkScale by animateFloatAsState(
                targetValue = if (set.done) 1f else 0.88f,
                animationSpec = spring(dampingRatio = 0.42f, stiffness = 700f),
                label = "checkScale",
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer { scaleX = checkScale; scaleY = checkScale }
                    .background(
                        if (set.done) RepColors.Lime else RepColors.SurfaceHigh,
                        CircleShape,
                    )
                    .semantics {
                        contentDescription = if (set.done) "Отменить подход" else "Выполнить подход"
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = if (set.done) Color.Black else RepColors.TextPrimary),
                        onClick = onToggle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = if (set.done) "Отменить подход" else "Выполнить подход",
                    tint = if (set.done) Color(0xFF0B1400) else RepColors.TextMuted,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
    }
}

@Composable
private fun RestTimerCard(
    secondsLeft: Int,
    total: Int,
    onAddTime: () -> Unit,
    onSkip: () -> Unit,
) {
    val progress = if (total <= 0) 0f else secondsLeft.toFloat() / total
    val urgent = secondsLeft <= 10
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RepColors.SurfaceHigh, RoundedCornerShape(24.dp))
            .border(
                1.dp,
                if (urgent) RepColors.Coral.copy(alpha = 0.5f) else RepColors.Lime.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProgressRing(
            progress = progress,
            modifier = Modifier.size(52.dp),
            strokeWidth = 5.dp,
            color = if (urgent) RepColors.Coral else RepColors.Lime,
        ) {
            Icon(
                Icons.Rounded.Timer,
                contentDescription = null,
                tint = if (urgent) RepColors.Coral else RepColors.Lime,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Отдых",
                style = MaterialTheme.typography.labelMedium,
                color = RepColors.TextMuted,
            )
            Text(
                text = formatTimer(secondsLeft * 1000L),
                style = MaterialTheme.typography.headlineMedium,
                color = if (urgent) RepColors.Coral else RepColors.TextPrimary,
            )
        }
        GhostButton(text = "+30 с", onClick = onAddTime, tint = RepColors.TextSecondary)
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(RepColors.Lime, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.Black),
                    onClick = onSkip,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = "Пропустить отдых",
                tint = Color(0xFF0B1400),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) {
            Catalog.exercises
        } else {
            Catalog.exercises.filter {
                it.name.contains(query, ignoreCase = true) || it.muscle.contains(query, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RepColors.Surface,
        contentColor = RepColors.TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding(),
        ) {
            Text(
                text = "Добавить упражнение",
                style = MaterialTheme.typography.headlineMedium,
                color = RepColors.TextPrimary,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск по названию или группе", color = RepColors.TextMuted) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RepColors.Lime,
                    unfocusedBorderColor = RepColors.Outline,
                    focusedContainerColor = RepColors.SurfaceHigh,
                    unfocusedContainerColor = RepColors.SurfaceHigh,
                    cursorColor = RepColors.Lime,
                    focusedTextColor = RepColors.TextPrimary,
                    unfocusedTextColor = RepColors.TextPrimary,
                ),
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 440.dp)) {
                items(filtered, key = { it.id }) { exercise ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = RepColors.Lime),
                                onClick = { onPick(exercise.id) },
                            )
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(RepColors.SurfaceHigh, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            ExerciseFigure(
                                movement = exercise.movement,
                                modifier = Modifier.fillMaxSize().padding(5.dp),
                                accent = exerciseAccent(exercise.muscle),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = RepColors.TextPrimary,
                            )
                            Text(
                                text = exercise.muscle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = RepColors.TextMuted,
                            )
                        }
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = RepColors.Lime,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
