package com.huanchengfly.tieba.post.api.models

import com.google.gson.annotations.SerializedName
import com.huanchengfly.tieba.post.models.BaseBean
import com.huanchengfly.tieba.post.models.ErrorBean

class MsgBean : ErrorBean() {
    val message: MessageBean? = null

    inner class MessageBean : BaseBean() {
        @SerializedName("replyme")
        val replyMe: String? = null

        @SerializedName("atme")
        val atMe: String? = null
        val fans: String? = null

        @SerializedName("agree")
        val agree: String? = null

        @SerializedName("agreed")
        val agreed: String? = null

        @SerializedName("agreecount")
        val agreeCount: String? = null

        @SerializedName("agree_me")
        val agreeMe: String? = null

        fun getAgreeNum(): Int {
            return (agree?.toIntOrNull() ?: 0).coerceAtLeast(
                (agreed?.toIntOrNull() ?: 0).coerceAtLeast(
                    (agreeCount?.toIntOrNull() ?: 0).coerceAtLeast(
                        agreeMe?.toIntOrNull() ?: 0
                    )
                )
            )
        }
    }
}