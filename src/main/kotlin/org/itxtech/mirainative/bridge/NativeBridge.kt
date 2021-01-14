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

package org.itxtech.mirainative.bridge

import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.fromNative
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.plugin.Event
import org.itxtech.mirainative.plugin.NativePlugin
import org.itxtech.mirainative.toNative
import org.itxtech.mirainative.util.ConfigMan
import java.io.File

object NativeBridge {
    private fun getPlugins() = PluginManager.plugins

    private fun getLogger() = MiraiNative.logger

    fun getPluginInfo(plugin: NativePlugin) = Bridge.callStringMethod(plugin.id, "pluginInfo".toNative()).fromNative()

    fun loadPlugin(plugin: NativePlugin, file: File): Int {
        val code = Bridge.loadNativePlugin(
            file.absolutePath.replace("\\", "\\\\").toNative(),
            plugin.id
        )
        val info = "插件 ${plugin.file.name} 已被加载，返回值为 $code 。"
        if (code == 0) {
            getLogger().info(info)
        } else {
            getLogger().error(info)
        }
        Bridge.syncWorkingDir()
        return code
    }

    fun unloadPlugin(plugin: NativePlugin) = Bridge.freeNativePlugin(plugin.id).apply {
        getLogger().info("插件 ${plugin.detailedIdentifier} 已被卸载，返回值为 $this 。")
    }

    fun disablePlugin(plugin: NativePlugin) {
        if (plugin.loaded && plugin.enabled) {
            singleEvent(plugin, Event.EVENT_DISABLE, true, "_eventDisable")
        }
    }

    fun enablePlugin(plugin: NativePlugin) {
        if (plugin.loaded && !plugin.enabled) {
            singleEvent(plugin, Event.EVENT_ENABLE, true, "_eventEnable")
        }
    }

    fun startPlugin(plugin: NativePlugin) {
        singleEvent(plugin, Event.EVENT_STARTUP, true, "_eventStartup")
    }

    fun exitPlugin(plugin: NativePlugin) {
        singleEvent(plugin, Event.EVENT_EXIT, true, "_eventExit")
    }

    fun updateInfo(plugin: NativePlugin) {
        if (ConfigMan.config.verboseNativeApiLog) {
            getLogger().verbose("正在调用插件 ${plugin.detailedIdentifier} 的 AppInfo 方法。")
        }
        val info = Bridge.callStringMethod(plugin.id, "AppInfo".toNative()).fromNative()
        if ("" != info) {
            plugin.setInfo(info)
        }
    }

    // Events

    private inline fun singleEvent(
        plugin: NativePlugin,
        ev: Int,
        ignoreState: Boolean,
        defaultMethod: String,
        block: NativePlugin.(evName: ByteArray) -> Int = { Bridge.callIntMethod(plugin.id, it) }
    ): Boolean {
        val eventName = plugin.getEventOrDefault(ev, defaultMethod)
        return (plugin.shouldCallEvent(ev, ignoreState).apply {
            if (this && ConfigMan.config.verboseNativeApiLog) {
                getLogger().verbose("正在调用插件 ${plugin.detailedIdentifier} 的 $eventName 事件。")
            }
        } && block(plugin, eventName.toNative()) == 1)
    }

    private inline fun event(ev: Int, defaultMethod: String, block: NativePlugin.(evName: ByteArray) -> Int) {
        for (plugin in getPlugins().values) {
            if (singleEvent(plugin, ev, false, defaultMethod, block)) {
                break
            }
        }
    }

    fun eventPrivateMessage(
        subType: Int,
        msgId: Int,
        fromAccount: Long,
        msg: String,
        font: Int
    ) {
        event(Event.EVENT_PRI_MSG, "_eventPrivateMsg") {
            Bridge.pEvPrivateMessage(
                id,
                it,
                subType,
                msgId,
                fromAccount,
                processMessage(Event.EVENT_PRI_MSG, msg).toNative(),
                font
            )
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
        event(Event.EVENT_GROUP_MSG, "_eventGroupMsg") {
            Bridge.pEvGroupMessage(
                id,
                it,
                subType,
                msgId,
                fromGroup,
                fromAccount,
                fromAnonymous.toNative(),
                processMessage(Event.EVENT_GROUP_MSG, msg).toNative(),
                font
            )
        }
    }

    fun eventGroupAdmin(subType: Int, time: Int, fromGroup: Long, beingOperateAccount: Long) {
        event(Event.EVENT_GROUP_ADMIN, "_eventSystem_GroupAdmin") {
            Bridge.pEvGroupAdmin(id, it, subType, time, fromGroup, beingOperateAccount)
        }
    }

    fun eventGroupMemberLeave(subType: Int, time: Int, fromGroup: Long, fromAccount: Long, beingOperateAccount: Long) {
        event(Event.EVENT_GROUP_MEMBER_DEC, "_eventSystem_GroupMemberDecrease") {
            Bridge.pEvGroupMember(id, it, subType, time, fromGroup, fromAccount, beingOperateAccount)
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
        event(Event.EVENT_GROUP_BAN, "_eventSystem_GroupBan") {
            Bridge.pEvGroupBan(id, it, subType, time, fromGroup, fromAccount, beingOperateAccount, duration)
        }
    }

    fun eventGroupMemberJoin(
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long
    ) {
        event(Event.EVENT_GROUP_MEMBER_INC, "_eventSystem_GroupMemberIncrease") {
            Bridge.pEvGroupMember(id, it, subType, time, fromGroup, fromAccount, beingOperateAccount)
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
        event(Event.EVENT_REQUEST_GROUP, "_eventRequest_AddGroup") {
            Bridge.pEvRequestAddGroup(id, it, subType, time, fromGroup, fromAccount, msg.toNative(), flag.toNative())
        }
    }

    fun eventRequestAddFriend(
        subType: Int,
        time: Int,
        fromAccount: Long,
        msg: String,
        flag: String
    ) {
        event(Event.EVENT_REQUEST_FRIEND, "_eventRequest_AddFriend") {
            Bridge.pEvRequestAddFriend(id, it, subType, time, fromAccount, msg.toNative(), flag.toNative())
        }
    }

    fun eventFriendAdd(subType: Int, time: Int, fromAccount: Long) {
        event(Event.EVENT_FRIEND_ADD, "_eventRequest_AddFriend") {
            Bridge.pEvFriendAdd(id, it, subType, time, fromAccount)
        }
    }
}
