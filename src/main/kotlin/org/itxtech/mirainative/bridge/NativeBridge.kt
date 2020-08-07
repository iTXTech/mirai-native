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

package org.itxtech.mirainative.bridge

import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.plugin.Event
import org.itxtech.mirainative.plugin.NativePlugin

object NativeBridge {
    private fun getPlugins() = PluginManager.plugins

    private fun getLogger() = MiraiNative.logger

    fun loadPlugin(plugin: NativePlugin): Int {
        val code = Bridge.loadNativePlugin(
            plugin.file.absolutePath.replace("\\", "\\\\"),
            plugin.id
        )
        if (plugin.pluginInfo != null) {
            getLogger().info("Native Plugin (w json) ${plugin.pluginInfo!!.name} has been loaded with code $code")
        } else {
            getLogger().info("Native Plugin (w/o json) ${plugin.file.name} has been loaded with code $code")
        }
        return code
    }

    fun unloadPlugin(plugin: NativePlugin) = Bridge.freeNativePlugin(plugin.id).apply {
        getLogger().info("Native Plugin ${plugin.id} has been unloaded with code $this")
    }

    fun disablePlugin(plugin: NativePlugin) {
        if (plugin.loaded && plugin.enabled) {
            if (plugin.shouldCallEvent(Event.EVENT_DISABLE, true)) {
                Bridge.callIntMethod(
                    plugin.id,
                    plugin.getEventOrDefault(Event.EVENT_DISABLE, "_eventDisable")
                )
            }
        }
    }

    fun enablePlugin(plugin: NativePlugin) {
        if (plugin.loaded && !plugin.enabled) {
            if (plugin.shouldCallEvent(Event.EVENT_ENABLE, true)) {
                Bridge.callIntMethod(
                    plugin.id,
                    plugin.getEventOrDefault(Event.EVENT_ENABLE, "_eventEnable")
                )
            }
        }
    }

    fun startPlugin(plugin: NativePlugin) {
        if (plugin.shouldCallEvent(Event.EVENT_STARTUP, true)) {
            Bridge.callIntMethod(
                plugin.id,
                plugin.getEventOrDefault(Event.EVENT_STARTUP, "_eventStartup")
            )
        }
    }

    fun exitPlugin(plugin: NativePlugin) {
        if (plugin.shouldCallEvent(Event.EVENT_EXIT, true)) {
            Bridge.callIntMethod(
                plugin.id,
                plugin.getEventOrDefault(Event.EVENT_EXIT, "_eventExit")
            )
        }
    }

    fun updateInfo(plugin: NativePlugin) {
        val info = Bridge.callStringMethod(plugin.id, "AppInfo")
        if ("" != info) {
            plugin.setInfo(info)
        }
    }

    // Events
    fun eventPrivateMessage(
        subType: Int,
        msgId: Int,
        fromAccount: Long,
        msg: String,
        font: Int
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_PRI_MSG) && Bridge.pEvPrivateMessage(
                    plugin.id,
                    plugin.getEventOrDefault(Event.EVENT_PRI_MSG, "_eventPrivateMsg"),
                    subType,
                    msgId,
                    fromAccount,
                    plugin.processMessage(Event.EVENT_PRI_MSG, msg),
                    font
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventGroupMessage(
        subType: Int,
        msgId: Int,
        fromGroup: Long,
        fromAccount: Long,
        fromAnonymous: String,
        msg: String,
        font: Int
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MSG) && Bridge.pEvGroupMessage(
                    plugin.id,
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MSG, "_eventGroupMsg"),
                    subType,
                    msgId,
                    fromGroup,
                    fromAccount,
                    fromAnonymous,
                    plugin.processMessage(Event.EVENT_GROUP_MSG, msg),
                    font
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventGroupAdmin(subType: Int, time: Int, fromGroup: Long, beingOperateAccount: Long) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_ADMIN) && Bridge.pEvGroupAdmin(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_GROUP_ADMIN,
                        "_eventSystem_GroupAdmin"
                    ),
                    subType, time, fromGroup, beingOperateAccount
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventGroupMemberLeave(
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MEMBER_DEC) && Bridge.pEvGroupMember(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_GROUP_MEMBER_DEC,
                        "_eventSystem_GroupMemberDecrease"
                    ),
                    subType, time, fromGroup, fromAccount, beingOperateAccount
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventGroupBan(
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long,
        duration: Long
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_BAN) && Bridge.pEvGroupBan(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_GROUP_BAN,
                        "_eventSystem_GroupBan"
                    ),
                    subType, time, fromGroup, fromAccount, beingOperateAccount, duration
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventGroupMemberJoin(
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MEMBER_INC) && Bridge.pEvGroupMember(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_GROUP_MEMBER_INC,
                        "_eventSystem_GroupMemberIncrease"
                    ),
                    subType, time, fromGroup, fromAccount, beingOperateAccount
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventRequestAddGroup(
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        msg: String,
        flag: String
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_REQUEST_GROUP) && Bridge.pEvRequestAddGroup(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_REQUEST_GROUP,
                        "_eventRequest_AddGroup"
                    ),
                    subType, time, fromGroup, fromAccount, msg, flag
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventRequestAddFriend(
        subType: Int,
        time: Int,
        fromAccount: Long,
        msg: String,
        flag: String
    ) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_REQUEST_FRIEND) && Bridge.pEvRequestAddFriend(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_REQUEST_FRIEND,
                        "_eventRequest_AddFriend"
                    ),
                    subType, time, fromAccount, msg, flag
                ) == 1
            ) {
                break
            }
        }
    }

    fun eventFriendAdd(subType: Int, time: Int, fromAccount: Long) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(Event.EVENT_FRIEND_ADD) && Bridge.pEvFriendAdd(
                    plugin.id,
                    plugin.getEventOrDefault(
                        Event.EVENT_FRIEND_ADD,
                        "_eventRequest_AddFriend"
                    ),
                    subType, time, fromAccount
                ) == 1
            ) {
                break
            }
        }
    }
}
