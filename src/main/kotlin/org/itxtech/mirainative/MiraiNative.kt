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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.registerCommand
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.console.plugins.PluginManager
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.currentTimeSeconds
import org.itxtech.mirainative.message.ChainCodeConverter
import org.itxtech.mirainative.message.MessageCache
import org.itxtech.mirainative.plugin.NativePlugin
import org.itxtech.mirainative.plugin.PluginInfo
import org.itxtech.mirainative.ui.FloatingWindow
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.NpmHelper
import java.io.File
import java.util.concurrent.Executors
import java.util.jar.Manifest
import kotlin.coroutines.ContinuationInterceptor

@OptIn(UnstableDefault::class)
object MiraiNative : PluginBase() {
    private var pluginId = atomic(0)
    var botOnline = false
    val bridge = Bridge()
    var plugins: HashMap<Int, NativePlugin> = HashMap()
    val bot: Bot by lazy { Bot.instances.first().get()!! }
    private val lib: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "libraries") }
    private val dll: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "CQP.dll") }

    override fun onLoad() {
        if (dll.exists()) {
            logger.info("正在加载 ${dll.absolutePath}")
            System.load(dll.absolutePath)
        } else {
            logger.error("找不到 ${dll.absolutePath}")
            return
        }

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
                    loadPlugin(file)
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

    fun loadPluginFromFile(f: String): Boolean {
        val file = File(dataFolder.absolutePath + File.separatorChar + f)
        if (file.isFile && file.exists()) {
            loadPlugin(file)
            return true
        }
        return false
    }

    fun loadPlugin(file: File) {
        plugins.values.forEach {
            if (it.loaded && it.file == file) {
                logger.error("DLL ${file.absolutePath} 已被加载，无法重复加载。")
                return
            }
        }
        launch(NativeDispatcher) {
            val plugin = NativePlugin(file, pluginId.value)
            val json = File(file.parent + File.separatorChar + file.name.replace(".dll", ".json"))
            if (json.exists()) {
                plugin.pluginInfo = Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    serializeSpecialFloatingPointValues = true
                    useArrayPolymorphism = true
                }.parse(PluginInfo.serializer(), json.readText())
            }
            if (bridge.loadPlugin(plugin) == 0) {
                plugins[pluginId.getAndIncrement()] = plugin
                bridge.updateInfo(plugin)
                bridge.startPlugin(plugin)
                Tray.update()
            }
        }
    }

    fun unloadPlugin(plugin: NativePlugin) {
        launch(NativeDispatcher) {
            bridge.disablePlugin(plugin)
            bridge.exitPlugin(plugin)
            delay(500)
            bridge.unloadPlugin(plugin)
            Tray.update()
        }
    }

    fun enablePlugin(plugin: NativePlugin): Boolean {
        if (botOnline && !plugin.enabled) {
            launch(NativeDispatcher) {
                bridge.enablePlugin(plugin)
            }
            Tray.update()
            return true
        }
        return false
    }

    fun disablePlugin(plugin: NativePlugin): Boolean {
        if (plugin.enabled) {
            launch(NativeDispatcher) {
                bridge.disablePlugin(plugin)
            }
            Tray.update()
            return true
        }
        return false
    }

    override fun onEnable() {
        registerCommands()
        registerEvents()

        launch(NativeDispatcher) {
            while (isActive) {
                bridge.processMessage()
                delay(10)
            }
        }
    }

    private fun registerCommands() {
        registerCommand {
            name = "npm"
            description = "Mirai Native 插件管理器"
            usage = "npm [list|enable|disable|menu|info|load|unload] (插件 Id / 路径) (方法名)"
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
                                            " 版本：" + p.pluginInfo!!.version + NpmHelper.state(p)

                                )
                            } else {
                                appendMessage(
                                    "Id：" + p.id + " 标识符：" + p.identifier + " （JSON文件缺失）" +
                                            NpmHelper.state(p)
                                )
                            }
                        }
                    }
                    "enable" -> {
                        if (!botOnline) {
                            appendMessage("Bot 还未上线，无法调用 Enable 事件。")
                        } else {
                            if (plugins.containsKey(it[1].toInt())) {
                                val p = plugins[it[1].toInt()]!!
                                enablePlugin(p)
                                appendMessage("插件 " + p.identifier + " 已启用。")
                            } else {
                                appendMessage("Id " + it[1] + " 不存在。")
                            }
                        }
                    }
                    "disable" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            val p = plugins[it[1].toInt()]!!
                            disablePlugin(p)
                            appendMessage("插件 " + p.identifier + " 已禁用。")
                        } else {
                            appendMessage("Id " + it[1] + " 不存在。")
                        }
                    }
                    "menu" -> {
                        if (it.size < 3) {
                            return@onCommand false
                        }
                        if (plugins.containsKey(it[1].toInt()) && plugins[it[1].toInt()]!!.verifyMenuFunc(it[2])) {
                            launch(NativeDispatcher) {
                                bridge.callIntMethod(it[1].toInt(), it[2])
                            }
                            appendMessage("已调用 Id " + it[1] + " 的 " + it[2] + " 方法。")
                        } else {
                            appendMessage("Id " + it[2] + " 不存在，或未注册该菜单入口。")
                        }
                    }
                    "info" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            appendMessage(NpmHelper.summary(plugins[it[1].toInt()]!!))
                        } else {
                            appendMessage("Id " + it[1] + " 不存在。")
                        }
                    }
                    "load" -> {
                        if (!loadPluginFromFile(it[1])) {
                            appendMessage("文件 ${it[1]} 不存在")
                        }
                    }
                    "unload" -> {
                        if (plugins.containsKey(it[1].toInt())) {
                            unloadPlugin(plugins[it[1].toInt()]!!)
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
            botOnline = true
            launch(NativeDispatcher) {
                logger.info("Mirai Native 正启用所有 DLL 插件。")
                bridge.eventEnable()
                Tray.update()
            }
        }

        // 消息事件
        subscribeAlways<FriendMessage> {
            launch(NativeDispatcher) {
                bridge.eventPrivateMessage(
                    Bridge.PRI_MSG_SUBTYPE_FRIEND,
                    MessageCache.cacheMessage(message[MessageSource]),
                    sender.id,
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }
        subscribeAlways<GroupMessage> {
            launch(NativeDispatcher) {
                bridge.eventGroupMessage(
                    1,
                    MessageCache.cacheMessage(message[MessageSource]),
                    group.id,
                    sender.id,
                    "",
                    ChainCodeConverter.chainToCode(message),
                    0
                )
            }
        }

        // 权限事件
        subscribeAlways<MemberPermissionChangeEvent> {
            launch(NativeDispatcher) {
                if (new == MemberPermission.MEMBER) {
                    bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_CANCEL_ADMIN, getTimestamp(), group.id, member.id)
                } else {
                    bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_SET_ADMIN, getTimestamp(), group.id, member.id)
                }
            }
        }
        subscribeAlways<BotGroupPermissionChangeEvent> {
            launch(NativeDispatcher) {
                if (new == MemberPermission.MEMBER) {
                    bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_CANCEL_ADMIN, getTimestamp(), group.id, bot.uin)
                } else {
                    bridge.eventGroupAdmin(Bridge.PERM_SUBTYPE_SET_ADMIN, getTimestamp(), group.id, bot.uin)
                }
            }
        }

        // 加群事件
        subscribeAlways<MemberJoinEvent> {
            launch(NativeDispatcher) {
                // TODO: 区分管理员批准/邀请，添加批准者
                bridge.eventGroupMemberJoin(Bridge.MEMBER_JOIN_PERMITTED, getTimestamp(), group.id, 0, member.id)
            }
        }

        // 退群事件
        subscribeAlways<MemberLeaveEvent.Kick> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.uin
                bridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_KICK, getTimestamp(), group.id, op, member.id)
            }
        }
        subscribeAlways<MemberLeaveEvent.Quit> {
            launch(NativeDispatcher) {
                bridge.eventGroupMemberLeave(Bridge.MEMBER_LEAVE_QUIT, getTimestamp(), group.id, 0, member.id)
            }
        }

        // 禁言事件
        subscribeAlways<MemberMuteEvent> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.uin
                bridge.eventGroupBan(
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
                val op = operator?.id ?: bot.uin
                bridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, member.id, 0)
            }
        }
        subscribeAlways<BotMuteEvent> {
            launch(NativeDispatcher) {
                bridge.eventGroupBan(
                    Bridge.GROUP_MUTE,
                    getTimestamp(),
                    group.id,
                    operator.id,
                    bot.uin,
                    durationSeconds.toLong()
                )
            }
        }
        subscribeAlways<BotUnmuteEvent> {
            launch(NativeDispatcher) {
                bridge.eventGroupBan(
                    Bridge.GROUP_UNMUTE,
                    getTimestamp(),
                    group.id,
                    operator.id,
                    bot.uin,
                    0
                )
            }
        }
        subscribeAlways<GroupMuteAllEvent> {
            launch(NativeDispatcher) {
                val op = operator?.id ?: bot.uin
                if (new) {
                    bridge.eventGroupBan(Bridge.GROUP_MUTE, getTimestamp(), group.id, op, 0, 0)
                } else {
                    bridge.eventGroupBan(Bridge.GROUP_UNMUTE, getTimestamp(), group.id, op, 0, 0)
                }
            }
        }
    }

    private fun getTimestamp(): Int {
        return currentTimeSeconds.toInt()
    }

    override fun onDisable() {
        launch(NativeDispatcher) {
            logger.info("Mirai Native 正停用所有DLL插件。")
            bridge.eventDisable()

            logger.info("Mirai Native 正调用 Exit 事件")
            bridge.eventExit()
        }
    }

    fun getVersion(): String {
        var version = PluginManager.getPluginDescription(MiraiNative).version
        val mf = this.javaClass.classLoader.getResources("META-INF/MANIFEST.MF")
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
