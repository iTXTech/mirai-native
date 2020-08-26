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

package org.itxtech.mirainative.manager

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.TempMessageEvent
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.utils.currentTimeSeconds
import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.NativeBridge
import org.itxtech.mirainative.message.ChainCodeConverter
import org.itxtech.mirainative.message.ChainCodeConverter.escape

object EventManager {
    fun registerEvents() {
        with(MiraiNative) {
            subscribeAlways<BotOnlineEvent> {
                setBotOnline()
            }

            // 消息事件
            subscribeAlways<FriendMessageEvent> {
                nativeLaunch {
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
                nativeLaunch {
                    NativeBridge.eventGroupMessage(
                        1,
                        CacheManager.cacheMessage(message.source, chain = message),
                        group.id,
                        sender.id,
                        if (sender.id == 80000000L) senderName else "",
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }
            subscribeAlways<TempMessageEvent> { msg ->
                nativeLaunch {
                    NativeBridge.eventPrivateMessage(
                        Bridge.PRI_MSG_SUBTYPE_GROUP,
                        CacheManager.cacheTempMessage(msg),
                        sender.id,
                        ChainCodeConverter.chainToCode(message),
                        0
                    )
                }
            }

            // 权限事件
            subscribeAlways<MemberPermissionChangeEvent> {
                nativeLaunch {
                    NativeBridge.eventGroupAdmin(
                        if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                        getTimestamp(), group.id, member.id
                    )
                }
            }
            subscribeAlways<BotGroupPermissionChangeEvent> {
                nativeLaunch {
                    NativeBridge.eventGroupAdmin(
                        if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                        getTimestamp(), group.id, bot.id
                    )
                }
            }

            // 加群事件
            subscribeAlways<MemberJoinEvent> { ev ->
                nativeLaunch {
                    NativeBridge.eventGroupMemberJoin(
                        if (ev is MemberJoinEvent.Invite) Bridge.MEMBER_JOIN_PERMITTED else Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                        getTimestamp(), group.id, 0, member.id
                    )
                }
            }
            subscribeAlways<MemberJoinRequestEvent> { ev ->
                nativeLaunch {
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
                nativeLaunch {
                    NativeBridge.eventRequestAddGroup(
                        Bridge.REQUEST_GROUP_INVITED,
                        getTimestamp(), groupId, invitorId, "", CacheManager.cacheEvent(ev)
                    )
                }
            }
            subscribeAlways<BotJoinGroupEvent.Invite> { ev ->
                NativeBridge.eventGroupMemberJoin(
                    Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                    getTimestamp(),
                    group.id,
                    ev.invitor.id,
                    bot.id
                )
            }
            subscribeAlways<BotJoinGroupEvent.Active> {
                NativeBridge.eventGroupMemberJoin(
                    Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                    getTimestamp(),
                    group.id,
                    0,
                    bot.id
                )
            }

            //加好友事件
            subscribeAlways<NewFriendRequestEvent> { ev ->
                nativeLaunch {
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
                nativeLaunch {
                    NativeBridge.eventFriendAdd(1, getTimestamp(), friend.id)
                }
            }

            // 退群事件
            subscribeAlways<MemberLeaveEvent.Kick> {
                nativeLaunch {
                    NativeBridge.eventGroupMemberLeave(
                        Bridge.MEMBER_LEAVE_KICK,
                        getTimestamp(), group.id, operator?.id ?: bot.id, member.id
                    )
                }
            }
            subscribeAlways<MemberLeaveEvent.Quit> {
                nativeLaunch {
                    NativeBridge.eventGroupMemberLeave(
                        Bridge.MEMBER_LEAVE_QUIT,
                        getTimestamp(), group.id, 0, member.id
                    )
                }
            }
            subscribeAlways<BotLeaveEvent.Active> {
                nativeLaunch {
                    NativeBridge.eventGroupMemberLeave(
                        Bridge.MEMBER_LEAVE_QUIT,
                        getTimestamp(), group.id, 0, bot.id
                    )
                }
            }
            subscribeAlways<BotLeaveEvent.Kick> { ev ->
                nativeLaunch {
                    NativeBridge.eventGroupMemberLeave(
                        Bridge.MEMBER_LEAVE_KICK,
                        getTimestamp(), group.id, ev.operator.id, bot.id
                    )
                }
            }

            // 禁言事件
            subscribeAlways<MemberMuteEvent> {
                nativeLaunch {
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
                nativeLaunch {
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
                nativeLaunch {
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
                nativeLaunch {
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
                nativeLaunch {
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

    fun getTimestamp() = currentTimeSeconds.toInt()
}
