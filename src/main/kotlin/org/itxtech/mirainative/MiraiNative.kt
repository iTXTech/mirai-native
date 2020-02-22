package org.itxtech.mirainative

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.messageUid
import net.mamoe.mirai.message.data.sequenceId
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.io.File
import kotlin.concurrent.thread

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
    private var bridge: Bridge? = null

    override fun onLoad() {
        bridge = Bridge(File(".").absolutePath)
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            bridge?.eventExit()
        })

        logger.info("Mirai Native 正在加载 CQP.dll。")
        System.loadLibrary("CQP")
        bridge?.loadDynamicLibraries() //扫描目录下所有DLL并加载
        bridge?.eventStartup()
    }

    @ExperimentalCoroutinesApi
    @MiraiExperimentalAPI
    override fun onEnable() {
        logger.info("Mirai Native 正启用所有DLL插件。")
        bridge?.eventEnable() //加载所有DLL插件并触发事件

        GlobalScope.subscribeAlways<BotOnlineEvent> {
            logger.info("Bot 已上线，监听事件。")
            this.bot.subscribeMessages {
                sentByFriend {
                    bridge?.eventPrivateMessage(
                        Bridge.PRI_MSG_SUBTYPE_FRIEND, message.sequenceId,
                        sender.id, message.toString(), 0)
                }
            }
        }
    }

    override fun onDisable() {
        logger.info("Mirai Native 正停用所有DLL插件。")
        bridge?.eventDisable()
    }
}
