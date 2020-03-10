/*
 *
 * Mirai Native
 *
 * Copyright (C) 2020 iTX Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author PeratX
 * @website https://github.com/iTXTech/mirai-native
 *
 */

package org.itxtech.mirainative

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import java.io.File

object ChainCodeConverter {
    private fun String.escape(c: Boolean = false): String {
        var s = replace("&", "&amp;")
            .replace("[", "&#91;")
            .replace("]", "&#93;")
        if (c) {
            s = s.replace(",", "&#44;")
        }
        return s
    }

    private fun String.toMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        this.split(",").forEach {
            val parts = it.split("=")
            map[parts[0]] = parts[1].escape(true)
        }
        return map
    }

    private suspend fun String.toMessageInternal(contact: Contact?): Message {
        return if (this.startsWith("[CQ:") && this.endsWith("]")) {
            val c = this.substring(4, this.length - 1)
            if (c.contains(",")) { // TODO: 支持更多码
                val parts = c.split(",")
                val args = parts[1].toMap()
                when (parts[0]) {
                    "at" -> {
                        if (args["qq"] == "all") {
                            AtAll
                        } else {
                            val group = MiraiNative.INSTANCE.bot.getGroupOrNull(contact!!.id)
                            if (group == null) {
                                MiraiNative.INSTANCE.logger.debug("你群没了：${contact.id}")
                                return PlainText("")
                            }
                            val member = group.getOrNull(args["qq"]!!.toLong())
                            if (member == null) {
                                MiraiNative.INSTANCE.logger.debug("你人没了：${args["qq"]}")
                                return PlainText.Empty
                            }
                            At(member)
                        }
                    }
                    "face" -> {
                        Face(args["id"]!!.toInt())
                    }
                    "emoji" -> {
                        PlainText(String(Character.toChars(args["id"]!!.toInt())))
                    }
                    "image" -> {
                        val file = File(
                            MiraiNative.INSTANCE.dataFolder.absolutePath +
                                    File.separatorChar + "data" + File.separatorChar + "image" + File.separatorChar + args["file"]!!
                        )
                        if (file.exists()) {
                            contact!!.uploadImage(file)
                        } else {
                            PlainText.Empty
                        }
                    }
                    "share" -> {
                        XmlMessageHelper.share(args["url"]!!, args["title"], args["content"], args["image"])
                    }
                    else -> {
                        MiraiNative.INSTANCE.logger.debug("不支持的 CQ码：$c")
                        PlainText.Empty
                    }
                }
            } else {
                MiraiNative.INSTANCE.logger.debug("不支持的 CQ码 ：$c")
                PlainText.Empty
            }
        } else {
            PlainText(this)
        }
    }

    fun chainToCode(chain: MessageChain): String {
        return chain.joinToString(separator = "") {
            when (it) {
                is MessageMetadata -> ""
                is At -> "[CQ:at,qq=${it.target}]"
                is AtAll -> "[CQ:at,qq=all]"
                is PlainText -> it.stringValue.escape(false)
                is Face -> "[CQ:face,id=${it.id}]"
                else -> ""//error("不支持的消息类型：${it::class.simpleName}")
            }
        }
    }

    suspend fun codeToChain(message: String, contact: Contact?): MessageChain {
        return buildMessageChain {
            if (message.contains("[CQ:")) {
                var interpreting = false
                val sb = StringBuilder()
                var index = 0
                message.forEach { c: Char ->
                    if (c == '[') {
                        if (interpreting) {
                            MiraiNative.INSTANCE.logger.error("CQ消息解析失败：$message，索引：$index")
                            return@forEach
                        } else {
                            interpreting = true
                            if (sb.isNotEmpty()) {
                                val lastMsg = sb.toString()
                                sb.delete(0, sb.length)
                                +lastMsg.toMessageInternal(contact)
                            }
                            sb.append(c)
                        }
                    } else if (c == ']') {
                        if (!interpreting) {
                            MiraiNative.INSTANCE.logger.error("CQ消息解析失败：$message，索引：$index")
                            return@forEach
                        } else {
                            interpreting = false
                            sb.append(c)
                            if (sb.isNotEmpty()) {
                                val lastMsg = sb.toString()
                                sb.delete(0, sb.length)
                                +lastMsg.toMessageInternal(contact)
                            }
                        }
                    } else {
                        sb.append(c)
                    }
                    index++
                }
                if (sb.isNotEmpty()) {
                    +sb.toString().toMessageInternal(contact)
                }
            } else {
                +PlainText(message)
            }
        }
    }
}

