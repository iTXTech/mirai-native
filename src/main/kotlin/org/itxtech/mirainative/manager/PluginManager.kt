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
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.NativeBridge
import org.itxtech.mirainative.plugin.NativePlugin
import org.itxtech.mirainative.plugin.PluginInfo
import org.itxtech.mirainative.toNative
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.NpmHelper
import java.io.File

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
                        readPlugin(file)
                    }
                }
            }
        }
    }

    fun unloadPlugins(): Job {
        MiraiNative.logger.info("正停用所有插件并调用Exit事件。")
        return MiraiNative.nativeLaunch {
            plugins.values.forEach {
                unloadPlugin(it)
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

    fun readPluginFromFile(f: String): Boolean {
        val file = File(pl.absolutePath + File.separatorChar + f)
        if (f.endsWith(".dll") && file.isFile && file.exists()) {
            readPlugin(file)
            return true
        }
        return false
    }

    fun readPlugin(file: File) {
        plugins.values.forEach {
            if (it.loaded && it.file == file) {
                MiraiNative.logger.error("DLL ${file.absolutePath} 已被加载，无法重复加载。")
                return
            }
        }
        MiraiNative.nativeLaunch {
            val plugin = NativePlugin(file, pluginId.value)
            loadPlugin(plugin)
            plugins[pluginId.getAndIncrement()] = plugin
            Tray.update()
        }
    }

    fun loadPlugin(plugin: NativePlugin) {
        if (plugin.loaded) {
            return
        }
        lateinit var suffix: String
        val file = if (plugin.reloadable) {
            suffix = ".tmp"
            plugin.tempFile = File(plugin.file.absolutePath.replace(".dev.dll", suffix))
            plugin.file.copyTo(plugin.tempFile!!, true)
        } else {
            suffix = ".dll"
            plugin.file
        }
        if (NativeBridge.loadPlugin(plugin, file) == 0) {
            val json = File(file.parent + File.separatorChar + file.name.replace(suffix, ".json"))

            try {
                plugin.pluginInfo = Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    allowSpecialFloatingPointValues = true
                    useArrayPolymorphism = true
                }.decodeFromString(
                    PluginInfo.serializer(),
                    if (json.exists()) json.readText() else NativeBridge.getPluginInfo(plugin)
                )
            } catch (ignored: Throwable) {
            }
            if (plugin.pluginInfo == null) {
                MiraiNative.logger.warning("无法找到 ${plugin.file.name} 的插件信息。")
            }
            plugin.loaded = true
            NativeBridge.updateInfo(plugin)
        }
    }

    fun unloadPlugin(plugin: NativePlugin) {
        if (plugin.loaded) {
            disablePlugin(plugin)
            NativeBridge.exitPlugin(plugin)
            if (NativeBridge.unloadPlugin(plugin) == 0) {
                plugin.loaded = false
                plugin.enabled = false
                plugin.started = false
                plugin.entries.forEach { it.vaild = false }
                plugin.entries.clear()
                plugin.events.clear()
                plugin.tempFile?.delete()
                Tray.update()
            }
        }
    }

    fun reloadPlugin(plugin: NativePlugin) {
        if (plugin.loaded) {
            if (plugin.reloadable) {
                unloadPlugin(plugin)
                loadPlugin(plugin)
                plugins[plugin.id] = plugin
                Tray.update()
            } else {
                MiraiNative.logger.error("插件 ${plugin.detailedIdentifier} 不可重新加载。文件名必须以 \".dev.dll\" 结尾。")
            }
        }
    }

    fun enablePlugin(plugin: NativePlugin): Boolean {
        if (MiraiNative.botOnline && !plugin.enabled) {
            if (!plugin.started) {
                NativeBridge.startPlugin(plugin)
                plugin.started = true
            }
            NativeBridge.enablePlugin(plugin)
            plugin.enabled = true
            Tray.update()
            return true
        }
        return false
    }

    fun disablePlugin(plugin: NativePlugin): Boolean {
        if (plugin.enabled) {
            NativeBridge.disablePlugin(plugin)
            plugin.enabled = false
            Tray.update()
            return true
        }
        return false
    }

    fun enablePlugins() {
        MiraiNative.nativeLaunch {
            plugins.values.forEach {
                if (it.autoEnable) {
                    enablePlugin(it)
                }
            }
        }
    }

    @OptIn(ConsoleExperimentalAPI::class)
    object NpmCommand : CompositeCommand(
        MiraiNative, "npm",
        description = "Mirai Native 插件管理器"
    ) {
        @Description("列出所有 Mirai Native 插件")
        @SubCommand
        suspend fun CommandSender.list() {
            sendMessage(buildString {
                appendLine("共加载了 ${plugins.size} 个 Mirai Native 插件")
                plugins.values.forEach { p ->
                    if (p.pluginInfo != null) {
                        appendLine(
                            "Id：${p.id} 标识符：${p.identifier} 名称：${p.pluginInfo!!.name} 版本：${p.pluginInfo!!.version} ${
                                NpmHelper.state(
                                    p
                                )
                            }"
                        )
                    } else {
                        appendLine("Id：${p.id} 标识符：${p.identifier} （JSON文件缺失）${NpmHelper.state(p)}")
                    }
                }
            })
        }

        @Description("启用指定 Mirai Native 插件")
        @SubCommand
        suspend fun CommandSender.enable(@Name("插件Id") id: Int) {
            sendMessage(buildString {
                if (!MiraiNative.botOnline) {
                    appendLine("Bot 还未上线，无法调用 Enable 事件。")
                } else {
                    if (plugins.containsKey(id)) {
                        val p = plugins[id]!!
                        MiraiNative.nativeLaunch {
                            enablePlugin(p)
                        }
                        appendLine("插件 ${p.identifier} 已启用。")
                    } else {
                        appendLine("Id $id 不存在。")
                    }
                }
            })
        }

        @Description("停用指定 Mirai Native 插件")
        @SubCommand
        suspend fun CommandSender.disable(@Name("插件Id") id: Int) {
            sendMessage(buildString {
                if (plugins.containsKey(id)) {
                    val p = plugins[id]!!
                    MiraiNative.nativeLaunch {
                        disablePlugin(p)
                    }
                    appendLine("插件 ${p.identifier} 已禁用。")
                } else {
                    appendLine("Id $id 不存在。")
                }
            })
        }

        @Description("调用指定 Mirai Native 插件的菜单方法")
        @SubCommand
        suspend fun CommandSender.menu(@Name("插件Id") id: Int, @Name("方法名") method: String) {
            sendMessage(buildString {
                if (plugins[id]?.verifyMenuFunc(method) == true) {
                    MiraiNative.nativeLaunch {
                        Bridge.callIntMethod(id, method.toNative())
                    }
                    appendLine("已调用 Id $id 的 $method 方法。")
                } else {
                    appendLine("Id $id 不存在，或未注册该菜单入口。")
                }
            })
        }

        @Description("查看指定 Mirai Native 插件的详细信息")
        @SubCommand
        suspend fun CommandSender.info(@Name("插件Id") id: Int) {
            sendMessage(buildString {
                if (plugins.containsKey(id)) {
                    appendLine(NpmHelper.summary(plugins[id]!!))
                } else {
                    appendLine("Id $id 不存在。")
                }
            })
        }

        @Description("加载指定DLL文件")
        @SubCommand
        suspend fun CommandSender.load(@Name("DLL文件名") file: String) {
            sendMessage(buildString {
                if (!readPluginFromFile(file)) {
                    appendLine("文件 $file 不存在。")
                }
            })
        }

        @Description("卸载指定 Mirai Native 插件")
        @SubCommand
        suspend fun CommandSender.unload(@Name("插件Id") id: Int) {
            sendMessage(buildString {
                if (plugins.containsKey(id)) {
                    MiraiNative.nativeLaunch {
                        unloadPlugin(plugins[id]!!)
                    }
                } else {
                    appendLine("Id $id 不存在。")
                }
            })
        }

        @Description("重新载入指定 Mirai Native 插件")
        @SubCommand
        suspend fun CommandSender.reload(@Name("插件Id") id: Int) {
            sendMessage(buildString {
                if (plugins.containsKey(id)) {
                    MiraiNative.nativeLaunch {
                        reloadPlugin(plugins[id]!!)
                    }
                } else {
                    appendLine("Id $id 不存在。")
                }
            })
        }
    }

    fun registerCommands() {
        NpmCommand.register()
    }
}
