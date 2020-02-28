package org.itxtech.mirainative

import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.messageRandom
import net.mamoe.mirai.message.data.sequenceId

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
object MessageCache {
    private val cache: HashMap<Int, CachedMessage> = HashMap()

    fun cacheMessage(message: MessageSource) {
        if (message.groupId == 0L) {
            cache[message.messageRandom] = CachedMessage(message.sequenceId, message.id, true)
        } else {
            cache[message.messageRandom] = CachedMessage(message.sequenceId, message.groupId, false)
        }
    }

    fun recall(id: Int): Boolean {
        if (cache.containsKey(id)) {
            //TODO
            return true
        }
        return false
    }
}

data class CachedMessage(val seq: Int, val id: Long, val friend: Boolean)
