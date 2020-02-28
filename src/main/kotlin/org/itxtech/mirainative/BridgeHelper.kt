package org.itxtech.mirainative

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.utils.MiraiExperimentalAPI

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
object BridgeHelper {
    @JvmStatic
    fun sendFriendMessage(id: Long, message: String): MessageReceipt<QQ> = runBlocking {
        MiraiNative.INSTANCE.bot.getFriend(id).sendMessage(message)
    }

    @JvmStatic
    fun sendGroupMessage(id: Long, message: String): MessageReceipt<Group> = runBlocking {
        MiraiNative.INSTANCE.bot.getGroup(id).sendMessage(message)
    }

    @JvmStatic
    fun setGroupBan(groupId: Long, memberId: Long, duration: Int) = runBlocking {
        if (duration == 0) {
            MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].unmute()
        } else {
            MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].mute(duration)
        }
    }

    @JvmStatic
    fun setGroupKick(groupId: Long, memberId: Long) = runBlocking {
        MiraiNative.INSTANCE.bot.getGroup(groupId)[memberId].kick()
    }

    @MiraiExperimentalAPI
    @JvmStatic
    fun setGroupLeave(groupId: Long) = runBlocking {
        MiraiNative.INSTANCE.bot.getGroup(groupId).quit()
    }
}
