package com.fitrep.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.fitrep.app.data.Movement
import com.fitrep.app.ui.theme.RepColors
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Мини-фигурка, которая повторяет движение упражнения.
 * Рисуется на Canvas в системе координат 100×100, поэтому одинаково
 * выглядит и в списке упражнений, и в карточке тренировки.
 */
@Composable
fun ExerciseFigure(
    movement: Movement,
    modifier: Modifier = Modifier,
    accent: Color = RepColors.Lime,
    animated: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "exerciseFigure")
    val phase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodFor(movement), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "figurePhase",
    )

    Canvas(modifier) {
        val unit = min(size.width, size.height) / 100f
        if (unit <= 0.01f) return@Canvas
        val originX = (size.width - 100f * unit) / 2f
        val originY = (size.height - 100f * unit) / 2f - verticalBias(movement) * unit
        val p: (Float, Float) -> Offset = { x, y -> Offset(originX + x * unit, originY + y * unit) }

        drawSoftGlow(p, unit, accent)

        val raw = if (animated) phase.value else 0.35f
        val t = smoothstep(raw)

        when (movement) {
            Movement.SQUAT -> drawSquat(p, t, unit, accent)
            Movement.BENCH -> drawBench(p, t, unit, accent)
            Movement.ROW -> drawRow(p, t, unit, accent)
            Movement.DEADLIFT -> drawDeadlift(p, t, unit, accent)
            Movement.PRESS -> drawOverheadPress(p, t, unit, accent)
            Movement.PULLUP -> drawPullUp(p, t, unit, accent)
            Movement.DIP -> drawDip(p, t, unit, accent)
            Movement.CURL -> drawCurl(p, t, unit, accent)
            Movement.TRICEPS -> drawTriceps(p, t, unit, accent)
            Movement.LUNGE -> drawLunge(p, t, unit, accent)
            Movement.LEG_PRESS -> drawLegPress(p, t, unit, accent)
            Movement.RDL -> drawRomanianDeadlift(p, t, unit, accent)
            Movement.CALF -> drawCalfRaise(p, t, unit, accent)
            Movement.LATERAL -> drawLateralRaise(p, t, unit, accent)
            Movement.PLANK -> drawPlank(p, t, unit, accent)
            Movement.CRUNCH -> drawCrunch(p, t, unit, accent)
            Movement.BURPEE -> drawBurpee(p, t, unit, accent)
            Movement.ROPE -> drawJumpRope(p, raw, t, unit, accent)
        }
    }
}

fun exerciseAccent(muscle: String): Color = when (muscle) {
    "Ноги" -> RepColors.Lime
    "Грудь" -> RepColors.Coral
    "Спина" -> RepColors.Cyan
    "Плечи" -> RepColors.Violet
    "Руки" -> RepColors.Gold
    "Кор" -> RepColors.Mint
    else -> RepColors.Coral
}

fun techniqueTip(movement: Movement): String = when (movement) {
    Movement.SQUAT -> "Колени по линии стоп, спина прямая"
    Movement.BENCH -> "Лопатки сведены, гриф над грудью"
    Movement.ROW -> "Тяни к поясу, локоть вдоль тела"
    Movement.DEADLIFT -> "Спина прямая, гриф у голеней"
    Movement.PRESS -> "Не прогибай поясницу, жми вверх"
    Movement.PULLUP -> "Тянись грудью к перекладине"
    Movement.DIP -> "Локти ближе к телу, плечи вниз"
    Movement.CURL -> "Локти неподвижны, без раскачки"
    Movement.TRICEPS -> "Разгибай плавно, локти вверх"
    Movement.LUNGE -> "Колено не выходит за носок"
    Movement.LEG_PRESS -> "Не отрывай таз от сиденья"
    Movement.RDL -> "Тянись тазом назад, колени мягкие"
    Movement.CALF -> "Поднимайся выше, опускайся медленно"
    Movement.LATERAL -> "Веди локтями, не поднимай плечи"
    Movement.PLANK -> "Пресс напряжён, тело — прямая линия"
    Movement.CRUNCH -> "Скручивай пресс, поясница на полу"
    Movement.BURPEE -> "Упор лёжа, прыжок, хлопок"
    Movement.ROPE -> "Прыгай мягко, кисти вращают скакалку"
}

private fun periodFor(movement: Movement): Int = when (movement) {
    Movement.ROPE -> 780
    Movement.CALF -> 900
    Movement.BURPEE -> 1150
    Movement.CRUNCH -> 1350
    Movement.PLANK -> 2600
    Movement.ROW, Movement.CURL, Movement.LATERAL, Movement.TRICEPS -> 1500
    else -> 1750
}

private fun smoothstep(v: Float): Float = v * v * (3f - 2f * v)

/** Горизонтальные позы (скамья, планка) приподнимаем, чтобы они не липли к низу. */
private fun verticalBias(movement: Movement): Float = when (movement) {
    Movement.BENCH, Movement.PLANK, Movement.CRUNCH, Movement.LEG_PRESS -> 12f
    else -> 0f
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

// ---------------------------------------------------------------------------
// Примитивы
// ---------------------------------------------------------------------------

private fun DrawScope.drawSoftGlow(
    p: (Float, Float) -> Offset,
    unit: Float,
    accent: Color,
) {
    val center = p(50f, 54f)
    val radius = 44f * unit
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accent.copy(alpha = 0.15f), Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

/** Ломаная с мягким неоновым свечением. */
private fun DrawScope.rod(
    p: (Float, Float) -> Offset,
    points: List<Pair<Float, Float>>,
    color: Color,
    width: Float,
    unit: Float,
    alpha: Float = 1f,
) {
    for (i in 0 until points.size - 1) {
        val a = p(points[i].first, points[i].second)
        val b = p(points[i + 1].first, points[i + 1].second)
        drawLine(
            color = color.copy(alpha = alpha * 0.13f),
            start = a,
            end = b,
            strokeWidth = (width + 3.4f) * unit,
            cap = StrokeCap.Round,
        )
    }
    for (i in 0 until points.size - 1) {
        val a = p(points[i].first, points[i].second)
        val b = p(points[i + 1].first, points[i + 1].second)
        drawLine(
            color = color.copy(alpha = alpha),
            start = a,
            end = b,
            strokeWidth = width * unit,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.head(
    p: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    unit: Float,
    accent: Color,
    alpha: Float = 1f,
    radius: Float = 5.8f,
) {
    drawCircle(
        color = accent.copy(alpha = alpha),
        radius = radius * unit,
        center = p(x, y),
    )
    drawCircle(
        color = RepColors.Background.copy(alpha = 0.35f * alpha),
        radius = (radius - 2.2f) * unit,
        center = p(x, y),
    )
}

/** Диск штанги, если смотреть сбоку. */
private fun DrawScope.sideWeight(
    p: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    unit: Float,
    accent: Color,
    radius: Float = 5.4f,
) {
    drawLine(
        color = RepColors.TextSecondary.copy(alpha = 0.8f),
        start = p(x - 7f, y),
        end = p(x + 2.5f, y),
        strokeWidth = 2f * unit,
        cap = StrokeCap.Round,
    )
    plate(p, x, y, radius, unit, accent)
}

/** Блин штанги (в фас). */
private fun DrawScope.plate(
    p: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    radius: Float,
    unit: Float,
    accent: Color,
) {
    drawCircle(color = accent, radius = radius * unit, center = p(x, y))
    drawCircle(
        color = RepColors.Background.copy(alpha = 0.8f),
        radius = radius * 0.4f * unit,
        center = p(x, y),
    )
}

private fun DrawScope.barbell(
    p: (Float, Float) -> Offset,
    centerX: Float,
    y: Float,
    halfWidth: Float,
    unit: Float,
    accent: Color,
    plateRadius: Float = 5f,
) {
    drawLine(
        color = RepColors.TextSecondary,
        start = p(centerX - halfWidth, y),
        end = p(centerX + halfWidth, y),
        strokeWidth = 2.2f * unit,
        cap = StrokeCap.Round,
    )
    plate(p, centerX - halfWidth, y, plateRadius, unit, accent)
    plate(p, centerX + halfWidth, y, plateRadius, unit, accent)
}

private fun DrawScope.dumbbell(
    p: (Float, Float) -> Offset,
    x: Float,
    y: Float,
    unit: Float,
    accent: Color,
    vertical: Boolean = true,
) {
    val color = RepColors.TextSecondary
    if (vertical) {
        drawLine(color, p(x, y - 4f), p(x, y + 4f), strokeWidth = 2f * unit, cap = StrokeCap.Round)
        for (dy in listOf(-4f, 4f)) {
            drawCircle(accent, radius = 2.9f * unit, center = p(x, y + dy))
            drawCircle(
                RepColors.Background.copy(alpha = 0.7f),
                radius = 1.1f * unit,
                center = p(x, y + dy),
            )
        }
    } else {
        drawLine(color, p(x - 4f, y), p(x + 4f, y), strokeWidth = 2f * unit, cap = StrokeCap.Round)
        for (dx in listOf(-4f, 4f)) {
            drawCircle(accent, radius = 2.9f * unit, center = p(x + dx, y))
            drawCircle(
                RepColors.Background.copy(alpha = 0.7f),
                radius = 1.1f * unit,
                center = p(x + dx, y),
            )
        }
    }
}

private fun DrawScope.floorLine(
    p: (Float, Float) -> Offset,
    unit: Float,
    accent: Color,
    y: Float = 93f,
) {
    drawLine(
        color = accent.copy(alpha = 0.18f),
        start = p(14f, y),
        end = p(86f, y),
        strokeWidth = 1.3f * unit,
        cap = StrokeCap.Round,
    )
}

/** Скамья, стойки, рама тренажёра. */
private fun DrawScope.apparatus(
    p: (Float, Float) -> Offset,
    points: List<Pair<Float, Float>>,
    unit: Float,
    width: Float = 3.2f,
) {
    rod(p, points, RepColors.TextMuted, width, unit, alpha = 0.7f)
}

// ---------------------------------------------------------------------------
// Движения
// ---------------------------------------------------------------------------

private fun DrawScope.drawSquat(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val headY = 20f + 12f * t
    val shoulderY = 31f + 12f * t
    val hipY = 56f + 8f * t
    val kneeX = 42f - 8f * t
    val kneeY = 74f + 2f * t
    val barY = shoulderY - 1.5f

    rod(p, listOf(50f to shoulderY, 41f to shoulderY + 6f, 36.5f to barY + 1f), accent, 3f, unit, 0.45f)
    rod(p, listOf(50f to hipY, 50f to shoulderY, 50f to headY + 4f), accent, 5f, unit)
    rod(p, listOf(50f to hipY, kneeX to kneeY, 37f to 91f, 30f to 92.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 100f - kneeX to kneeY, 63f to 91f, 70f to 92.5f), accent, 3.2f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to shoulderY, 59f to shoulderY + 6f, 63.5f to barY + 1f), accent, 3f, unit)
    barbell(p, 50f, barY, 20f, unit, accent)
}

private fun DrawScope.drawBench(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    apparatus(p, listOf(24f to 66f, 78f to 66f), unit, 4f)
    apparatus(p, listOf(30f to 66f, 30f to 92f), unit, 3f)
    apparatus(p, listOf(71f to 66f, 71f to 92f), unit, 3f)

    val wristY = 52f - 16f * t
    val elbowY = 56f - 8f * t

    rod(p, listOf(62f to 63f, 72f to 75f, 74f to 90f), accent, 3f, unit, 0.45f)
    rod(p, listOf(45f to 63f, 45f to elbowY, 45f to wristY), accent, 2.9f, unit, 0.45f)
    rod(p, listOf(63f to 63f, 45f to 63f, 35f to 60f), accent, 5f, unit)
    rod(p, listOf(63f to 63f, 72f to 75f, 74f to 90f, 80f to 91f), accent, 3.2f, unit)
    head(p, 31f, 58f, unit, accent)
    rod(p, listOf(45f to 63f, 45f to elbowY, 45f to wristY), accent, 3f, unit)
    sideWeight(p, 45f, wristY, unit, accent, 6f)
}

private fun DrawScope.drawRow(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val wristX = 57f - 2f * t
    val wristY = 63f - 12f * t
    val elbowX = 57f - 6f * t
    val elbowY = 54f - 6f * t

    rod(p, listOf(46f to 59f, 52f to 76f, 51f to 91f, 58f to 92f), accent, 3f, unit, 0.4f)
    rod(p, listOf(55f to 41f, elbowX to elbowY, wristX to wristY), accent, 3f, unit, 0.4f)
    rod(p, listOf(46f to 59f, 55f to 41f, 59f to 36f), accent, 5f, unit)
    rod(p, listOf(46f to 59f, 50f to 76f, 49f to 91f, 56f to 92f), accent, 3.2f, unit)
    head(p, 61f, 32f, unit, accent)
    rod(p, listOf(55f to 41f, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    sideWeight(p, wristX, wristY, unit, accent)
}

private fun DrawScope.drawDeadlift(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val hipX = lerp(49f, 45f, t)
    val hipY = lerp(56f, 67f, t)
    val shoulderX = lerp(49f, 57f, t)
    val shoulderY = lerp(31f, 47f, t)
    val headX = lerp(50f, 60f, t)
    val headY = lerp(20f, 39f, t)
    val kneeX = lerp(49f, 47f, t)
    val kneeY = lerp(74f, 78f, t)
    val elbowX = lerp(51f, 59f, t)
    val elbowY = lerp(45f, 66f, t)
    val wristX = lerp(51f, 60f, t)
    val wristY = lerp(59f, 82f, t)

    rod(p, listOf(hipX - 2f to hipY, kneeX - 2f to kneeY, 47f to 91f, 54f to 92f), accent, 3f, unit, 0.4f)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit, 0.4f)
    rod(p, listOf(hipX to hipY, shoulderX to shoulderY, headX - 2f to headY + 4f), accent, 5f, unit)
    rod(p, listOf(hipX to hipY, kneeX to kneeY, 49f to 91f, 56f to 92f), accent, 3.2f, unit)
    head(p, headX, headY, unit, accent)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    sideWeight(p, wristX, wristY, unit, accent, 5.6f)
}

private fun DrawScope.drawOverheadPress(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val barY = 31f - 19f * t
    val elbowX = lerp(38f, 34f, t)
    val elbowY = lerp(40f, 25f, t)
    val headY = 20f + 1.5f * t

    rod(p, listOf(50f to 56f, 42f to 74f, 38f to 91f, 31f to 92.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to 56f, 58f to 74f, 62f to 91f, 69f to 92.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to 56f, 50f to 31f, 50f to headY + 4f), accent, 5f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to 31f, 100f - elbowX to elbowY, 64f to barY), accent, 3f, unit, 0.45f)
    rod(p, listOf(50f to 31f, elbowX to elbowY, 36f to barY), accent, 3f, unit)
    barbell(p, 50f, barY, 22f, unit, accent, 4.8f)
}

private fun DrawScope.drawPullUp(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    apparatus(p, listOf(26f to 14f, 74f to 14f), unit, 3.4f)
    apparatus(p, listOf(27f to 14f, 27f to 9f), unit, 2.4f)
    apparatus(p, listOf(73f to 14f, 73f to 9f), unit, 2.4f)

    val lift = 10f * t
    val headY = 31f - lift
    val shoulderY = 43f - lift
    val hipY = 64f - lift
    val kneeY = 78f - lift
    val ankleY = 89f - lift
    val elbowY = lerp(25f, 21f, t)
    val elbowX = lerp(36f, 29f, t)

    rod(p, listOf(50f to hipY, 45f to kneeY, 43f to ankleY, 39f to ankleY + 1.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 55f to kneeY, 57f to ankleY, 61f to ankleY + 1.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 50f to shoulderY, 50f to headY + 4f), accent, 5f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to shoulderY, 100f - elbowX to elbowY, 64f to 14f), accent, 3f, unit, 0.45f)
    rod(p, listOf(50f to shoulderY, elbowX to elbowY, 36f to 14f), accent, 3f, unit, 0.45f)
}

private fun DrawScope.drawDip(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    apparatus(p, listOf(15f to 40f, 38f to 40f), unit, 4f)
    apparatus(p, listOf(62f to 40f, 85f to 40f), unit, 4f)
    apparatus(p, listOf(20f to 40f, 20f to 92f), unit, 2.6f)
    apparatus(p, listOf(80f to 40f, 80f to 92f), unit, 2.6f)

    val drop = 6f * t
    val headY = 33f + drop
    val shoulderY = 45f + drop
    val hipY = 62f + drop
    val kneeY = 76f + drop
    val ankleY = 87f + 2.5f * t
    val elbowY = 42f + drop

    rod(p, listOf(50f to hipY, 47f to kneeY, 46f to ankleY, 42f to ankleY + 1f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 53f to kneeY, 54f to ankleY, 58f to ankleY + 1f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 50f to shoulderY, 50f to headY + 4f), accent, 5f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to shoulderY, 37f to elbowY, 34f to 40f), accent, 3f, unit)
    rod(p, listOf(50f to shoulderY, 63f to elbowY, 66f to 40f), accent, 3f, unit)
}

private fun DrawScope.drawCurl(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val wristX = lerp(57f, 54f, t)
    val wristY = lerp(56f, 37f, t)
    val elbowX = lerp(52f, 51f, t)
    val elbowY = 44f

    rod(p, listOf(50f to 57f, 54f to 44f, 57f to 52f), accent, 3f, unit, 0.4f)
    rod(p, listOf(50f to 57f, 50f to 31f, 50f to 23f), accent, 5f, unit)
    rod(p, listOf(50f to 57f, 49f to 75f, 49f to 92f, 57f to 93f), accent, 3.2f, unit)
    head(p, 50f, 19f, unit, accent)
    rod(p, listOf(50f to 31f, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    sideWeight(p, wristX, wristY, unit, accent, 5.2f)
}

private fun DrawScope.drawTriceps(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val wristX = lerp(44f, 53f, t)
    val wristY = lerp(28f, 10f, t)
    val elbowX = lerp(56f, 54f, t)
    val elbowY = lerp(24f, 20f, t)

    rod(p, listOf(50f to 57f, 50f to 31f, 51f to 23f), accent, 5f, unit)
    rod(p, listOf(50f to 57f, 49f to 75f, 49f to 92f, 57f to 93f), accent, 3.2f, unit)
    rod(p, listOf(50f to 31f, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    head(p, 51f, 19f, unit, accent)
    sideWeight(p, wristX, wristY, unit, accent, 4.6f)
}

private fun DrawScope.drawLunge(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val hipX = lerp(49f, 47f, t)
    val hipY = lerp(56f, 63f, t)
    val shoulderY = lerp(31f, 38f, t)
    val headY = lerp(19f, 26f, t)
    val frontKneeX = lerp(49f, 61f, t)
    val frontKneeY = lerp(75f, 77f, t)
    val frontAnkleX = lerp(49f, 65f, t)
    val backKneeX = lerp(49f, 36f, t)
    val backKneeY = lerp(75f, 80f, t)
    val backAnkleX = lerp(49f, 32f, t)
    val backAnkleY = lerp(92f, 86f, t)
    val wristX = lerp(54f, 53f, t)
    val wristY = lerp(57f, 65f, t)
    val elbowY = lerp(44f, 51f, t)

    rod(p, listOf(hipX to hipY, backKneeX - 1f to backKneeY, backAnkleX to backAnkleY, backAnkleX - 6f to 92f), accent, 3f, unit, 0.45f)
    rod(p, listOf(hipX to hipY, 53f to 44f, 56f to 62f), accent, 3f, unit, 0.4f)
    rod(p, listOf(hipX to hipY, 47f to shoulderY, 47f to headY + 4f), accent, 5f, unit)
    rod(p, listOf(hipX to hipY, frontKneeX to frontKneeY, frontAnkleX to 92f, frontAnkleX + 7f to 93f), accent, 3.2f, unit)
    head(p, 47f, headY, unit, accent)
    rod(p, listOf(47f to shoulderY, 52f to elbowY, wristX to wristY), accent, 3f, unit)
    dumbbell(p, wristX, wristY, unit, accent)
}

private fun DrawScope.drawLegPress(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    apparatus(p, listOf(28f to 68f, 52f to 68f), unit, 4.4f)
    apparatus(p, listOf(28f to 68f, 24f to 43f), unit, 4f)
    apparatus(p, listOf(80f to 36f, 80f to 64f), unit, 4.4f)
    apparatus(p, listOf(80f to 64f, 80f to 92f), unit, 2.8f)

    val kneeX = lerp(50f, 62f, t)
    val kneeY = lerp(64f, 56f, t)
    val ankleX = lerp(64f, 76f, t)
    val ankleY = lerp(62f, 50f, t)

    rod(p, listOf(44f to 64f, 31f to 50f, 26f to 43f), accent, 5f, unit)
    rod(p, listOf(44f to 64f, kneeX to kneeY, ankleX to ankleY, ankleX + 3f to ankleY - 2f), accent, 3.4f, unit)
    head(p, 26f, 40f, unit, accent)
    rod(p, listOf(31f to 50f, 36f to 56f, 43f to 58f), accent, 3f, unit)
}

private fun DrawScope.drawRomanianDeadlift(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val hipX = lerp(49f, 45f, t)
    val hipY = lerp(56f, 63f, t)
    val shoulderX = lerp(49f, 57f, t)
    val shoulderY = lerp(31f, 45f, t)
    val headX = lerp(50f, 60f, t)
    val headY = lerp(19f, 36f, t)
    val kneeX = lerp(49f, 47f, t)
    val elbowX = lerp(52f, 58f, t)
    val elbowY = lerp(44f, 57f, t)
    val wristX = lerp(51f, 59f, t)
    val wristY = lerp(57f, 70f, t)

    rod(p, listOf(hipX - 2f to hipY, kneeX - 2f to 75f, 47f to 92f, 54f to 93f), accent, 3f, unit, 0.4f)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit, 0.4f)
    rod(p, listOf(hipX to hipY, shoulderX to shoulderY, headX - 2f to headY + 4f), accent, 5f, unit)
    rod(p, listOf(hipX to hipY, kneeX to 75f, 49f to 92f, 57f to 93f), accent, 3.2f, unit)
    head(p, headX, headY, unit, accent)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    sideWeight(p, wristX, wristY, unit, accent, 5.4f)
}

private fun DrawScope.drawCalfRaise(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent, y = 92f)
    val lift = 6f * t
    val headY = 19f - lift
    val shoulderY = 31f - lift
    val hipY = 56f - lift
    val kneeY = 74f - lift
    val ankleY = 88f - lift

    rod(p, listOf(50f to hipY, 49f to kneeY, 50f to ankleY, 58f to 91.5f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 50f to shoulderY, 50f to headY + 4f), accent, 5f, unit)
    rod(p, listOf(45f to 91.5f - 4f * t, 50f to ankleY, 58f to 91.5f), accent, 2.6f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to shoulderY, 56f to 44f - lift, 51f to 53f - lift), accent, 3f, unit)
}

private fun DrawScope.drawLateralRaise(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val elbowX = lerp(41f, 35f, t)
    val elbowY = lerp(42f, 32f, t)
    val wristX = lerp(37f, 22f, t)
    val wristY = lerp(53f, 31f, t)

    rod(p, listOf(50f to 57f, 43f to 75f, 40f to 92f, 34f to 93f), accent, 3.2f, unit)
    rod(p, listOf(50f to 57f, 57f to 75f, 60f to 92f, 66f to 93f), accent, 3.2f, unit)
    rod(p, listOf(50f to 57f, 50f to 31f, 50f to 23f), accent, 5f, unit)
    head(p, 50f, 19f, unit, accent)
    rod(p, listOf(50f to 31f, 100f - elbowX to elbowY, 100f - wristX to wristY), accent, 2.9f, unit, 0.85f)
    rod(p, listOf(50f to 31f, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
    dumbbell(p, 100f - wristX, wristY, unit, accent)
    dumbbell(p, wristX, wristY, unit, accent)
}

private fun DrawScope.drawPlank(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent, y = 90f)
    val bob = 1.2f * t
    val shoulderY = 61f - bob
    val hipY = 65f - bob * 0.7f
    val kneeY = 71f - bob * 0.4f
    val ankleY = 80f

    rod(p, listOf(45f to 86f, 54f to 87.5f), accent, 2.6f, unit, 0.5f)
    rod(p, listOf(38f to shoulderY, 62f to hipY, 75f to kneeY, 87f to ankleY, 91f to 88f), accent, 5f, unit)
    rod(p, listOf(38f to shoulderY, 38f to 86f, 48f to 88f), accent, 3f, unit)
    head(p, 31f, 57f - bob, unit, accent)
}

private fun DrawScope.drawCrunch(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent, y = 90f)
    val shoulderX = lerp(44f, 48f, t)
    val shoulderY = lerp(80f, 70f, t)
    val headX = lerp(35f, 40f, t)
    val headY = lerp(78f, 62f, t)
    val elbowX = lerp(44f, 48f, t)
    val elbowY = lerp(73f, 63f, t)
    val wristX = lerp(37f, 41f, t)
    val wristY = lerp(74f, 64f, t)

    rod(p, listOf(62f to 82f, 66f to 68f, 78f to 87f, 84f to 88.5f), accent, 3.2f, unit)
    rod(p, listOf(62f to 82f, shoulderX to shoulderY, headX to headY + 3f), accent, 5f, unit)
    head(p, headX, headY, unit, accent)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 2.9f, unit)
}

private fun DrawScope.drawBurpee(
    p: (Float, Float) -> Offset,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent, y = 92f)
    val hipX = lerp(48f, 48f, t)
    val hipY = lerp(68f, 50f, t)
    val shoulderX = lerp(52f, 48f, t)
    val shoulderY = lerp(47f, 23f, t)
    val headX = lerp(54f, 48f, t)
    val headY = lerp(37f, 13f, t)
    val kneeX = lerp(60f, 48f, t)
    val kneeY = lerp(78f, 72f, t)
    val ankleX = lerp(56f, 48f, t)
    val ankleY = lerp(91f, 86f, t)
    val elbowX = lerp(58f, 45f, t)
    val elbowY = lerp(58f, 16f, t)
    val wristX = lerp(63f, 43f, t)
    val wristY = lerp(84f, 6f, t)

    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit, 0.4f)
    rod(p, listOf(hipX to hipY, shoulderX to shoulderY, headX to headY + 4f), accent, 5f, unit)
    rod(p, listOf(hipX to hipY, kneeX to kneeY, ankleX to ankleY, ankleX - 6f to 91f), accent, 3.2f, unit)
    head(p, headX, headY, unit, accent)
    rod(p, listOf(shoulderX to shoulderY, elbowX to elbowY, wristX to wristY), accent, 3f, unit)
}

private fun DrawScope.drawJumpRope(
    p: (Float, Float) -> Offset,
    phase: Float,
    t: Float,
    unit: Float,
    accent: Color,
) {
    floorLine(p, unit, accent)
    val lift = 4f * sin(phase * PI.toFloat())
    val headY = 19f - lift
    val shoulderY = 31f - lift
    val hipY = 56f - lift
    val ankleY = 88f - lift

    // Скакалка
    val bottomX = 50f + 15f * sin(phase * 2f * PI.toFloat())
    val rope = Path()
    val left = p(35f, 52f - lift)
    val right = p(65f, 52f - lift)
    rope.moveTo(left.x, left.y)
    rope.quadraticTo(
        p(bottomX - 9f, 80f).x, p(bottomX - 9f, 80f).y,
        p(bottomX, 90f).x, p(bottomX, 90f).y,
    )
    rope.quadraticTo(
        p(bottomX + 9f, 80f).x, p(bottomX + 9f, 80f).y,
        right.x, right.y,
    )
    drawPath(
        path = rope,
        color = RepColors.Cyan.copy(alpha = 0.65f),
        style = Stroke(width = 1.6f * unit, cap = StrokeCap.Round),
    )

    rod(p, listOf(50f to hipY, 44f to 74f - lift, 42f to ankleY, 38f to ankleY + 1f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 56f to 74f - lift, 58f to ankleY, 62f to ankleY + 1f), accent, 3.2f, unit)
    rod(p, listOf(50f to hipY, 50f to shoulderY, 50f to headY + 4f), accent, 5f, unit)
    head(p, 50f, headY, unit, accent)
    rod(p, listOf(50f to shoulderY, 39f to 42f - lift, 35f to 52f - lift), accent, 2.9f, unit, 0.85f)
    rod(p, listOf(50f to shoulderY, 61f to 42f - lift, 65f to 52f - lift), accent, 2.9f, unit, 0.85f)
    rod(p, listOf(35f to 52f - lift, 35f to 57f - lift), accent, 2.6f, unit)
    rod(p, listOf(65f to 52f - lift, 65f to 57f - lift), accent, 2.6f, unit)
}
