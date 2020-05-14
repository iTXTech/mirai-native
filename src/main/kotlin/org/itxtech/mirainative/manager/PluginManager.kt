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

import kotlinx.atomicfu.atomic
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.registerCommand
import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.NativeBridge
import org.itxtech.mirainative.plugin.NativePlugin
import org.itxtech.mirainative.plugin.PluginInfo
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.NpmHelper
import java.io.File

@OptIn(UnstableDefault::class)
object PluginManager {
    private val pluginId = atomic(0)
    val plugins = hashMapOf<Int, NativePlugin>()
    private val pl: File by lazy { File(MiraiNative.dataFolder.absolutePath + File.separatorChar + "plugins").also { it.mkdirs() } }

    fun loadPlugins() {
        if (!MiraiNative.dataFolder.isDirectory) {
            MiraiNative.logger.error("数据文件夹不是一个文件夹！" + MiraiNative.dataFolder.absolutePath)
        } else {
            MiraiNative.nativeLaunch {
                pl.listFiles()?.forEach { file ->
                    if (file.isFile && file.absolutePath.endsWith("dll") && !file.absolutePath.endsWith("CQP.dll")) {
                        loadPlugin(file)
                    }
                }
            }
        }
    }

    fun unloadPlugins() {
        MiraiNative.logger.info("正停用所有插件并调用Exit事件。")
        MiraiNative.nativeLaunch {
            plugins.values.forEach {
                if (it.enabled) {
                    NativeBridge.disablePlugin(it)
                }
                NativeBridge.exitPlugin(it)
                NativeBridge.unloadPlugin(it)
            }
            plugins.clear()
        }
    }

    fun getPluginByIdentifier(id: String): NativePlugin? {
        plugins.values.forEach {
            if (it.identifier == id) {
                return it
            }
        }
        return null
    }

    fun loadPluginFromFile(f: String): Boolean {
        val file = File(MiraiNative.dataFolder.absolutePath + File.separatorChar + f)
        if (file.isFile && file.exists()) {
            loadPlugin(file)
            return true
        }
        return false
    }

    fun loadPlugin(file: File) {
        plugins.values.forEach {
            if (it.loaded && it.file == file) {
                MiraiNative.logger.error("DLL ${file.absolutePath} 已被加载，无法重复加载。")
                return
            }
        }
        MiraiNative.nativeLaunch {
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
            if (NativeBridge.loadPlugin(plugin) == 0) {
                plugin.loaded = true
                plugins[pluginId.getAndIncrement()] = plugin
                NativeBridge.updateInfo(plugin)
                NativeBridge.startPlugin(plugin)
                Tray.update()
            }
        }
    }

    fun unloadPlugin(plugin: NativePlugin) {
        MiraiNative.nativeLaunch {
            disablePlugin(plugin)
            NativeBridge.exitPlugin(plugin)
            if (NativeBridge.unloadPlugin(plugin) == 0) {
                plugin.loaded = false
                plugin.enabled = false
                Tray.update()
            }
        }
    }

    fun enablePlugin(plugin: NativePlugin): Boolean {
        if (MiraiNative.botOnline && !plugin.enabled) {
            MiraiNative.nativeLaunch {
                NativeBridge.enablePlugin(plugin)
                plugin.enabled = true
                Tray.update()
            }
            return true
        }
        return false
    }

    fun disablePlugin(plugin: NativePlugin): Boolean {
        if (plugin.enabled) {
            MiraiNative.nativeLaunch {
                NativeBridge.disablePlugin(plugin)
                plugin.enabled = false
                Tray.update()
            }
            return true
        }
        return false
    }

    fun enablePlugins() {
        plugins.values.forEach {
            if (it.autoEnable) {
                enablePlugin(it)
            }
        }
    }

    fun registerCommands() {
        MiraiNative.registerCommand {
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
                                            " 版本：" + p.pluginInfo!!.version + " " + NpmHelper.state(p)

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
                        if (!MiraiNative.botOnline) {
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
                            MiraiNative.nativeLaunch {
                                Bridge.callIntMethod(it[1].toInt(), it[2])
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
                            unloadPlugin(
                                plugins[it[1].toInt()]!!
                            )
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
}
