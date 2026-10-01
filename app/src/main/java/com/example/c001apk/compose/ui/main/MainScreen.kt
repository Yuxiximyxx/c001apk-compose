package com.example.c001apk.compose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.model.UpdateCheckItem
import com.example.c001apk.compose.ui.component.SlideTransition
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.home.HomeScreen
import com.example.c001apk.compose.ui.mine.MyScreen
import com.example.c001apk.compose.util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
@Composable
fun MainScreen(
    selectIndex: Int,
    setSelectIndex: (Int) -> Unit,
    badge: Int,
    resetBadge: () -> Unit,
    onParamsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onMessageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onSearch: () -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewApp: (String) -> Unit,
    onLogin: () -> Unit,
    onCheckUpdate: (List<UpdateCheckItem>) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewBlackList: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    widthSizeClass: WindowWidthSizeClass,
) {

    val screens = listOf(
        Router.HOME,
        Router.MINE,
    )

    val savableStateHolder = rememberSaveableStateHolder()
    val performHapticClick = rememberHapticClick {}
    var refreshState by remember { mutableStateOf(false) }

    val customNavSuiteType = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
        else -> NavigationSuiteType.NavigationRail
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            screens.forEach { screen ->
                item(
                    icon = {
                        BadgedBox(
                            badge = {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = if (screen == Router.MINE) badge > 0
                                    else false,
                                    enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                                    exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                                ) {
                                    Badge(
                                        modifier = Modifier
                                            .padding(start = 15.dp, bottom = 10.dp)
                                    ) {
                                        Text(text = badge.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector =
                                if (selectIndex == screens.indexOf(screen)) {
                                    screen.selectedIcon!!
                                } else {
                                    screen.unselectedIcon!!
                                },
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(text = stringResource(id = screen.stringId!!)) },
                    selected = selectIndex == screens.indexOf(screen),
                    onClick = {
                        performHapticClick()
                        with(screens.indexOf(screen)) {
                            if (selectIndex == 0 && this == 0) {
                                refreshState = true
                            }
                            setSelectIndex(this)
                        }
                    },
                    alwaysShowLabel = false
                )
            }
        },
        layoutType = customNavSuiteType
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            label = "home-content",
            targetState = selectIndex,
            transitionSpec = {
                SlideTransition.slideLeft.enterTransition()
                    .togetherWith(SlideTransition.slideLeft.exitTransition())
            },
        ) { page ->
            savableStateHolder.SaveableStateProvider(
                key = page,
                content = {
                    when (page) {
                        0 -> HomeScreen(
                            refreshState = refreshState,
                            onRefresh = {
                                refreshState = true
                            },
                            resetRefreshState = {
                                refreshState = false
                            },
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onSearch = onSearch,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onViewApp = onViewApp,
                            onCheckUpdate = onCheckUpdate,
                            onReport = onReport,
                        )

                        1 -> MyScreen(
                            badge = badge,
                            onMessageClick = {
                                resetBadge()
                                onMessageClick()
                            },
                            onSettingsClick = onSettingsClick,
                            onLogin = onLogin,
                            onViewUser = onViewUser,
                            onOpenLink = onOpenLink,
                            onViewFFFList = onViewFFFList,
                            onViewHistory = onViewHistory,
                        )
                    }
                }
            )
        }
    }

    BackHandler(enabled = selectIndex != 0) {
        setSelectIndex(0)
    }

}
