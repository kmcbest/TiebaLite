package com.huanchengfly.tieba.post.repository

import com.huanchengfly.tieba.post.api.TiebaApi
import com.huanchengfly.tieba.post.api.models.AddThreadBean
import com.huanchengfly.tieba.post.api.models.protos.addPost.AddPostResponse
import com.huanchengfly.tieba.post.arch.GlobalEvent
import com.huanchengfly.tieba.post.arch.emitGlobalEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

object AddPostRepository {
    fun addThread(
        content: String,
        forumId: Long,
        forumName: String,
        title: String? = "",
        isHide: Int? = 1,
        isTitle: Int? = 1
    ): Flow<AddThreadBean> =
        TiebaApi.getInstance()
            .addThreadFlow(
                content,
                forumName,
                forumId.toString(),
                title.orEmpty(),
                requireNotNull(isHide),
                requireNotNull(isTitle)
            ).onEach {
                GlobalScope.launch {
                    emitGlobalEvent(
                        GlobalEvent.AddThreadSuccess(
                            checkNotNull(it.tid?.toLong()),
                            checkNotNull(it.pid?.toLong()),
                            checkNotNull(it.errorMsg),
                        )
                    )
                }
            }

    fun addPost(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String? = null,
        nameShow: String? = null,
        postId: Long? = null,
        subPostId: Long? = null,
        replyUserId: Long? = null,
    ): Flow<AddPostResponse> =
        TiebaApi.getInstance()
            .addPostFlow(
                content,
                forumId.toString(),
                forumName,
                threadId.toString(),
                tbs,
                nameShow,
                postId?.toString(),
                subPostId?.toString(),
                replyUserId?.toString()
            )
            .onEach {
                val newPostId = checkNotNull(it.data_?.pid?.toLongOrNull())
                GlobalScope.launch {
                    if (postId != null) {
                        emitGlobalEvent(
                            GlobalEvent.ReplySuccess(
                                threadId,
                                postId,
                                postId,
                                subPostId,
                                newPostId
                            )
                        )
                    } else {
                        emitGlobalEvent(GlobalEvent.ReplySuccess(threadId, newPostId))
                    }
                }
            }

    fun webReply(
        content: String,
        forumId: Long,
        forumName: String,
        threadId: Long,
        tbs: String,
        webImgInfo: String,
        postId: Long? = null,
        subPostId: Long? = null,
    ): Flow<com.huanchengfly.tieba.post.api.models.WebReplyResultBean> = kotlinx.coroutines.flow.flow {
        val nickName = com.huanchengfly.tieba.post.utils.AccountUtil.getAccountInfo { name } ?: ""
        val call = com.huanchengfly.tieba.post.api.retrofit.RetrofitTiebaApi.WEB_TIEBA_API.webReply(
            content = content,
            imgInfo = webImgInfo,
            forumId = forumId.toString(),
            forumName = forumName,
            tbs = tbs,
            threadId = threadId.toString(),
            nickName = nickName,
            postId = postId?.toString(),
            replyPostId = subPostId?.toString(),
            bsk = "",
            referer = "https://tieba.baidu.com/p/$threadId?lp=5028&mo_device=1&is_jingpost=0&pn=1&"
        )
        val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { call.execute() }
        val body = response.body() ?: throw com.huanchengfly.tieba.post.api.retrofit.exception.TiebaUnknownException
        if (body.errorCode != 0) {
            throw com.huanchengfly.tieba.post.api.retrofit.exception.TiebaLocalException(body.errorCode, body.errorMsg ?: "回帖失败")
        }
        emit(body)
    }.onEach {
        val newPostId = it.data?.pid ?: 0L
        GlobalScope.launch {
            if (postId != null) {
                emitGlobalEvent(
                    GlobalEvent.ReplySuccess(
                        threadId,
                        postId,
                        postId,
                        subPostId,
                        newPostId
                    )
                )
            } else {
                emitGlobalEvent(GlobalEvent.ReplySuccess(threadId, newPostId))
            }
        }
    }
}