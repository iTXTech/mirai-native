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
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.recall
import org.itxtech.mirainative.MiraiNative

object CacheManager {
    private val msgCache: HashMap<Int, MessageSource> = HashMap()
    private val evCache: HashMap<Int, BotEvent> = HashMap()
    private val internalId = atomic(0)

    fun nextId(): Int = internalId.getAndIncrement()

    fun cacheEvent(event: BotEvent, id: Int = nextId()): String {
        evCache[id] = event
        return id.toString()
    }

    fun getEvent(id: String): BotEvent? {
        return evCache[id.toInt()]?.also { evCache.remove(id.toInt()) }
    }

    fun cacheMessage(message: MessageSource, id: Int = nextId()): Int {
        msgCache[id] = message
        return id
    }

    fun recall(id: Int): Boolean {
        val message = msgCache[id] ?: return false
        msgCache.remove(id)
        MiraiNative.launch {
            message.recall()
        }
        return true
    }

    fun getMessage(id: Int): MessageSource? {
        return msgCache[id]
    }
}
