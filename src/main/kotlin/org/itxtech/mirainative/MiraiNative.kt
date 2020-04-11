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

package org.itxtech.mirainative

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.PluginManager.getPluginDescription
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.TempMessage
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.currentTimeSeconds
import org.itxtech.mirainative.bridge.NativeBridge
import org.itxtech.mirainative.message.CacheManager
import org.itxtech.mirainative.message.ChainCodeConverter
import org.itxtech.mirainative.ui.FloatingWindow
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.ConfigMan
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.jar.Manifest
import kotlin.coroutines.ContinuationInterceptor

object MiraiNative : PluginBase() {
    var botOnline = false
    val bot: Bot by lazy { Bot.instances.first().get()!! }
    private val lib: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "libraries") }
    private val dll: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "CQP.dll") }

    override fun onLoad() {
        //暂时只支持 x86 平台运行，不兼容 amd64
        val mode = System.getProperty("sun.arch.data.model")
        if (mode != "32") {
            logger.warning("当前运行环境 $mode 可能不与 Mirai Native 兼容，推荐使用 32位 JRE 运行 Mirai Native。")
            logger.warning("如果您正在开发或调试其他环境下的 Mirai Native，请忽略此警告。")
        }

        if (!dll.exists()) {
            logger.error("找不到 ${dll.absolutePath}，写出自带的 CQP.dll。")
            val cqp = FileOutputStream(dll)
            getResources("CQP.dll")?.copyTo(cqp)
            cqp.close()
        }
        logger.info("正在加载 ${dll.absolutePath}")
        System.load(dll.absolutePath)

        initDataDir()

        lib.listFiles()?.forEach { file ->
            if (file.absolutePath.endsWith(".dll")) {
                logger.info("正在加载外部库 " + file.absolutePath)
                System.load(file.absolutePath)
            }
        }

        Tray.create()
        FloatingWindow.create()

        if (!dataFolder.isDirectory) {
            logger.error("数据文件夹不是一个文件夹！" + dataFolder.absolutePath)
        } else {
            dataFolder.listFiles()?.forEach { file ->
                if (file.isFile && file.absolutePath.endsWith("dll") && !file.absolutePath.endsWith("CQP.dll")) {
                    PluginManager.loadPlugin(file)
                }
            }
        }
    }

    private fun initDataDir() {
        lib.mkdirs()
        File("data" + File.separatorChar + "image").mkdirs()
        File(
            System.getProperty("java.library.path")
                .substringBefore(";") + File.separatorChar + "data" + File.separatorChar + "image"
        ).mkdirs()
    }

    fun getDataFile(type: String, name: String): File? {
        arrayOf(
            "data" + File.separatorChar + type + File.separatorChar,
            System.getProperty("java.library.path")
                .substringBefore(";") + File.separatorChar + "data" + File.separatorChar + type + File.separatorChar,
            ""
        ).forEach {
            val f = File(it + name).absoluteFile
            if (f.exists()) {
                return f
            }
        }
        return null
    }

    override fun onEnable() {
        PluginManager.registerCommands()
        registerEvents()

        launch(NativeDispatcher) {
            while (isActive) {
                Bridge.processMessage()
                delay(10)
            }
        }
    }

    override fun onDisable() {
        ConfigMan.save()

        launch(NativeDispatcher) {
            logger.info("Mirai Native 正停用所有DLL插件并调用Exit事件。")
            PluginManager.disableAndExitPlugins()
        }
    }

    private fun registerEvents() {
        subscribeAlways<BotOnlineEvent> {
            botOnline = true
            launch(NativeDispatcher) {
                ConfigMan.init()
                logger.info("Mirai Native 正启用所有 DLL 插件。")
                PluginManager.enablePlugins()
                Tray.update()
            }
        }

        // 消息事件
        subscribeAlways<FriendMessage> {
            launch(NativeDispatcher) {
                NativeBridge.eventPrivateMessage(
                    Bridge.PRI_MSG_SUBTYPE_FRIEND,
                    CacheManager.cacheMessage(message[MessageSource]),
                    sender.id,
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }
        subscribeAlways<GroupMessage> {
            launch(NativeDispatcher) {
                NativeBridge.eventGroupMessage(
                    1,
                    CacheManager.cacheMessage(message[MessageSource]),
                    group.id,
                    sender.id,
                    "",
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }
        subscribeAlways<TempMessage> {
            launch(NativeDispatcher) {
                NativeBridge.eventPrivateMessage(
                    Bridge.PRI_MSG_SUBTYPE_GROUP,
                    CacheManager.cacheMessage(message[MessageSource]),
                    sender.id,
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }

        // 权限事件
        subscribeAlways<MemberPermissionChangeEvent> {
            launch(NativeDispatcher) {
                if (new == MemberPermission.MEMBER) {
                    NativeBridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_CANCEL_ADMIN, getTimestamp(), group.id, member.id)
                } else {
                    NativeBridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_SET_ADMIN, getTimestamp(), group.id, member.id)
                }
            }
        }
        subscribeAlways<BotGroupPermissionChangeEvent> {
            launch(NativeDispatcher) {
                if (new == MemberPermission.MEMBER) {
                    NativeBridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_CANCEL_ADMIN, getTimestamp(), group.id, bot.id)
                } else {
                    NativeBridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_SET_ADMIN, getTimestamp(), group.id, bot.id)
                }
            }
        }

        // 加群事件
        subscribeAlways<MemberJoinEvent> { ev ->
            launch(NativeDispatcher) {
                NativeBridge.eventGroupMemberJoin(
                    if (ev is MemberJoinEvent.Invite) Bridge.MEMBER_JOIN_PERMITTED else Bridge.MEMBER_JOIN_INVITED_BY_ADMIN,
                    getTimestamp(), group.id, 0, member.id
                )
            }
        }
        subscribeAlways<MemberJoinRequestEvent> { ev ->
            launch(NativeDispatcher) {
                NativeBridge.eventRequestAddGroup(
                    Bridge.REQUEST_GROUP_INVITED,
                    getTimestamp(), groupId, fromId, message, CacheManager.cacheEvent(ev)
                )
            }
        }

        //加好友事件
        subscribeAlways<NewFriendRequestEvent> { ev ->
            launch(NativeDispatcher) {
                NativeBridge.eventRequestAddFriend(1, getTimestamp(), fromId, message, CacheManager.cacheEvent(ev))
            }
        }
        subscribeAlways<FriendAddEvent> {
            launch(NativeDispatcher) {
                NativeBridge.eventFriendAdd(1, getTimestamp(), friend.id)
            }
        }

        // 退群事件
        subscribeAlways<MemberLeaveEvent.Kick> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.id
                NativeBridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_KICK, getTimestamp(), group.id, op, member.id)
            }
        }
        subscribeAlways<MemberLeaveEvent.Quit> {
            launch(NativeDispatcher) {
                NativeBridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_QUIT, getTimestamp(), group.id, 0, member.id)
            }
        }

        // 禁言事件
        subscribeAlways<MemberMuteEvent> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.id
                NativeBridge.eventGroupBan(
                    Bridge.GROUP_MUTE,
                    getTimestamp(),
                    group.id,
                    op,
                    member.id,
                    durationSeconds.toLong()
                )
            }
        }
        subscribeAlways<MemberUnmuteEvent> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.id
                NativeBridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, member.id, 0)
            }
        }
        subscribeAlways<BotMuteEvent> {
            launch(NativeDispatcher) {
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
            launch(NativeDispatcher) {
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
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.id
                if (new) {
                    NativeBridge.eventGroupBan(Bridge.GROUP_MUTE, getTimestamp(), group.id, op, 0, 0)
                } else {
                    NativeBridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, 0, 0)
                }
            }
        }
    }

    private fun getTimestamp(): Int {
        return currentTimeSeconds.toInt()
    }

    fun getVersion(): String {
        var version = getPluginDescription(MiraiNative).version
        val mf = javaClass.classLoader.getResources("META-INF/MANIFEST.MF")
        while (mf.hasMoreElements()) {
            val manifest = Manifest(mf.nextElement().openStream())
            if ("iTXTech MiraiNative" == manifest.mainAttributes.getValue("Name")) {
                version += "-" + manifest.mainAttributes.getValue("Revision")
            }
        }
        return version
    }
}

object NativeDispatcher : ContinuationInterceptor by Executors.newFixedThreadPool(1).asCoroutineDispatcher()
