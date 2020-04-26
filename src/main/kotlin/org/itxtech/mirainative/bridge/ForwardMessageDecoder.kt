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

package org.itxtech.mirainative.bridge

import io.ktor.util.InternalAPI
import io.ktor.util.decodeBase64Bytes
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.getFriendOrNull
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.buildForwardMessage
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.MiraiBridge.readString
import org.itxtech.mirainative.message.ChainCodeConverter

@InternalAPI
object ForwardMessageDecoder {
    /**
     * 结构
     * Short => 消息条数
     * Entry => MessageEntry
     */
    fun decode(contact: Contact, strategy: String, data: String): ForwardMessage {
        val pk = ByteReadPacket(data.decodeBase64Bytes())
        return runBlocking {
            return@runBlocking buildForwardMessage(contact, decodeStrategy(strategy)) {
                for (i in 1..pk.readShort()) {
                    MessageEntry(pk.readLong(), pk.readString(), pk.readInt(), pk.readString()).append(this, contact)
                }
            }
        }
    }

    /**
     * Strategy格式
     * Title, Brief, Source, Summary
     * Preview数目，Strings
     */
    private fun decodeStrategy(strategy: String): ForwardMessage.DisplayStrategy {
        if (strategy == "") {
            return ForwardMessage.DisplayStrategy
        }
        val pk = ByteReadPacket(strategy.decodeBase64Bytes())
        val title = pk.readString()
        val brief = pk.readString()
        val source = pk.readString()
        val summary = pk.readString()
        val pre = pk.readShort()
        val seq = arrayListOf<String>()
        for (i in 1..pre) {
            seq.add(pk.readString())
        }
        return ForwardMessage.DisplayStrategy(
            generateTitle = {
                if (title == "") ForwardMessage.DisplayStrategy.generateTitle(it) else title
            },
            generateBrief = {
                if (brief == "") ForwardMessage.DisplayStrategy.generateBrief(it) else brief
            },
            generateSource = {
                if (source == "") ForwardMessage.DisplayStrategy.generateSource(it) else source
            },
            generateSummary = {
                if (summary == "") ForwardMessage.DisplayStrategy.generateSummary(it) else summary
            },
            generatePreview = {
                if (pre == 0.toShort()) ForwardMessage.DisplayStrategy.generatePreview(it) else seq.asSequence()
            }
        )
    }
}

/**
 * 当sender=0时为bot
 * 当time=0时为当前时间
 */
data class MessageEntry(val sender: Long, val name: String, val time: Int, val msg: String) {
    suspend fun append(builder: ForwardMessageBuilder, contact: Contact) {
        val s = if (sender == 0L) MiraiNative.bot.id else sender
        builder.add(
            ForwardMessage.Node(
                s,
                if (time == 0) builder.currentTime++ else time,
                getName(name, s, contact),
                ChainCodeConverter.codeToChain(msg, contact)
            )
        )
    }

    private fun getName(name: String, sender: Long, contact: Contact): String {
        if (name != "") {
            return name
        }
        if (contact is Group && contact.contains(sender)) {
            return contact[sender].nameCardOrNick
        }
        if (sender == MiraiNative.bot.id) {
            return MiraiNative.bot.nick
        }
        val friend = MiraiNative.bot.getFriendOrNull(sender)
        if (friend != null) {
            return friend.nameCardOrNick
        }
        return sender.toString()
    }
}
