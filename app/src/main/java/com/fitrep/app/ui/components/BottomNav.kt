package com.fitrep.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fitrep.app.ui.theme.RepColors

@Composable
fun BottomNav(
    selectedTab: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .shadow(24.dp, shape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .background(RepColors.Surface.copy(alpha = 0.98f), shape)
            .border(1.dp, RepColors.Outline, shape)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NavTab(
            label = "Тренировки",
            icon = Icons.Rounded.FitnessCenter,
            selected = selectedTab == 0,
            onClick = { onSelect(0) },
        )
        NavTab(
            label = "Статистика",
            icon = Icons.Rounded.BarChart,
            selected = selectedTab == 1,
            onClick = { onSelect(1) },
        )
    }
}

@Composable
private fun NavTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (selected) RepColors.Lime else Color.Transparent,
        animationSpec = spring(stiffness = 700f),
        label = "tabBackground",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF0B1400) else RepColors.TextMuted,
        animationSpec = spring(stiffness = 700f),
        label = "tabContent",
    )
    Row(
        modifier = Modifier
            .background(background, RoundedCornerShape(19.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = RepColors.Lime),
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}
