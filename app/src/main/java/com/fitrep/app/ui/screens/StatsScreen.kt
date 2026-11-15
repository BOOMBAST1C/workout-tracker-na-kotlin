package com.fitrep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitrep.app.data.WorkoutSession
import com.fitrep.app.data.computeStats
import com.fitrep.app.data.daysWord
import com.fitrep.app.data.formatNumber
import com.fitrep.app.data.formatVolume
import com.fitrep.app.data.formatWeight
import com.fitrep.app.data.personalRecords
import com.fitrep.app.data.setsWord
import com.fitrep.app.data.workoutsWord
import com.fitrep.app.ui.components.AnimatedNumber
import com.fitrep.app.ui.components.StatTile
import com.fitrep.app.ui.components.WeekBarChart
import com.fitrep.app.ui.theme.RepColors

@Composable
fun StatsScreen(
    history: List<WorkoutSession>,
    onBack: () -> Unit,
) {
    val stats = remember(history) { computeStats(history) }
    val records = remember(history) { personalRecords(history) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 130.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = RepColors.TextSecondary,
                    )
                }
                Column {
                    Text(
                        text = "Статистика",
                        style = MaterialTheme.typography.headlineMedium,
                        color = RepColors.TextPrimary,
                    )
                    Text(
                        text = "Твой прогресс в цифрах",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RepColors.TextMuted,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(RepColors.SurfaceHigh, RepColors.Surface)),
                        RoundedCornerShape(28.dp),
                    )
                    .border(1.dp, RepColors.Cyan.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "ОБЩИЙ ОБЪЁМ",
                            style = MaterialTheme.typography.labelMedium,
                            color = RepColors.Cyan,
                        )
                        Icon(
                            Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = RepColors.Cyan.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        AnimatedNumber(
                            value = stats.totalVolume.toInt(),
                            style = MaterialTheme.typography.displayLarge,
                            color = RepColors.TextPrimary,
                        )
                        Text(
                            text = "кг",
                            style = MaterialTheme.typography.titleLarge,
                            color = RepColors.TextMuted,
                            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "за ${stats.totalWorkouts} ${workoutsWord(stats.totalWorkouts)} · ${formatNumber(stats.totalSets)} ${setsWord(stats.totalSets)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RepColors.TextMuted,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RepColors.Surface, RoundedCornerShape(24.dp))
                    .border(1.dp, RepColors.Outline.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .padding(18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Активность",
                        style = MaterialTheme.typography.titleLarge,
                        color = RepColors.TextPrimary,
                    )
                    Text(
                        text = "${formatVolume(stats.weekVolume)} кг",
                        style = MaterialTheme.typography.labelLarge,
                        color = RepColors.Cyan,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Объём за последние 7 дней",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RepColors.TextMuted,
                )
                Spacer(Modifier.height(18.dp))
                WeekBarChart(week = stats.week)
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile(
                    label = "Тренировок",
                    value = stats.totalWorkouts.toString(),
                    unit = "всего",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Подходов",
                    value = formatNumber(stats.totalSets),
                    unit = "всего",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile(
                    label = "Средняя",
                    value = stats.averageMinutes.toString(),
                    unit = "мин",
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "Серия",
                    value = stats.streak.toString(),
                    unit = daysWord(stats.streak),
                    accent = RepColors.Coral,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(26.dp))
        }

        if (records.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Личные рекорды",
                        style = MaterialTheme.typography.titleLarge,
                        color = RepColors.TextPrimary,
                    )
                    Icon(
                        Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = RepColors.Coral,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    records.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(RepColors.Surface, RoundedCornerShape(18.dp))
                                .border(1.dp, RepColors.Outline.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(RepColors.Gold.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = RepColors.Gold,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = RepColors.TextPrimary,
                                )
                                Text(
                                    text = record.exercise.muscle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RepColors.TextMuted,
                                )
                            }
                            Text(
                                text = formatWeight(record.weight),
                                style = MaterialTheme.typography.headlineMedium,
                                color = RepColors.Gold,
                            )
                            Text(
                                text = " кг × ${record.reps}",
                                style = MaterialTheme.typography.labelMedium,
                                color = RepColors.TextMuted,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (history.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RepColors.Surface.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .border(1.dp, RepColors.Outline.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "📊", fontSize = 30.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Пока нет данных",
                        style = MaterialTheme.typography.titleMedium,
                        color = RepColors.TextPrimary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Заверши первую тренировку — и здесь появится прогресс, графики и рекорды.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RepColors.TextMuted,
                    )
                }
            }
        }
    }
}

