package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.api.adapters.MessageListAdapter
import com.huanchengfly.tieba.post.api.adapters.PortraitAdapter
import com.huanchengfly.tieba.post.models.BaseBean

class MessageListBean : BaseBean() {
    @SerializedName("error_code")
    val errorCode: String? = null
    val time: Long = 0

    @SerializedName("has_more")
    val hasMore: String? = null

    @JsonAdapter(MessageListAdapter::class)
    @SerializedName("reply_list")
    val replyList: List<MessageInfoBean>? = null

    @JsonAdapter(MessageListAdapter::class)
    @SerializedName("at_list")
    val atList: List<MessageInfoBean>? = null

    @JsonAdapter(MessageListAdapter::class)
    @SerializedName("agree_list", alternate = ["agreed_list", "agreeList"])
    val agreeList: List<MessageInfoBean>? = null
    val page: PageInfoBean? = null
    val message: MessageBean? = null

    fun getErrorCode(): Int = Integer.valueOf(errorCode!!)

    data class UserInfoBean(
        val id: String? = null,
        val name: String? = null,

        @SerializedName("name_show")
        val nameShow: String? = null,

        @JsonAdapter(PortraitAdapter::class)
        val portrait: String? = null,
    )

    data class ThreadInfoBean(
        val id: String? = null,
        val title: String? = null,
        val fname: String? = null,
        val fid: String? = null,
        @SerializedName("thread_type")
        val threadType: String? = null,
    )

    data class PostInfoContentBean(
        val text: String? = null,
        val type: String? = null,
        val src: String? = null,
    )

    data class PostInfoBean(
        val id: String? = null,
        val author: UserInfoBean? = null,
        val content: List<PostInfoContentBean>? = null,
        @SerializedName("quote_id")
        val quoteId: String? = null,
        val ptype: String? = null,
    ) {
        fun getText(): String? {
            return content?.joinToString("") { it.text.orEmpty() }?.takeIf { it.isNotBlank() }
        }
    }

    data class ReplyerInfoBean(
        @SerializedName("id", alternate = ["user_id", "uid"])
        val id: String? = null,

        @SerializedName("name", alternate = ["user_name", "author_name"])
        val name: String? = null,

        @SerializedName("name_show", alternate = ["show_name", "nickname"])
        val nameShow: String? = null,

        @JsonAdapter(PortraitAdapter::class)
        @SerializedName("portrait", alternate = ["user_portrait", "icon"])
        val portrait: String? = null,

        @SerializedName("is_friend")
        val isFriend: String? = null,

        @SerializedName("is_fans")
        val isFans: String? = null,
    )

    data class MessageInfoBean(
        @SerializedName("is_floor", alternate = ["is_sub_post"])
        val isFloor: String? = null,

        @SerializedName("title", alternate = ["thread_title", "target_title", "subject"])
        val title: String? = null,

        @SerializedName("content", alternate = ["post_content", "text", "msg", "agree_content"])
        val content: String? = null,

        @SerializedName("quote_content", alternate = ["target_content", "orig_content", "quote_post_content", "source_content"])
        //有时候会引用的回复楼，有时候引用的楼中楼
        val quoteContent: String? = null,

        @SerializedName("agreeer", alternate = ["replyer", "from_user", "user", "agree_user", "author", "user_info", "liker"])
        val replyer: ReplyerInfoBean? = null,

        @SerializedName("thread_info")
        val threadInfo: ThreadInfoBean? = null,

        @SerializedName("post_info")
        val postInfo: PostInfoBean? = null,

        @SerializedName("type")
        val agreeType: String? = null,

        @SerializedName("op_time")
        val opTime: String? = null,

        @SerializedName("user_id", alternate = ["uid", "from_uid"])
        val userId: String? = null,

        @SerializedName("user_name", alternate = ["author_name", "from_name"])
        val userName: String? = null,

        @SerializedName("name_show", alternate = ["show_name", "nickname", "from_name_show"])
        val nameShow: String? = null,

        @JsonAdapter(PortraitAdapter::class)
        @SerializedName("portrait", alternate = ["user_portrait", "icon", "from_portrait"])
        val portrait: String? = null,

        @SerializedName("quote_user")
        val quoteUser: UserInfoBean? = null,

        @SerializedName("thread_id", alternate = ["tid", "target_thread_id"])
        val threadId: String? = null,

        @SerializedName("post_id", alternate = ["pid", "target_post_id"])
        val postId: String? = null,

        @SerializedName("time", alternate = ["agree_time", "create_time", "time_stamp"])
        val time: String? = null,

        @SerializedName("fname", alternate = ["forum_name"])
        val forumName: String? = null,

        @SerializedName("quote_pid")
        val quotePid: String? = null,

        @SerializedName("thread_type")
        val threadType: String? = null,

        val unread: String? = null,
    ) {
        fun getEffectiveReplyer(): ReplyerInfoBean? {
            if (replyer != null && (!replyer.id.isNullOrEmpty() || !replyer.name.isNullOrEmpty() || !replyer.nameShow.isNullOrEmpty() || !replyer.portrait.isNullOrEmpty())) {
                return replyer
            }
            if (!userId.isNullOrEmpty() || !userName.isNullOrEmpty() || !nameShow.isNullOrEmpty() || !portrait.isNullOrEmpty()) {
                return ReplyerInfoBean(
                    id = userId,
                    name = userName,
                    nameShow = nameShow,
                    portrait = portrait
                )
            }
            return replyer
        }

        fun getEffectiveTitle(): String? {
            return threadInfo?.title ?: title
        }

        fun getEffectiveQuoteContent(): String? {
            return postInfo?.getText() ?: quoteContent ?: threadInfo?.title ?: title
        }

        fun getEffectiveThreadId(): String? {
            return threadId ?: threadInfo?.id
        }

        fun getEffectivePostId(): String? {
            return postId ?: postInfo?.id
        }

        fun getEffectiveTime(): String? {
            return time?.takeIf { it.isNotBlank() } ?: opTime
        }
    }

    class MessageBean {
        @SerializedName("replyme")
        val replyMe: String? = null

        @SerializedName("atme")
        val atMe: String? = null
        val fans: String? = null
        val recycle: String? = null

        @SerializedName("storethread")
        val storeThread: String? = null

    }

    class PageInfoBean {
        @SerializedName("current_page")
        val currentPage: String? = null

        @SerializedName("has_more")
        val hasMore: String? = null

        @SerializedName("has_prev")
        val hasPrev: String? = null
    }
}