/*
 *
 * Mirai Native
 *
 * Copyright (C) 2020-2021 iTX Technologies
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

package org.itxtech.mirainative.manager

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.itxtech.mirainative.MiraiNative

@OptIn(MiraiExperimentalApi::class)
object CacheManager {
    private val msgCache = hashMapOf<Int, MessageSource>()
    private val evCache = hashMapOf<Int, BotEvent>()
    private val senders = hashMapOf<Long, User>()
    private val anonymousMembers = hashMapOf<Long, HashMap<String, AnonymousMember>>()
    private val records = hashMapOf<String, Voice>()
    private val internalId = atomic(0)

    fun nextId() = internalId.getAndIncrement()

    fun cacheEvent(event: BotEvent, id: Int = nextId()) = id.apply { evCache[this] = event }.toString()

    fun getEvent(id: String) = evCache[id.toInt()]?.also { evCache.remove(id.toInt()) }

    fun cacheMessage(source: MessageSource, id: Int = nextId(), chain: MessageChain? = null): Int {
        msgCache[id] = source
        chain?.forEach {
            if (it is Voice) {
                records[it.fileName] = it
            }
        }
        return id
    }

    fun cacheMember(member: User) {
        senders[member.id] = member
    }

    fun cacheTempMessage(message: GroupTempMessageEvent, id: Int = nextId()): Int {
        cacheMember(message.sender)
        return cacheMessage(message.message.source, id, message.message)
    }

    fun cacheAnonymousMember(ev: GroupMessageEvent) {
        if (anonymousMembers[ev.group.id] == null) {
            anonymousMembers[ev.group.id] = hashMapOf()
        }
        val sender = ev.sender as AnonymousMember
        anonymousMembers[ev.group.id]!![sender.anonymousId] = sender
    }

    fun recall(id: Int): Boolean {
        val message = msgCache[id] ?: return false
        msgCache.remove(id)
        MiraiNative.launch {
            message.recall()
        }
        return true
    }

    fun getMessage(id: Int) = msgCache[id]

    fun getRecord(name: String) = records[name.replace(".mnrec", "")]

    fun findUser(id: Long): User? {
        var member = MiraiNative.bot.getFriend(id) ?: senders[id]
        if (member == null) {
            member = MiraiNative.bot.strangers[id]
        }
        if (member == null) {
            MiraiNative.bot.groups.forEach {
                if (it[id] != null) {
                    return it[id]
                }
            }
        }
        return member
    }

    fun findAnonymousMember(group: Long, id: String): AnonymousMember? = anonymousMembers[group]?.get(id)

    fun clear() {
        msgCache.clear()
        evCache.clear()
        senders.clear()
        anonymousMembers.clear()
    }
}
