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
import org.itxtech.mirainative.util.ConfigMan
import java.nio.charset.Charset

object NativeBridge {
    private fun getPlugins() = PluginManager.plugins

    private fun getLogger() = MiraiNative.logger

    fun String.toNative() = toByteArray(Charset.forName("GB18030"))

    fun ByteArray.fromNative() = String(this, Charset.forName("GB18030"))

    fun init() {
        Bridge.config(ConfigMan.config.codePage)
    }

    fun getPluginInfo(plugin: NativePlugin) = Bridge.callStringMethod(plugin.id, "pluginInfo".toNative()).fromNative()

    fun loadPlugin(plugin: NativePlugin): Int {
        val code = Bridge.loadNativePlugin(
            plugin.file.absolutePath.replace("\\", "\\\\").toNative(),
            plugin.id
        )
        val info = "Native Plugin ${plugin.file.name} has been loaded with code $code"
        if (code == 0) {
            getLogger().info(info)
        } else {
            getLogger().error(info)
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
                    plugin.getEventOrDefault(Event.EVENT_DISABLE, "_eventDisable").toNative()
                )
            }
        }
    }

    fun enablePlugin(plugin: NativePlugin) {
        if (plugin.loaded && !plugin.enabled) {
            if (plugin.shouldCallEvent(Event.EVENT_ENABLE, true)) {
                Bridge.callIntMethod(
                    plugin.id,
                    plugin.getEventOrDefault(Event.EVENT_ENABLE, "_eventEnable").toNative()
                )
            }
        }
    }

    fun startPlugin(plugin: NativePlugin) {
        if (plugin.shouldCallEvent(Event.EVENT_STARTUP, true)) {
            Bridge.callIntMethod(
                plugin.id,
                plugin.getEventOrDefault(Event.EVENT_STARTUP, "_eventStartup").toNative()
            )
        }
    }

    fun exitPlugin(plugin: NativePlugin) {
        if (plugin.shouldCallEvent(Event.EVENT_EXIT, true)) {
            Bridge.callIntMethod(
                plugin.id,
                plugin.getEventOrDefault(Event.EVENT_EXIT, "_eventExit").toNative()
            )
        }
    }

    fun updateInfo(plugin: NativePlugin) {
        val info = Bridge.callStringMethod(plugin.id, "AppInfo".toNative()).fromNative()
        if ("" != info) {
            plugin.setInfo(info)
        }
    }

    // Events

    private inline fun event(ev: Int, defaultMethod: String, block: NativePlugin.(evName: ByteArray) -> Int) {
        for (plugin in getPlugins().values) {
            if (plugin.shouldCallEvent(ev) && block(
                    plugin,
                    plugin.getEventOrDefault(ev, defaultMethod).toNative()
                ) == 1
            ) {
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
                fromAnonymous,
                processMessage(Event.EVENT_GROUP_MSG, msg),
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
            Bridge.pEvRequestAddGroup(id, it, subType, time, fromGroup, fromAccount, msg, flag)
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
            Bridge.pEvRequestAddFriend(id, it, subType, time, fromAccount, msg, flag)
        }
    }

    fun eventFriendAdd(subType: Int, time: Int, fromAccount: Long) {
        event(Event.EVENT_FRIEND_ADD, "_eventRequest_AddFriend") {
            Bridge.pEvFriendAdd(id, it, subType, time, fromAccount)
        }
    }
}
