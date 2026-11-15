package com.fitrep.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitrep.app.data.WorkoutSession
import com.fitrep.app.data.WorkoutStore
import com.fitrep.app.data.WorkoutTemplate
import com.fitrep.app.ui.components.BottomNav
import com.fitrep.app.ui.components.ScreenBackground
import com.fitrep.app.ui.screens.HomeScreen
import com.fitrep.app.ui.screens.StatsScreen
import com.fitrep.app.ui.screens.SummaryScreen
import com.fitrep.app.ui.screens.WorkoutScreen
import com.fitrep.app.ui.theme.RepColors

private sealed interface Route {
    data object Home : Route
    data object Stats : Route
    data object Workout : Route
    data class Summary(val session: WorkoutSession, val fresh: Boolean) : Route
}

private fun depth(route: Route): Int = when (route) {
    is Route.Home -> 0
    is Route.Stats -> 0
    is Route.Workout -> 1
    is Route.Summary -> if (route.fresh) 2 else 1
}

@Composable
fun RepApp(store: WorkoutStore) {
    val history by store.history.collectAsStateWithLifecycle()
    val active by store.active.collectAsStateWithLifecycle()

    var route by remember { mutableStateOf<Route>(Route.Home) }
    var pendingTemplate by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var lastSession by remember { mutableStateOf<WorkoutSession?>(null) }

    LaunchedEffect(active) {
        if (active != null) lastSession = active
    }

    fun startTemplate(template: WorkoutTemplate) {
        if (active != null) {
            pendingTemplate = template
        } else {
            store.start(template)
            route = Route.Workout
        }
    }

    BackHandler(enabled = route !is Route.Home) {
        route = when (route) {
            is Route.Summary -> Route.Home
            is Route.Stats -> Route.Home
            is Route.Workout -> Route.Home
            is Route.Home -> Route.Home
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScreenBackground()
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            AnimatedContent(
                targetState = route,
                transitionSpec = {
                    val forward = depth(targetState) >= depth(initialState)
                    val direction = if (forward) 1 else -1
                    val enter = slideInHorizontally(
                        animationSpec = tween(360, easing = FastOutSlowInEasing),
                        initialOffsetX = { width -> direction * width / 3 },
                    ) + fadeIn(tween(240))
                    val exit = slideOutHorizontally(
                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                        targetOffsetX = { width -> -direction * width / 5 },
                    ) + fadeOut(tween(180))
                    enter togetherWith exit
                },
                label = "routeTransition",
            ) { currentRoute ->
                when (currentRoute) {
                    is Route.Home -> HomeScreen(
                        history = history,
                        active = active,
                        onStartTemplate = ::startTemplate,
                        onResume = { route = Route.Workout },
                        onOpenSession = { route = Route.Summary(it, fresh = false) },
                        onOpenStats = { route = Route.Stats },
                        onDiscardActive = {
                            store.discard()
                            route = Route.Home
                        },
                    )

                    is Route.Stats -> StatsScreen(
                        history = history,
                        onBack = { route = Route.Home },
                    )

                    is Route.Workout -> {
                        val session = active ?: lastSession
                        if (session != null) {
                            WorkoutScreen(
                                session = session,
                                store = store,
                                onFinish = {
                                    val finished = store.finish()
                                    if (finished != null) {
                                        route = Route.Summary(finished, fresh = true)
                                    }
                                },
                                onMinimize = { route = Route.Home },
                            )
                        }
                    }

                    is Route.Summary -> SummaryScreen(
                        session = currentRoute.session,
                        fresh = currentRoute.fresh,
                        onHome = { route = Route.Home },
                    )
                }
            }

            if (route is Route.Home || route is Route.Stats) {
                BottomNav(
                    selectedTab = if (route is Route.Stats) 1 else 0,
                    onSelect = { tab ->
                        route = if (tab == 1) Route.Stats else Route.Home
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 18.dp),
                )
            }
        }
    }

    val template = pendingTemplate
    if (template != null) {
        AlertDialog(
            onDismissRequest = { pendingTemplate = null },
            containerColor = RepColors.SurfaceHigh,
            titleContentColor = RepColors.TextPrimary,
            textContentColor = RepColors.TextSecondary,
            shape = RoundedCornerShape(26.dp),
            title = { Text("Начать новую тренировку?") },
            text = {
                Text(
                    "Незавершённая тренировка «${active?.name.orEmpty()}» будет сброшена. " +
                        "Если хочешь сохранить прогресс — сначала заверши её.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    store.discard()
                    store.start(template)
                    pendingTemplate = null
                    route = Route.Workout
                }) {
                    Text("Начать заново", color = RepColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingTemplate = null }) {
                    Text("Отмена", color = RepColors.TextSecondary)
                }
            },
        )
    }
}
