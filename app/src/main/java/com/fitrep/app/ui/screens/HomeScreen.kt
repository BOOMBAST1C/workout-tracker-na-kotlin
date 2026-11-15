package com.fitrep.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitrep.app.data.Catalog
import com.fitrep.app.data.WorkoutSession
import com.fitrep.app.data.WorkoutTemplate
import com.fitrep.app.data.computeStats
import com.fitrep.app.data.daysWord
import com.fitrep.app.data.exercisesWord
import com.fitrep.app.data.formatDuration
import com.fitrep.app.data.formatLongDate
import com.fitrep.app.data.formatRelativeDate
import com.fitrep.app.data.formatVolume
import com.fitrep.app.data.setsWord
import com.fitrep.app.ui.components.AnimatedNumber
import com.fitrep.app.ui.components.GlowChip
import com.fitrep.app.ui.components.SectionHeader
import com.fitrep.app.ui.theme.RepColors
import java.time.LocalDate

@Composable
fun HomeScreen(
    history: List<WorkoutSession>,
    active: WorkoutSession?,
    onStartTemplate: (WorkoutTemplate) -> Unit,
    onResume: () -> Unit,
    onOpenSession: (WorkoutSession) -> Unit,
    onOpenStats: () -> Unit,
    onDiscardActive: () -> Unit,
) {
    val stats = remember(history) { computeStats(history) }
    val recent = history.take(6)
    var confirmDiscard by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 130.dp),
    ) {
        item {
            HomeHeader(streak = stats.streak)
            Spacer(Modifier.height(22.dp))
        }

        item {
            WeekSummaryCard(
                weekWorkouts = stats.weekWorkouts,
                weekVolume = stats.weekVolume,
            )
            Spacer(Modifier.height(16.dp))
        }

        item {
            AnimatedVisibility(
                visible = active != null,
                enter = fadeIn(tween(250)) + expandVertically(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                ),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(250)),
            ) {
                if (active != null) {
                    ActiveDraftCard(
                        active = active,
                        onResume = onResume,
                        onDiscard = { confirmDiscard = true },
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        item {
            Column {
                SectionHeader(
                    title = "Шаблоны",
                    action = "Статистика",
                    onAction = onOpenStats,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Начни тренировку в один тап",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RepColors.TextMuted,
                )
                Spacer(Modifier.height(14.dp))
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(end = 4.dp),
            ) {
                items(Catalog.templates, key = { it.id }) { template ->
                    TemplateCard(template = template, onClick = { onStartTemplate(template) })
                }
            }
            Spacer(Modifier.height(26.dp))
        }

        item {
            SectionHeader(
                title = "История",
                action = if (recent.isEmpty()) null else "Вся статистика",
                onAction = onOpenStats,
            )
            Spacer(Modifier.height(14.dp))
        }

        if (recent.isEmpty()) {
            item { EmptyHistoryCard() }
        } else {
            items(recent, key = { it.id }) { session ->
                HistoryCard(session = session, onClick = { onOpenSession(session) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (confirmDiscard && active != null) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            containerColor = RepColors.SurfaceHigh,
            titleContentColor = RepColors.TextPrimary,
            textContentColor = RepColors.TextSecondary,
            shape = RoundedCornerShape(26.dp),
            title = { Text("Сбросить тренировку?") },
            text = { Text("Все подходы этой тренировки исчезнут. Отменить это действие будет нельзя.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onDiscardActive()
                }) {
                    Text("Сбросить", color = RepColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) {
                    Text("Отмена", color = RepColors.TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun HomeHeader(streak: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatLongDate(LocalDate.now()).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = RepColors.TextMuted,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Тренировки",
                style = MaterialTheme.typography.headlineLarge,
                color = RepColors.TextPrimary,
            )
        }
        if (streak > 0) {
            GlowChip(
                text = "$streak ${daysWord(streak)}",
                icon = Icons.Rounded.LocalFireDepartment,
                tint = RepColors.Coral,
            )
        } else {
            GlowChip(
                text = "Новичок",
                icon = Icons.Rounded.Bolt,
                tint = RepColors.Lime,
            )
        }
    }
}

@Composable
private fun WeekSummaryCard(weekWorkouts: Int, weekVolume: Double) {
    val goal = 5
    val progress by animateFloatAsState(
        targetValue = (weekWorkouts.toFloat() / goal).coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "weekGoal",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(RepColors.SurfaceHigh, RepColors.Surface),
                ),
                shape = RoundedCornerShape(26.dp),
            )
            .border(1.dp, RepColors.Lime.copy(alpha = 0.14f), RoundedCornerShape(26.dp)),
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 250.dp, y = (-90).dp)
                .background(
                    Brush.radialGradient(
                        listOf(RepColors.Lime.copy(alpha = 0.18f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "ЭТА НЕДЕЛЯ",
                style = MaterialTheme.typography.labelMedium,
                color = RepColors.Lime,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedNumber(
                        value = weekWorkouts,
                        style = MaterialTheme.typography.displayMedium,
                        color = RepColors.TextPrimary,
                    )
                    Text(
                        text = "тренировок",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RepColors.TextMuted,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatVolume(weekVolume)} кг",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RepColors.Cyan,
                    )
                    Text(
                        text = "общий объём",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RepColors.TextMuted,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(RepColors.Background.copy(alpha = 0.6f), CircleShape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(listOf(RepColors.Lime, RepColors.Mint)),
                            CircleShape,
                        ),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Цель недели",
                    style = MaterialTheme.typography.labelMedium,
                    color = RepColors.TextMuted,
                )
                Text(
                    text = "$weekWorkouts / $goal",
                    style = MaterialTheme.typography.labelMedium,
                    color = RepColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ActiveDraftCard(active: WorkoutSession, onResume: () -> Unit, onDiscard: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "livePulseAlpha",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(RepColors.Lime.copy(alpha = 0.16f), RepColors.Mint.copy(alpha = 0.05f)),
                ),
                RoundedCornerShape(22.dp),
            )
            .border(1.dp, RepColors.Lime.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = RepColors.Lime),
                onClick = onResume,
            )
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .graphicsLayer { alpha = pulse }
                .background(RepColors.Lime, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Незавершённая тренировка",
                style = MaterialTheme.typography.labelMedium,
                color = RepColors.Lime,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${active.emoji} ${active.name}",
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .background(RepColors.Lime, CircleShape)
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF0B1400),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "Продолжить",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF0B1400),
            )
        }
        Spacer(Modifier.width(2.dp))
        IconButton(onClick = onDiscard, modifier = Modifier.size(34.dp)) {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = "Сбросить тренировку",
                tint = RepColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun TemplateCard(template: WorkoutTemplate, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.955f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 800f),
        label = "templatePress",
    )
    val start = Color(template.accentStart)
    val end = Color(template.accentEnd)
    Box(
        modifier = Modifier
            .width(248.dp)
            .height(172.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(start, end)))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.Black),
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = 170.dp, y = (-40).dp)
                .background(Color.White.copy(alpha = 0.16f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 196.dp, y = 76.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = template.emoji, fontSize = 21.sp)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0x33000000), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A1408),
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = template.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xCC0A1408),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemplateMetaPill("${template.exercises.size} ${exercisesWord(template.exercises.size)}")
                    TemplateMetaPill("${template.exercises.sumOf { it.sets }} ${setsWord(template.exercises.sumOf { it.sets })}")
                }
            }
        }
    }
}

@Composable
private fun TemplateMetaPill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xCC0A1408),
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.28f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

@Composable
private fun HistoryCard(session: WorkoutSession, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "historyPress")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(RepColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, RepColors.Outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = RepColors.TextPrimary),
                onClick = onClick,
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
            Text(text = session.emoji, fontSize = 21.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${formatRelativeDate(session.date)} · ${formatDuration(session.durationMs)} · ${session.doneSets} ${setsWord(session.doneSets)}",
                style = MaterialTheme.typography.bodyMedium,
                color = RepColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatVolume(session.volume),
                style = MaterialTheme.typography.titleMedium,
                color = RepColors.Cyan,
            )
            Text(
                text = "кг",
                style = MaterialTheme.typography.labelSmall,
                color = RepColors.TextMuted,
            )
        }
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = RepColors.TextMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RepColors.Surface.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .border(1.dp, RepColors.Outline.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(RepColors.SurfaceHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = RepColors.TextMuted,
                modifier = Modifier.size(26.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Пока пусто",
            style = MaterialTheme.typography.titleMedium,
            color = RepColors.TextPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Выбери шаблон выше и отметь первый подход",
            style = MaterialTheme.typography.bodyMedium,
            color = RepColors.TextMuted,
        )
    }
}
