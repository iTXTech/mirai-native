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

import net.mamoe.mirai.contact.AnonymousMember
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.MiraiExperimentalApi
import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.MiraiNative.launchEvent
import org.itxtech.mirainative.MiraiNative.setBotOnline
import org.itxtech.mirainative.bridge.NativeBridge
import org.itxtech.mirainative.message.ChainCodeConverter
import org.itxtech.mirainative.message.ChainCodeConverter.escape

object EventManager {
    @OptIn(MiraiExperimentalApi::class)
    fun registerEvents() {
        with(MiraiNative.globalEventChannel()) {
            subscribeAlways<BotOnlineEvent> {
                setBotOnline()
            }

            subscribeAlways<BotReloginEvent> {
                setBotOnline()
            }

            // 消息事件
            subscribeAlways<FriendMessageEvent> {
                launchEvent {
                    NativeBridge.eventPrivateMessage(
                        Bridge.PRI_MSG_SUBTYPE_FRIEND,
                        CacheManager.cacheMessage(message.source, chain = message),
                        sender.id,
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }
            subscribeAlways<GroupMessageEvent> {
                if (sender is AnonymousMember) {
                    CacheManager.cacheAnonymousMember(this)
                }
                launchEvent {
                    NativeBridge.eventGroupMessage(
                        1,
                        CacheManager.cacheMessage(message.source, chain = message),
                        group.id,
                        sender.id,
                        if (sender is AnonymousMember) (sender as AnonymousMember).anonymousId else "",//可能不兼容某些插件
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }
            subscribeAlways<GroupTempMessageEvent> { msg ->
                launchEvent {
                    NativeBridge.eventPrivateMessage(
                        Bridge.PRI_MSG_SUBTYPE_GROUP,
                        CacheManager.cacheTempMessage(msg),
                        sender.id,
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }
            subscribeAlways<StrangerMessageEvent> {
                launchEvent {
                    NativeBridge.eventPrivateMessage(
                        Bridge.PRI_MSG_SUBTYPE_ONLINE_STATE,
                        CacheManager.cacheMessage(message.source, chain = message),
                        sender.id,
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }

            // 权限事件
            subscribeAlways<MemberPermissionChangeEvent> {
                launchEvent {
                    NativeBridge.eventGroupAdmin(
                        if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                        getTimestamp(), group.id, member.id
                    )
                }
            }
            subscribeAlways<BotGroupPermissionChangeEvent> {
                launchEvent {
                    NativeBridge.eventGroupAdmin(
                        if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                        getTimestamp(), group.id, bot.id
                    )
                }
            }

            // 加群事件
            subscribeAlways<MemberJoinEvent> { ev ->
                launchEvent {
                    NativeBridge.eventGroupMemberJoin(
                        if (ev is MemberJoinEvent.Invite) Bridge.MEMBER_JOIN_PERMITTED else Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                        getTimestamp(), group.id, if (ev is MemberJoinEvent.Invite) ev.invitor.id else 0, member.id
                    )
                }
            }
            subscribeAlways<MemberJoinRequestEvent> { ev ->
                launchEvent {
                    NativeBridge.eventRequestAddGroup(
                        Bridge.REQUEST_GROUP_APPLY,
                        getTimestamp(),
                        groupId,
                        fromId,
                        message.escape(false),
                        CacheManager.cacheEvent(ev)
                    )
                }
            }
            subscribeAlways<BotInvitedJoinGroupRequestEvent> { ev ->
                launchEvent {
                    NativeBridge.eventRequestAddGroup(
                        Bridge.REQUEST_GROUP_INVITED,
                        getTimestamp(), groupId, invitorId, "", CacheManager.cacheEvent(ev)
                    )
                }
            }
            subscribeAlways<BotJoinGroupEvent> {
                launchEvent {
                    NativeBridge.eventGroupMemberJoin(
                        Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                        getTimestamp(),
                        group.id,
                        if (this@subscribeAlways is BotJoinGroupEvent.Invite) invitor.id else 0,
                        bot.id
                    )
                }
            }

            //加好友事件
            subscribeAlways<NewFriendRequestEvent> { ev ->
                launchEvent {
                    NativeBridge.eventRequestAddFriend(
                        1,
                        getTimestamp(),
                        fromId,
                        message.escape(false),
                        CacheManager.cacheEvent(ev)
                    )
                }
            }
            subscribeAlways<FriendAddEvent> {
                launchEvent {
                    NativeBridge.eventFriendAdd(1, getTimestamp(), friend.id)
                }
            }

            // 退群事件
            subscribeAlways<MemberLeaveEvent> {
                CacheManager.cacheMember(member)
                launchEvent {
                    NativeBridge.eventGroupMemberLeave(
                        if (it is MemberLeaveEvent.Kick) Bridge.MEMBER_LEAVE_KICK else Bridge.MEMBER_LEAVE_QUIT,
                        getTimestamp(),
                        group.id,
                        if (it is MemberLeaveEvent.Kick) it.operator?.id ?: bot.id else 0,
                        member.id
                    )
                }
            }
            subscribeAlways<BotLeaveEvent> {
                launchEvent {
                    NativeBridge.eventGroupMemberLeave(
                        if (it is BotLeaveEvent.Kick) Bridge.MEMBER_LEAVE_KICK else Bridge.MEMBER_LEAVE_QUIT,
                        getTimestamp(), group.id, if (it is BotLeaveEvent.Kick) it.operator.id else 0, bot.id
                    )
                }
            }

            // 禁言事件
            subscribeAlways<MemberMuteEvent> {
                launchEvent {
                    NativeBridge.eventGroupBan(
                        Bridge.GROUP_MUTE,
                        getTimestamp(),
                        group.id,
                        operator?.id ?: bot.id,
                        member.id,
                        durationSeconds.toLong()
                    )
                }
            }
            subscribeAlways<MemberUnmuteEvent> {
                launchEvent {
                    NativeBridge.eventGroupBan(
                        Bridge.GROUP_UNMUTE,
                        getTimestamp(),
                        group.id,
                        operator?.id ?: bot.id,
                        member.id,
                        0
                    )
                }
            }
            subscribeAlways<BotMuteEvent> {
                launchEvent {
                    NativeBridge.eventGroupBan(
                        Bridge.GROUP_MUTE,
                        getTimestamp(),
                        group.id,
                        operator.id,
                        bot.id,
                        durationSeconds.toLong()
                    )
                }
            }
            subscribeAlways<BotUnmuteEvent> {
                launchEvent {
                    NativeBridge.eventGroupBan(
                        Bridge.GROUP_UNMUTE,
                        getTimestamp(),
                        group.id,
                        operator.id,
                        bot.id,
                        0
                    )
                }
            }
            subscribeAlways<GroupMuteAllEvent> {
                launchEvent {
                    NativeBridge.eventGroupBan(
                        if (new) Bridge.GROUP_MUTE else Bridge.GROUP_UNMUTE,
                        getTimestamp(),
                        group.id,
                        operator?.id ?: bot.id,
                        0,
                        0
                    )
                }
            }
        }
    }

    fun getTimestamp() = System.currentTimeMillis().toInt()
}
