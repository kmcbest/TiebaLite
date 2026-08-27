package com.huanchengfly.tieba.post.ui.page.main.notifications.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.arch.collectPartialAsState
import com.huanchengfly.tieba.post.arch.pageViewModel
import com.huanchengfly.tieba.post.ui.common.theme.compose.ExtendedTheme
import com.huanchengfly.tieba.post.ui.common.theme.compose.pullRefreshIndicator
import com.huanchengfly.tieba.post.ui.page.LocalNavigator
import com.huanchengfly.tieba.post.ui.page.destinations.SubPostsPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.ThreadPageDestination
import com.huanchengfly.tieba.post.ui.page.destinations.UserProfilePageDestination
import com.huanchengfly.tieba.post.ui.widgets.compose.Avatar
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockTip
import com.huanchengfly.tieba.post.ui.widgets.compose.BlockableContent
import com.huanchengfly.tieba.post.ui.widgets.compose.Container
import com.huanchengfly.tieba.post.ui.widgets.compose.EmoticonText
import com.huanchengfly.tieba.post.ui.widgets.compose.LazyLoad
import com.huanchengfly.tieba.post.ui.widgets.compose.LoadMoreLayout
import com.huanchengfly.tieba.post.ui.widgets.compose.MyLazyColumn
import com.huanchengfly.tieba.post.ui.widgets.compose.Sizes
import com.huanchengfly.tieba.post.ui.widgets.compose.UserHeader
import com.huanchengfly.tieba.post.ui.widgets.compose.debounceClickable
import com.huanchengfly.tieba.post.utils.AgreeDebugUtil
import com.huanchengfly.tieba.post.utils.DateTimeUtils
import com.huanchengfly.tieba.post.utils.StringUtil
import com.huanchengfly.tieba.post.utils.TiebaUtil
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationsListPage(
    type: NotificationsType,
) {
    val viewModel: NotificationsListViewModel = when (type) {
        NotificationsType.ReplyMe -> pageViewModel<NotificationsListUiIntent, ReplyMeListViewModel>(key = "ReplyMe")
        NotificationsType.AtMe -> pageViewModel<NotificationsListUiIntent, AtMeListViewModel>(key = "AtMe")
        NotificationsType.AgreeMe -> pageViewModel<NotificationsListUiIntent, AgreeMeListViewModel>(key = "AgreeMe")
    }
    LazyLoad(key = type, loaded = viewModel.initialized) {
        viewModel.send(NotificationsListUiIntent.Refresh)
        viewModel.initialized = true
    }
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val isRefreshing by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::isRefreshing,
        initial = false
    )
    val isLoadingMore by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::isLoadingMore,
        initial = false
    )
    val hasMore by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::hasMore,
        initial = true
    )
    val data by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::data,
        initial = persistentListOf()
    )
    val currentPage by viewModel.uiState.collectPartialAsState(
        prop1 = NotificationsListUiState::currentPage,
        initial = 1
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.send(NotificationsListUiIntent.Refresh) }
    )
    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier.pullRefresh(pullRefreshState)
    ) {
        LoadMoreLayout(
            isLoading = isLoadingMore,
            onLoadMore = { viewModel.send(NotificationsListUiIntent.LoadMore(currentPage + 1)) },
            loadEnd = !hasMore,
            lazyListState = lazyListState,
        ) {
            MyLazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                state = lazyListState,
            ) {
                itemsIndexed(
                    items = data,
                    key = { index, (info, _) ->
                        val rep = info.getEffectiveReplyer()
                        "${index}_${info.getEffectivePostId()}_${rep?.id}_${info.getEffectiveTime()}"
                    },
                ) { _, (info, blocked) ->
                    Container {
                        BlockableContent(
                            blocked = blocked,
                            blockedTip = {
                                BlockTip {
                                    Text(
                                        text = stringResource(id = R.string.tip_blocked_message)
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .debounceClickable(onClick =  {
                                        val tid = info.getEffectiveThreadId()?.toLongOrNull()
                                        val pid = info.getEffectivePostId()?.toLongOrNull()
                                        if (tid != null && tid > 0) {
                                            if (info.isFloor == "1" && pid != null && pid > 0) {
                                                navigator.navigate(
                                                    SubPostsPageDestination(
                                                        threadId = tid,
                                                        postId = 0,
                                                        subPostId = pid,
                                                        loadFromSubPost = true
                                                    )
                                                )
                                            } else {
                                                navigator.navigate(
                                                    ThreadPageDestination(
                                                        threadId = tid,
                                                        postId = pid ?: 0
                                                    )
                                                )
                                            }
                                        }
                                    })
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val replyer = info.getEffectiveReplyer()
                                if (replyer != null) {
                                    UserHeader(
                                        avatar = {
                                            Avatar(
                                                data = StringUtil.getAvatarUrl(replyer.portrait),
                                                size = Sizes.Small,
                                                contentDescription = null
                                            )
                                        },
                                        name = {
                                            val displayName = replyer.nameShow?.ifEmpty { null } ?: replyer.name?.ifEmpty { null } ?: ""
                                            Text(text = displayName)
                                        },
                                        onClick = {
                                            replyer.id?.toLongOrNull()?.let { uid ->
                                                navigator.navigate(UserProfilePageDestination(uid))
                                            }
                                        },
                                        desc = {
                                            val timeStr = info.getEffectiveTime()
                                            if (!timeStr.isNullOrBlank()) {
                                                Text(
                                                    text = DateTimeUtils.getRelativeTimeString(
                                                        LocalContext.current,
                                                        timeStr
                                                    )
                                                )
                                            }
                                        },
                                    ) {}
                                }
                                val mainText = if (type == NotificationsType.AgreeMe && info.content.isNullOrBlank()) {
                                    val pid = info.getEffectivePostId()
                                    val tid = info.getEffectiveThreadId()
                                    if (info.isFloor == "1") {
                                        "赞了你的楼中楼"
                                    } else if (pid != null && pid != "0" && pid != tid) {
                                        "赞了你的回复"
                                    } else if (!info.quoteContent.isNullOrBlank() || info.postInfo != null) {
                                        "赞了你的回复"
                                    } else {
                                        "赞了你的贴子"
                                    }
                                } else {
                                    info.content ?: ""
                                }
                                if (mainText.isNotBlank()) {
                                    EmoticonText(text = mainText)
                                }
                                val quoteText = when (type) {
                                    NotificationsType.ReplyMe -> {
                                        if ("1" == info.isFloor) {
                                            info.quoteContent
                                        } else {
                                            stringResource(
                                                id = R.string.text_message_list_item_reply_my_thread,
                                                info.title ?: ""
                                            )
                                        }
                                    }
                                    NotificationsType.AtMe -> info.title
                                    NotificationsType.AgreeMe -> info.getEffectiveQuoteContent()
                                }
                                if (quoteText != null) {
                                    EmoticonText(
                                        text = quoteText,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .debounceClickable(onClick =  {
                                                val tid = info.getEffectiveThreadId()?.toLongOrNull()
                                                val pid = info.getEffectivePostId()?.toLongOrNull()
                                                if (tid != null && tid > 0) {
                                                    if (info.isFloor == "1" && pid != null && pid > 0) {
                                                        navigator.navigate(
                                                            SubPostsPageDestination(
                                                                threadId = tid,
                                                                postId = info.quotePid?.toLongOrNull() ?: 0,
                                                                subPostId = pid,
                                                                loadFromSubPost = true
                                                            )
                                                        )
                                                    } else {
                                                        navigator.navigate(
                                                            ThreadPageDestination(
                                                                threadId = tid,
                                                                postId = pid ?: 0
                                                            )
                                                        )
                                                    }
                                                }
                                            })
                                            .background(
                                                ExtendedTheme.colors.chip,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(8.dp),
                                        color = ExtendedTheme.colors.onChip,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = ExtendedTheme.colors.pullRefreshIndicator,
            contentColor = ExtendedTheme.colors.primary,
        )
    }
}