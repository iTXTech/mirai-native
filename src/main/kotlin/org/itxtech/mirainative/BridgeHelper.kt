package org.itxtech.mirainative

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.MessageReceipt

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
    fun sendMessageToFriend(id: Long, message: String): MessageReceipt<QQ> = runBlocking {
        MiraiNative._instance.bot.getFriend(id).sendMessage(message)
    }

    @JvmStatic
    fun sendMessageToGroup(id: Long, message: String): MessageReceipt<Group> = runBlocking {
        MiraiNative._instance.bot.getGroup(id).sendMessage(message)
    }
}
