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

import io.ktor.util.InternalAPI
import io.ktor.util.encodeBase64
import kotlinx.coroutines.launch
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.nio.charset.Charset

object BridgeHelper {
    @JvmStatic
    fun sendFriendMessage(id: Long, message: String): Int {
        val internalId = MessageCache.nextId()
        MiraiNative.INSTANCE.launch {
            MiraiNative.INSTANCE.bot.getFriend(id).sendMessage(message).apply {
                source.ensureSequenceIdAvailable()
                MessageCache.cacheMessage(source, internalId)
            }
        }
        return internalId
    }

    @JvmStatic
    fun sendGroupMessage(id: Long, message: String): Int {
        val internalId = MessageCache.nextId()
        MiraiNative.INSTANCE.launch {
            MiraiNative.INSTANCE.bot.getGroup(id).sendMessage(message).apply {
                source.ensureSequenceIdAvailable()
                MessageCache.cacheMessage(source, internalId)
            }
        }
        return internalId
    }

    @JvmStatic
    fun setGroupBan(groupId: Long, memberId: Long, duration: Int) {
        MiraiNative.INSTANCE.launch {
            if (duration == 0) {
                MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].unmute()
            } else {
                MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].mute(duration)
            }
        }
    }

    @JvmStatic
    fun setGroupKick(groupId: Long, memberId: Long) {
        MiraiNative.INSTANCE.launch {
            MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].kick()
        }
    }

    @MiraiExperimentalAPI
    @JvmStatic
    fun setGroupLeave(groupId: Long) {
        MiraiNative.INSTANCE.launch {
            MiraiNative.INSTANCE.bot.getGroup(groupId).quit()
        }
    }

    private inline fun BytePacketBuilder.writeShortLVPacket(
        lengthOffset: ((Long) -> Long) = { it },
        builder: BytePacketBuilder.() -> Unit
    ): Int =
        BytePacketBuilder().apply(builder).build().use {
            val length = lengthOffset.invoke(it.remaining)
            writeShort(length.toShort())
            writePacket(it)
            return length.toInt()
        }


    private fun BytePacketBuilder.writeString(string: String) {
        val b = string.toByteArray(Charset.forName("GB18030"))
        writeShort(b.size.toShort())
        writeFully(b)
    }

    @InternalAPI
    @JvmStatic
    fun getFriendList(): String {
        val list = MiraiNative.INSTANCE.bot.friends
        return buildPacket {
            writeInt(list.size)
            list.forEach { qq ->
                writeShortLVPacket {
                    writeLong(qq.id)
                    writeString(qq.nick)
                    //TODO: 备注
                    writeString("")
                }
            }
        }.readBytes().encodeBase64()
    }

    @InternalAPI
    @JvmStatic
    fun getGroupInfo(id: Long): String {
        val info = MiraiNative.INSTANCE.bot.getGroupOrNull(id)
        if (info != null) {
            return buildPacket {
                writeLong(id)
                writeString(info.name)
                writeInt(info.members.size + 1)
                //TODO: 上限
                writeInt(1000)
            }.readBytes().encodeBase64()
        }
        return ""
    }

    @InternalAPI
    @JvmStatic
    fun getGroupList(): String {
        val list = MiraiNative.INSTANCE.bot.groups
        return buildPacket {
            writeInt(list.size)
            list.forEach {
                writeShortLVPacket {
                    writeLong(it.id)
                    writeString(it.name)
                }
            }
        }.readBytes().encodeBase64()
    }
}
