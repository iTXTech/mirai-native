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

package org.itxtech.mirainative.message

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadImage
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.util.Music
import org.itxtech.mirainative.util.NeteaseMusic
import org.itxtech.mirainative.util.QQMusic
import java.net.URL

object ChainCodeConverter {
    private val MSG_EMPTY = PlainText("")

    private fun String.escape(part: Boolean): String {
        val s = replace("&", "&amp;")
            .replace("[", "&#91;")
            .replace("]", "&#93;")
        return if (part) s.replace(",", "&#44;") else s
    }

    private fun String.unescape(part: Boolean): String {
        val s = replace("&amp;", "&")
            .replace("&#91;", "[")
            .replace("&#93;", "]")
        return if (part) s.replace("&#44;", ",") else s
    }

    private fun String.toMap() = HashMap<String, String>().apply {
        this@toMap.split(",").forEach {
            val parts = it.split(delimiters = *arrayOf("="), limit = 2)
            this[parts[0]] = parts[1].unescape(true)
        }
    }

    private suspend fun String.toMessageInternal(contact: Contact?): Message {
        if (startsWith("[CQ:") && endsWith("]")) {
            val parts = substring(4, length - 1).split(delimiters = *arrayOf(","), limit = 2)
            val args = if (parts.size == 2) {
                parts[1].toMap()
            } else {
                HashMap()
            }
            when (parts[0]) {
                "at" -> {
                    if (args["qq"] == "all") {
                        return AtAll
                    } else {
                        val group = MiraiNative.bot.getGroupOrNull(contact!!.id)
                        if (group == null) {
                            MiraiNative.logger.debug("你群没了：${contact.id}")
                            return MSG_EMPTY
                        }
                        val member = group.getOrNull(args["qq"]!!.toLong())
                        if (member == null) {
                            MiraiNative.logger.debug("你人没了：${args["qq"]}")
                            return MSG_EMPTY
                        }
                        return At(member)
                    }
                }
                "face" -> {
                    return Face(args["id"]!!.toInt())
                }
                "emoji" -> {
                    return PlainText(String(Character.toChars(args["id"]!!.toInt())))
                }
                "image" -> {
                    var image: Image? = null
                    if (args.containsKey("file")) {
                        if (args["file"]!!.endsWith(".mnimg")) {
                            image = Image(args["file"]!!.replace(".mnimg", ""))
                        }
                        val file = MiraiNative.getDataFile("image", args["file"]!!)
                        if (file != null) {
                            image = contact!!.uploadImage(file)
                        }
                    } else if (args.containsKey("url")) {
                        image = withContext(Dispatchers.IO) { contact!!.uploadImage(URL(args["url"]!!)) }
                    }
                    if (image != null) {
                        if (args["type"] == "flash") {
                            return image.flash()
                        }
                        return image
                    }
                }
                "share" -> {
                    return RichMessageHelper.share(
                        args["url"]!!,
                        args["title"],
                        args["content"],
                        args["image"]
                    )
                }
                "contact" -> {
                    return if (args["type"] == "qq") {
                        RichMessageHelper.contactQQ(args["id"]!!.toLong())
                    } else {
                        RichMessageHelper.contactGroup(args["id"]!!.toLong())
                    }
                }
                "music" -> {
                    when (args["type"]) {
                        "qq" -> return QQMusic.send(args["id"]!!)
                        "163" -> return NeteaseMusic.send(args["id"]!!)
                        "custom" -> return Music.custom(
                            args["url"]!!,
                            args["audio"]!!,
                            args["title"]!!,
                            args["content"],
                            args["image"]
                        )
                    }
                }
                "shake" -> {
                    return PokeMessage.Poke
                }
                "poke" -> {
                    PokeMessage.values.forEach {
                        if (it.type == args["type"]!!.toInt() && it.id == args["id"]!!.toInt()) {
                            return it
                        }
                    }
                    return MSG_EMPTY
                }
                "xml" -> {
                    return XmlMessage(args["data"]!!)
                }
                "json" -> {
                    return JsonMessage(args["data"]!!)
                }
                else -> {
                    MiraiNative.logger.debug("不支持的 CQ码：${parts[0]}")
                }
            }
            return MSG_EMPTY
        }
        return PlainText(unescape(false))
    }

    fun chainToCode(chain: MessageChain): String {
        return chain.joinToString(separator = "") {
            when (it) {
                is At -> "[CQ:at,qq=${it.target}]"
                is AtAll -> "[CQ:at,qq=all]"
                is PlainText -> it.content.escape(false)
                is Face -> "[CQ:face,id=${it.id}]"
                is VipFace -> "[CQ:vipface,id=${it.kind.id},name=${it.kind.name},count=${it.count}]"
                is Image -> "[CQ:image,file=${it.imageId}.mnimg]" // Real file not supported
                is RichMessage -> "[CQ:rich,data=${it.content.escape(true)}]"
                is Voice -> "[CQ:voice,url=${it.url},md5=${it.md5},file=${it.fileName}]"
                is PokeMessage -> "[CQ:poke,id=${it.id},type=${it.type},name=${it.name}]"
                is FlashImage -> "[CQ:image,file=${it.image.imageId}.mning,type=flash]"
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
                            MiraiNative.logger.error("CQ消息解析失败：$message，索引：$index")
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
                            MiraiNative.logger.error("CQ消息解析失败：$message，索引：$index")
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
                +PlainText(message.unescape(false))
            }
        }
    }
}
