package org.itxtech.mirainative

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.sequenceId
import net.mamoe.mirai.utils.currentTimeSeconds
import org.itxtech.mirainative.plugin.NativePlugin
import org.itxtech.mirainative.plugin.PluginInfo
import java.io.File

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
class MiraiNative : PluginBase() {
    companion object {
        @Suppress("ObjectPropertyName")
        internal lateinit var _instance: MiraiNative
    }

    private var pluginId: Int = 0
    private var bridge: Bridge = Bridge()

    var plugins: HashMap<Int, NativePlugin> = HashMap()

    val bot: Bot by lazy { Bot.instances.first().get()!! }

    @UnstableDefault
    override fun onLoad() {
        _instance = this

        val dll = dataFolder.absolutePath + File.separatorChar + "CQP.dll"
        logger.info("Mirai Native 正在加载 $dll")
        System.load(dll)

        if (!dataFolder.isDirectory) {
            logger.error("数据文件夹不是一个文件夹！" + dataFolder.absolutePath)
        } else {
            dataFolder.listFiles()?.forEach { file ->
                if (file.isFile && file.absolutePath.endsWith("dll") && !file.absolutePath.endsWith("CQP.dll")) {
                    val plugin = NativePlugin(file, pluginId)
                    plugins[pluginId++] = plugin
                    val json = File(file.parent + File.separatorChar + file.name.replace(".dll", ".json"))
                    if (json.exists()) {
                        plugin.pluginInfo = Json.nonstrict.parse(PluginInfo.serializer(), json.readText())
                    }
                    bridge.loadPlugin(plugin)
                }
            }
            bridge.eventStartup()
            logger.info("Mirai Native 已调用 Startup 事件")
        }
    }

    override fun onEnable() {
        logger.info("Mirai Native 正启用所有 DLL 插件。")

        registerCommands()
        registerEvents()
    }

    private fun registerCommands() {
        registerCommand {
            name = "npm"
            description = "Mirai Native 插件管理器"
            usage = "npm [list|enable|disable|menu|info] (插件 Id) (方法名)"
            onCommand {
                if ((it.isEmpty() || (it[0] != "list" && it.size < 2))) {
                    return@onCommand false
                }
                when (it[0]) {
                    "list" -> {
                        appendMessage("共加载了 " + plugins.size + " 个 Mirai Native 插件")
                        plugins.values.forEach { p ->
                            if (p.pluginInfo != null) {
                                appendMessage(
                                    "Id：" + p.id + " 标识符：" + p.identifier + " 名称：" + p.pluginInfo!!.name +
                                            " 版本：" + p.pluginInfo!!.version
                                )
                            } else {
                                appendMessage("Id：" + p.id + " 标识符：" + p.identifier + " （JSON文件缺失）")
                            }
                        }
                    }
                    "enable" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            val p = plugins[it[1].toInt()]!!
                            if (!p.enabled) {
                                bridge.enablePlugin(p)
                            }
                            appendMessage("插件 " + p.identifier + " 已启用。")
                        } else {
                            appendMessage("Id " + it[1] + " 不存在。")
                        }
                    }
                    "disable" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            val p = plugins[it[1].toInt()]!!
                            if (p.enabled) {
                                bridge.disablePlugin(p)
                            }
                            appendMessage("插件 " + p.identifier + " 已禁用。")
                        } else {
                            appendMessage("Id " + it[1] + " 不存在。")
                        }
                    }
                    "menu" -> {
                        if (it.size < 3) {
                            return@onCommand false
                        }
                        if (plugins.containsKey(it[1].toInt())) {
                            bridge.callIntMethod(it[1].toInt(), it[2])
                            appendMessage("已调用 Id " + it[1] + " 的 " + it[2] + " 方法。")
                        } else {
                            appendMessage("Id " + it[2] + " 不存在。")
                        }
                    }
                    "info" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            val p = plugins[it[1].toInt()]!!
                            val i = p.pluginInfo
                            if (i == null) {
                                appendMessage("Id：" + p.id + " （JSON文件缺失）")
                                appendMessage("标识符：" + p.identifier)
                                appendMessage("CQ API：" + p.api)
                            } else {
                                appendMessage("Id：" + p.id)
                                appendMessage("标识符：" + p.identifier)
                                appendMessage("CQ API：" + p.api + " CQ API（JSON）：" + i.apiver)
                                appendMessage("名称：" + i.name)
                                appendMessage("版本：" + i.version + " 版本号：" + i.version_id)
                                appendMessage("描述：" + i.description)
                                appendMessage("作者：" + i.author)
                                appendMessage("注册了 " + i.event.size + " 个事件")
                                i.event.forEach { ev ->
                                    appendMessage("类型：" + ev.type + " 描述：" + ev.name + " 方法名：" + ev.function)
                                }
                                appendMessage("注册了 " + i.status.size + " 个悬浮窗项目")
                                i.status.forEach { s ->
                                    appendMessage("名称：" + s.name + " 标题：" + s.title + " 方法名：" + s.function)
                                }
                                appendMessage("注册了 " + i.menu.size + " 个菜单入口")
                                i.menu.forEach { m ->
                                    appendMessage("名称：" + m.name + " 方法名：" + m.function)
                                }
                            }
                        } else {
                            appendMessage("Id " + it[1] + " 不存在。")
                        }
                    }
                    else -> {
                        return@onCommand false
                    }
                }
                true
            }
        }
    }

    private fun registerEvents() {
        subscribeAlways<BotOnlineEvent> {
            bridge.eventEnable()
        }

        // 消息事件
        subscribeAlways<FriendMessage> {
            bridge.eventPrivateMessage(
                Bridge.PRI_MSG_SUBTYPE_FRIEND,
                message.sequenceId,
                sender.id,
                message.toString(),
                0
            )
        }
        subscribeAlways<GroupMessage> {
            bridge.eventGroupMessage(1, message.sequenceId, group.id, sender.id, "", message.toString(), 0)
        }

        // 权限事件
        subscribeAlways<MemberPermissionChangeEvent> {
            if (new == MemberPermission.MEMBER) {
                bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_CANCEL_ADMIN, getTimestamp(), group.id, member.id)
            } else {
                bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_SET_ADMIN, getTimestamp(), group.id, member.id)
            }
        }

        // 退群事件
        subscribeAlways<MemberLeaveEvent.Kick> {
            val op = operator?.id ?: bot.uin
            bridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_KICK, getTimestamp(), group.id, op, member.id)
        }
        subscribeAlways<MemberLeaveEvent.Quit> {
            bridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_QUIT, getTimestamp(), group.id, 0, member.id)
        }

        // 禁言事件
        subscribeAlways<MemberMuteEvent> {
            val op = operator?.id ?: bot.uin
            bridge.eventGroupBan(Bridge.GROUP_MUTE, getTimestamp(), group.id, op, member.id, durationSeconds.toLong())
        }
        subscribeAlways<MemberUnmuteEvent> {
            val op = operator?.id ?: bot.uin
            bridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, member.id, 0)
        }
        subscribeAlways<BotMuteEvent> {
            bridge.eventGroupBan(
                Bridge.GROUP_MUTE,
                getTimestamp(),
                group.id,
                operator.id,
                bot.uin,
                durationSeconds.toLong()
            )
        }
        subscribeAlways<BotUnmuteEvent> {
            bridge.eventGroupBan(
                Bridge.GROUP_UNMUTE,
                getTimestamp(),
                group.id,
                operator.id,
                bot.uin,
                0
            )
        }
        subscribeAlways<GroupMuteAllEvent> {
            val op = operator?.id ?: bot.uin
            if (new) {
                bridge.eventGroupBan(Bridge.GROUP_MUTE, getTimestamp(), group.id, op, 0, 0)
            } else {
                bridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, 0, 0)
            }
        }
    }

    private fun getTimestamp(): Int {
        return currentTimeSeconds.toInt()
    }

    override fun onDisable() {
        logger.info("Mirai Native 正停用所有DLL插件。")
        bridge.eventDisable()

        logger.info("Mirai Native 正调用 Exit 事件")
        bridge.eventExit()
    }
}
