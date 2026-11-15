package com.fitrep.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitrep.app.data.DayVolume
import com.fitrep.app.data.formatNumber
import com.fitrep.app.ui.theme.RepColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScreenBackground(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(RepColors.Background)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RepColors.Lime.copy(alpha = 0.10f), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.02f),
                radius = size.width * 0.95f,
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.88f, size.height * 0.02f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(RepColors.Violet.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(size.width * 0.05f, size.height * 0.92f),
                radius = size.width * 0.8f,
            ),
            radius = size.width * 0.8f,
            center = Offset(size.width * 0.05f, size.height * 0.92f),
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    colors: List<Color> = listOf(RepColors.Lime, RepColors.Mint),
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 900f),
        label = "buttonPress",
    )
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .background(Brush.horizontalGradient(colors), RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.Black),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color(0xFF0B1400), modifier = Modifier.size(20.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B1400),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = RepColors.TextSecondary,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "ghostPress")
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(RepColors.SurfaceHigh.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .border(1.dp, RepColors.Outline, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = RepColors.TextPrimary),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    color: Color = RepColors.Lime,
    trackColor: Color = RepColors.SurfaceHigh,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ringProgress",
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (animated > 0.001f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.5f), color, color)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
                val angle = Math.toRadians((-90f + 360f * animated).toDouble())
                val radius = (size.width - stroke) / 2f
                val centerX = size.width / 2f + radius * cos(angle).toFloat()
                val centerY = size.height / 2f + radius * sin(angle).toFloat()
                drawCircle(color.copy(alpha = 0.25f), radius = stroke * 1.3f, center = Offset(centerX, centerY))
                drawCircle(color, radius = stroke * 0.55f, center = Offset(centerX, centerY))
            }
        }
        content()
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    accent: Color = RepColors.TextPrimary,
) {
    Column(
        modifier = modifier
            .background(RepColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, RepColors.Outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = RepColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = if (value.length <= 6) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = accent,
                maxLines = 1,
            )
            if (unit != null) {
                Spacer(Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = RepColors.TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
        }
    }
}

@Composable
fun AnimatedNumber(
    value: Int,
    modifier: Modifier = Modifier,
    suffix: String = "",
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = RepColors.TextPrimary,
) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "animatedNumber",
    )
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(text = formatNumber(animated), style = style, color = color)
        if (suffix.isNotEmpty()) {
            Text(
                text = suffix,
                style = MaterialTheme.typography.labelLarge,
                color = RepColors.TextMuted,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
            )
        }
    }
}

@Composable
fun GlowChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = RepColors.Lime,
) {
    Row(
        modifier = modifier
            .background(tint.copy(alpha = 0.12f), CircleShape)
            .border(1.dp, tint.copy(alpha = 0.28f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = RepColors.TextPrimary)
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = RepColors.Lime,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAction,
                    )
                    .padding(4.dp),
            )
        }
    }
}

@Composable
fun WeekBarChart(
    week: List<DayVolume>,
    modifier: Modifier = Modifier,
) {
    val max = week.maxOfOrNull { it.volume }?.takeIf { it > 0 } ?: 1.0
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        week.forEachIndexed { index, day ->
            val target = (day.volume / max).toFloat().coerceIn(0f, 1f)
            val fraction by animateFloatAsState(
                targetValue = if (animate) target else 0f,
                animationSpec = tween(
                    durationMillis = 700,
                    delayMillis = index * 55,
                    easing = FastOutSlowInEasing,
                ),
                label = "bar$index",
            )
            val isToday = index == week.lastIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    val barHeight = if (day.volume > 0) {
                        (104.dp * fraction.coerceAtLeast(0.08f))
                    } else {
                        5.dp
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(barHeight)
                            .background(
                                brush = if (isToday && day.volume > 0) {
                                    Brush.verticalGradient(listOf(RepColors.Lime, RepColors.Mint))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(RepColors.Cyan.copy(alpha = 0.65f), RepColors.Cyan.copy(alpha = 0.16f)),
                                    )
                                },
                                shape = RoundedCornerShape(7.dp),
                            ),
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) RepColors.Lime else RepColors.TextMuted,
                )
            }
        }
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(RepColors.Outline.copy(alpha = 0.6f)),
    )
}

@Composable
fun ValueStepper(
    value: String,
    label: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RepColors.TextMuted,
            fontSize = 9.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            StepButton(symbol = "\u2212", onClick = onDecrease, enabled = enabled)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) RepColors.TextPrimary else RepColors.TextMuted,
                maxLines = 1,
                modifier = Modifier.width(44.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            StepButton(symbol = "+", onClick = onIncrease, enabled = enabled)
        }
    }
}

@Composable
private fun StepButton(
    symbol: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.85f else 1f, label = "stepPress")
    Box(
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(RepColors.SurfaceHigh, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = RepColors.TextPrimary, bounded = true),
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) RepColors.TextSecondary else RepColors.TextMuted.copy(alpha = 0.4f),
        )
    }
}
