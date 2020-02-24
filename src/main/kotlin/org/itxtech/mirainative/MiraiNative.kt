package org.itxtech.mirainative

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.sequenceId
import org.itxtech.mirainative.plugin.NativePlugin
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

    private var _plugins: HashMap<Int, NativePlugin> = HashMap()
    val plugins: HashMap<Int, NativePlugin> get() = _plugins

    val bot: Bot by lazy { Bot.instances.first().get()!! }

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
                    _plugins[pluginId++] = plugin
                    loadPlugin(plugin)
                }
            }
            bridge.eventStartup()
            logger.info("Mirai Native 已调用 Startup 事件")
        }
    }

    private fun loadPlugin(plugin: NativePlugin) {
        bridge.loadNativePlugin(plugin.file.absolutePath.replace("\\", "\\\\"), plugin.id)
    }

    fun enablePlugin(plugin: NativePlugin) {
        bridge.enablePlugin(plugin.id)
    }

    fun disablePlugin(plugin: NativePlugin) {
        bridge.disablePlugin(plugin.id)
    }

    override fun onEnable() {
        logger.info("Mirai Native 正启用所有DLL插件。")
        bridge.eventEnable() //加载所有DLL插件并触发事件

        subscribeAlways<BotOnlineEvent> {
            logger.info("Bot 已上线，监听事件。")
        }
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
    }

    override fun onDisable() {
        logger.info("Mirai Native 正停用所有DLL插件。")
        bridge.eventDisable()

        logger.info("Mirai Native 正调用 Exit 事件")
        bridge.eventExit()
    }
}
