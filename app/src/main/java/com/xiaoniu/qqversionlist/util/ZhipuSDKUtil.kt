// SPDX-License-Identifier: AGPL-3.0-or-later

/*
    Qverbow Util
    Copyright (C) 2023 klxiaoniu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.xiaoniu.qqversionlist.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.zhipu.oapi.ClientV4
import com.zhipu.oapi.Constants
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest
import com.zhipu.oapi.service.v4.model.ChatMessage
import com.zhipu.oapi.service.v4.model.ChatMessageRole
import okhttp3.ConnectionPool
import java.util.concurrent.TimeUnit

object ZhipuSDKUtil {
    const val REQUEST_ID_TEMPLATE = "request_id_%s"
    const val TEMPERATURE = "temperature"
    const val MAX_TOKENS = "max_tokens"

    /**
     * 使用指定的系统提示和问题，通过 Zhipu API 获取回答
     *
     * @param systemPrompt 系统提示文本，用于设置对话的背景或规则
     * @param question 用户提出的问题文本
     * @param token 访问 Zhipu API 所需的认证令牌
     * @return Zhipu API 返回的回答内容的字符串表示，如果请求失败则可能返回 null
     */
    fun getZhipuWrite(systemPrompt: String, question: String, token: String): String? {
        val client = ClientV4.Builder(token).networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(8, 1, TimeUnit.SECONDS)).build()

        val messages = ArrayList<ChatMessage>()
        val systemMessage = ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt)
        val chatMessage = ChatMessage(ChatMessageRole.USER.value(), question)
        messages.add(systemMessage)
        messages.add(chatMessage)
        val requestId = String.format(REQUEST_ID_TEMPLATE, System.currentTimeMillis())

        val extraJson = hashMapOf<String, Any>()
        extraJson[TEMPERATURE] = 0
        extraJson[MAX_TOKENS] = 1024

        val chatCompletionRequest =
            ChatCompletionRequest.builder().model(Constants.ModelChatGLM4Flash).stream(false)
                .invokeMethod(Constants.invokeMethod).messages(messages).requestId(requestId)
                .extraJson(extraJson).build()

        val invokeModelApiResp = client.invokeModelApi(chatCompletionRequest)
        return ObjectMapper().writeValueAsString(invokeModelApiResp)
    }
}