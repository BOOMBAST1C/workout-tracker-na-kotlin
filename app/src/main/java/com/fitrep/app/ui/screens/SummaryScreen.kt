package com.fitrep.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitrep.app.data.Catalog
import com.fitrep.app.data.ExerciseEntry
import com.fitrep.app.data.WorkoutSession
import com.fitrep.app.data.formatDuration
import com.fitrep.app.data.formatRelativeDate
import com.fitrep.app.data.formatTimer
import com.fitrep.app.data.formatVolume
import com.fitrep.app.data.formatWeight
import com.fitrep.app.ui.components.ExerciseFigure
import com.fitrep.app.ui.components.GhostButton
import com.fitrep.app.ui.components.GradientButton
import com.fitrep.app.ui.components.ProgressRing
import com.fitrep.app.ui.components.StatTile
import com.fitrep.app.ui.components.exerciseAccent
import com.fitrep.app.ui.theme.RepColors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SummaryScreen(
    session: WorkoutSession,
    fresh: Boolean,
    onHome: () -> Unit,
) {
    var sweep by remember(session.id) { mutableStateOf(0f) }
    var showCheck by remember(session.id) { mutableStateOf(false) }

    LaunchedEffect(session.id) {
        delay(180)
        sweep = 1f
        showCheck = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onHome) {
                    Icon(
                        if (fresh) Icons.Rounded.Home else Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "На главную",
                        tint = RepColors.TextSecondary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (fresh) "ТРЕНИРОВКА ЗАВЕРШЕНА" else "ИТОГИ ТРЕНИРОВКИ",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (fresh) RepColors.Lime else RepColors.TextMuted,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(10.dp))

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(RepColors.Lime.copy(alpha = 0.07f), CircleShape),
                )
                ProgressRing(
                    progress = sweep,
                    modifier = Modifier.size(156.dp),
                    strokeWidth = 8.dp,
                    color = if (fresh) RepColors.Lime else RepColors.TextMuted,
                ) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .background(RepColors.SurfaceHigh, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        val checkScale by animateFloatAsState(
                            targetValue = if (showCheck) 1f else 0f,
                            animationSpec = spring(dampingRatio = 0.4f, stiffness = 420f),
                            label = "checkPop",
                        )
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (fresh) RepColors.Lime else RepColors.TextSecondary,
                            modifier = Modifier
                                .size(48.dp)
                                .graphicsLayer {
                                    scaleX = checkScale
                                    scaleY = checkScale
                                    alpha = checkScale.coerceIn(0f, 1f)
                                },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (fresh) "Красиво!" else session.name,
                style = MaterialTheme.typography.headlineLarge,
                color = RepColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (fresh) {
                    "${session.emoji} ${session.name} · ${formatRelativeDate(session.date)}"
                } else {
                    "${formatRelativeDate(session.date)} · ${formatDuration(session.durationMs)}"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = RepColors.TextMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile(
                    label = "Время",
                    value = formatTimer(session.durationMs),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Подходы",
                    value = session.doneSets.toString(),
                    unit = "из ${session.totalSets}",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Объём",
                    value = formatVolume(session.volume),
                    unit = "кг",
                    accent = RepColors.Cyan,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Упражнения",
                    style = MaterialTheme.typography.titleLarge,
                    color = RepColors.TextPrimary,
                )
                Text(
                    text = "${session.exercises.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = RepColors.TextMuted,
                )
            }
            Spacer(Modifier.height(12.dp))

            session.exercises.forEach { entry ->
                SummaryExerciseRow(entry)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))

            if (fresh) {
                GradientButton(
                    text = "На главную",
                    icon = Icons.Rounded.Home,
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                GhostButton(
                    text = "Назад",
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(40.dp))
        }

        if (fresh) {
            Confetti(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SummaryExerciseRow(entry: ExerciseEntry) {
    val exercise = Catalog.exercise(entry.exerciseId)
    val doneSets = entry.sets.filter { it.done }
    val best = doneSets.maxByOrNull { it.weight * it.reps }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RepColors.Surface, RoundedCornerShape(18.dp))
            .border(1.dp, RepColors.Outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExerciseFigure(
                movement = exercise.movement,
                modifier = Modifier.size(30.dp),
                accent = exerciseAccent(exercise.muscle),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${doneSets.size}/${entry.sets.size}",
                style = MaterialTheme.typography.labelLarge,
                color = if (doneSets.isEmpty()) RepColors.TextMuted else RepColors.Lime,
            )
        }
        if (best != null) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                doneSets.forEach { set ->
                    Text(
                        text = "${formatWeight(set.weight)}×${set.reps}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (set == best) Color(0xFF0B1400) else RepColors.TextSecondary,
                        modifier = Modifier
                            .background(
                                if (set == best) RepColors.Lime else RepColors.SurfaceHigh,
                                CircleShape,
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val speed: Float,
    val sway: Float,
    val phase: Float,
    val spin: Float,
)

@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        val random = Random(7)
        val colors = listOf(
            RepColors.Lime,
            RepColors.Cyan,
            RepColors.Coral,
            RepColors.Violet,
            RepColors.Gold,
            RepColors.Mint,
        )
        List(80) {
            Particle(
                x = random.nextFloat(),
                width = 5f + random.nextFloat() * 6f,
                height = 10f + random.nextFloat() * 10f,
                color = colors[random.nextInt(colors.size)],
                speed = 0.6f + random.nextFloat() * 0.55f,
                sway = 18f + random.nextFloat() * 42f,
                phase = random.nextFloat() * 6.28f,
                spin = -2f + random.nextFloat() * 4f,
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing),
        )
    }

    Canvas(modifier = modifier) {
        val overall = progress.value
        if (overall >= 1f) return@Canvas
        particles.forEach { particle ->
            val t = (overall * (0.8f + particle.speed)).coerceIn(0f, 1f)
            val x = particle.x * size.width + sin(t * 12.56f + particle.phase) * particle.sway
            val y = -40f + t * (size.height + 90f)
            val alpha = if (t > 0.75f) ((1f - t) / 0.25f).coerceIn(0f, 1f) else 1f
            if (alpha <= 0.01f) return@forEach
            rotate(degrees = t * particle.spin * 360f, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(x - particle.width / 2f, y - particle.height / 2f),
                    size = Size(particle.width, particle.height),
                    cornerRadius = CornerRadius(2.5f, 2.5f),
                )
            }
        }
    }
}
