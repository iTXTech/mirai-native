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
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.ConfigMan

object EventManager {
    fun registerEvents() {
        MiraiNative.subscribeAlways<BotOnlineEvent> {
            MiraiNative.botOnline = true
            MiraiNative.nativeLaunch {
                ConfigMan.init()
                MiraiNative.logger.info("Mirai Native 正启用所有插件。")
                PluginManager.enablePlugins()
                Tray.update()
            }
        }

        // 消息事件
        MiraiNative.subscribeAlways<FriendMessageEvent> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventPrivateMessage(
                    Bridge.PRI_MSG_SUBTYPE_FRIEND,
                    CacheManager.cacheMessage(message.source),
                    sender.id,
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }
        MiraiNative.subscribeAlways<GroupMessageEvent> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMessage(
                    1,
                    CacheManager.cacheMessage(message.source),
                    group.id,
                    sender.id,
                    "",
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }
        MiraiNative.subscribeAlways<TempMessageEvent> { msg ->
            MiraiNative.nativeLaunch {
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
        MiraiNative.subscribeAlways<MemberPermissionChangeEvent> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupAdmin(
                    if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                    getTimestamp(), group.id, member.id
                )
            }
        }
        MiraiNative.subscribeAlways<BotGroupPermissionChangeEvent> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupAdmin(
                    if (new == MemberPermission.MEMBER) Bridge.PERM_SUBTYPE_CANCEL_ADMIN else Bridge.PERM_SUBTYPE_SET_ADMIN,
                    getTimestamp(), group.id, bot.id
                )
            }
        }

        // 加群事件
        MiraiNative.subscribeAlways<MemberJoinEvent> { ev ->
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMemberJoin(
                    if (ev is MemberJoinEvent.Invite) Bridge.MEMBER_JOIN_PERMITTED else Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                    getTimestamp(), group.id, 0, member.id
                )
            }
        }
        MiraiNative.subscribeAlways<MemberJoinRequestEvent> { ev ->
            MiraiNative.nativeLaunch {
                NativeBridge.eventRequestAddGroup(
                    Bridge.REQUEST_GROUP_APPLY,
                    getTimestamp(), groupId, fromId, message, CacheManager.cacheEvent(ev)
                )
            }
        }
        MiraiNative.subscribeAlways<BotInvitedJoinGroupRequestEvent> { ev ->
            MiraiNative.nativeLaunch {
                NativeBridge.eventRequestAddGroup(
                    Bridge.REQUEST_GROUP_INVITED,
                    getTimestamp(), groupId, invitorId, "", CacheManager.cacheEvent(ev)
                )
            }
        }

        //加好友事件
        MiraiNative.subscribeAlways<NewFriendRequestEvent> { ev ->
            MiraiNative.nativeLaunch {
                NativeBridge.eventRequestAddFriend(1, getTimestamp(), fromId, message, CacheManager.cacheEvent(ev))
            }
        }
        MiraiNative.subscribeAlways<FriendAddEvent> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventFriendAdd(1, getTimestamp(), friend.id)
            }
        }

        // 退群事件
        MiraiNative.subscribeAlways<MemberLeaveEvent.Kick> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMemberLeave(
                    Bridge.MEMBER_LEAVE_KICK,
                    getTimestamp(), group.id, operator?.id ?: bot.id, member.id
                )
            }
        }
        MiraiNative.subscribeAlways<MemberLeaveEvent.Quit> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMemberLeave(
                    Bridge.MEMBER_LEAVE_QUIT,
                    getTimestamp(), group.id, 0, member.id
                )
            }
        }
        MiraiNative.subscribeAlways<BotLeaveEvent.Active> {
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMemberLeave(
                    Bridge.MEMBER_LEAVE_QUIT,
                    getTimestamp(), group.id, 0, bot.id
                )
            }
        }
        MiraiNative.subscribeAlways<BotLeaveEvent.Kick> { ev ->
            MiraiNative.nativeLaunch {
                NativeBridge.eventGroupMemberLeave(
                    Bridge.MEMBER_LEAVE_KICK,
                    getTimestamp(), group.id, ev.operator.id, bot.id
                )
            }
        }

        // 禁言事件
        MiraiNative.subscribeAlways<MemberMuteEvent> {
            MiraiNative.nativeLaunch {
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
        MiraiNative.subscribeAlways<MemberUnmuteEvent> {
            MiraiNative.nativeLaunch {
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
        MiraiNative.subscribeAlways<BotMuteEvent> {
            MiraiNative.nativeLaunch {
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
        MiraiNative.subscribeAlways<BotUnmuteEvent> {
            MiraiNative.nativeLaunch {
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
        MiraiNative.subscribeAlways<GroupMuteAllEvent> {
            MiraiNative.nativeLaunch {
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

    fun getTimestamp() = currentTimeSeconds.toInt()
}
