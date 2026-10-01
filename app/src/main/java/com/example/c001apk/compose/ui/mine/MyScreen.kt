package com.example.c001apk.compose.ui.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.state.LoadingState
import com.example.c001apk.compose.ui.component.cards.AppCard
import com.example.c001apk.compose.ui.component.cards.AppCardType
import com.example.c001apk.compose.ui.component.cards.MessageFFFCard
import com.example.c001apk.compose.ui.component.cards.MessageHeaderCard
import com.example.c001apk.compose.ui.component.cards.MessageWidgetCard
import com.example.c001apk.compose.ui.ffflist.FFFContentViewModel
import com.example.c001apk.compose.ui.ffflist.FFFListType
import com.example.c001apk.compose.ui.message.MessageViewModel
import com.example.c001apk.compose.util.CookieUtil

/**
 * 我的页面：底栏第二个 tab，右上角提供消息与设置入口
 *
 * 上半部分复用原消息页的资料卡片（头像/等级/经验、动态/关注/粉丝、功能宫格），
 * 下半部分将消息列表替换为「我的常去」展开显示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen(
    badge: Int,
    onMessageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogin: () -> Unit,
    onViewUser: (String) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onViewFFFList: (String?, String) -> Unit,
    onViewHistory: (String) -> Unit,
) {

    val prefs = LocalUserPreferences.current

    val messageViewModel =
        hiltViewModel<MessageViewModel, MessageViewModel.ViewModelFactory> { factory ->
            factory.create(url = "/v6/notification/list")
        }
    // 按 uid 区分 key，登录/登出/切换账号时自动使用新的 ViewModel 实例加载数据
    val recentViewModel =
        hiltViewModel<FFFContentViewModel, FFFContentViewModel.ViewModelFactory>(
            key = "mine_recent_${CookieUtil.uid}"
        ) { factory ->
            factory.create(
                url = "/v6/user/recentHistoryList",
                uid = CookieUtil.uid,
                id = null,
                showDefault = null
            )
        }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(prefs.isLogin) {
        if (prefs.isLogin && messageViewModel.fffList.isEmpty()) {
            messageViewModel.refresh()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "我的") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (badge > 0) {
                                Badge { Text(text = badge.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onMessageClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "消息"
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                MessageHeaderCard(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    isLogin = prefs.isLogin,
                    userAvatar = prefs.userAvatar,
                    userName = prefs.username,
                    level = prefs.level,
                    experience = prefs.experience,
                    nextLevelExperience = prefs.nextLevelExperience,
                    onLogin = onLogin,
                    onLogout = { showLogoutDialog = true },
                    onViewUser = onViewUser
                )
            }

            if (prefs.isLogin) {
                item(key = "fff") {
                    MessageFFFCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fffList = messageViewModel.fffList,
                        onViewFFFList = onViewFFFList,
                    )
                }

                item(key = "widget") {
                    MessageWidgetCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        onViewFFFList = onViewFFFList,
                        onViewHistory = onViewHistory,
                    )
                }

                item(key = "recent_title") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "我的常去",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        TextButton(
                            onClick = {
                                onViewFFFList(CookieUtil.uid, FFFListType.RECENT.name)
                            }
                        ) {
                            Text(text = "查看全部")
                        }
                    }
                }

                when (val state = recentViewModel.loadingState) {
                    is LoadingState.Success -> {
                        items(
                            items = state.response,
                            key = { (it.id ?: "") + (it.targetId ?: "") + (it.targetType ?: "") }
                        ) { data ->
                            AppCard(
                                data = data,
                                onOpenLink = onOpenLink,
                                appCardType = AppCardType.RECENT,
                                onViewUser = onViewUser,
                            )
                        }
                    }

                    is LoadingState.Loading -> {
                        item(key = "recent_loading") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        messageViewModel.onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            title = {
                Text(text = "确定退出登录？")
            }
        )
    }
}
