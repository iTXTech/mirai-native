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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.message.data.MessageSource
import org.itxtech.mirainative.MiraiNative

object MessageCache {
    private val cache: HashMap<Int, MessageSource> = HashMap()
    private val internalId = atomic(0)

    fun nextId(): Int = internalId.getAndIncrement()

    fun cacheMessage(message: MessageSource, id: Int = nextId()): Int {
        if (message.groupId == 0L) {
            cache[id] = message
        } else {
            cache[id] = message
        }
        return id
    }

    @LowLevelAPI
    fun recall(id: Int): Boolean {
        val message = cache[id] ?: return false
        cache.remove(id)
        MiraiNative.INSTANCE.launch {
            if (message.groupId == 0L) {
                //MiraiNative.INSTANCE.bot._lowLevelRecallFriendMessage(message.id)
            } else {
                MiraiNative.INSTANCE.bot._lowLevelRecallGroupMessage(
                    groupId = message.id,
                    messageId = message.id
                )
            }
        }
        return true
    }

    fun getMessage(id: Int): MessageSource? {
        return cache[id]
    }
}
