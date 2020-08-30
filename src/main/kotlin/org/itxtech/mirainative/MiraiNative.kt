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

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import org.itxtech.mirainative.manager.CacheManager
import org.itxtech.mirainative.manager.EventManager
import org.itxtech.mirainative.manager.LibraryManager
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.ui.FloatingWindow
import org.itxtech.mirainative.ui.Tray
import org.itxtech.mirainative.util.ConfigMan
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.jar.Manifest

object MiraiNative : KotlinPlugin() {
    private val lib: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "libraries").also { it.mkdirs() } }
    private val dll: File by lazy { File(dataFolder.absolutePath + File.separatorChar + "CQP.dll") }
    val imageDataPath: File by lazy { File("data" + File.separatorChar + "image").also { it.mkdirs() } }
    val recDataPath: File by lazy { File("data" + File.separatorChar + "record").also { it.mkdirs() } }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val dispatcher = newSingleThreadContext("MiraiNative") + SupervisorJob()

    var botOnline = false
    val bot: Bot by lazy { Bot.botInstances.first() }

    private fun ByteArray.checksum() = BigInteger(1, MessageDigest.getInstance("MD5").digest(this))

    private fun checkNativeLibs() {
        logger.info("正在加载 ${dll.absolutePath}")
        LibraryManager.load(dll.absolutePath)

        lib.listFiles()?.forEach { file ->
            if (file.absolutePath.endsWith(".dll")) {
                logger.info("正在加载外部库 " + file.absolutePath)
                LibraryManager.load(file.absolutePath)
            }
        }
    }

    fun setBotOnline() {
        if (!botOnline) {
            botOnline = true
            nativeLaunch {
                ConfigMan.init()
                MiraiNative.logger.info("Mirai Native 正启用所有插件。")
                PluginManager.enablePlugins()
                Tray.update()
            }
        }
    }

    override fun onLoad() {
        //暂时只支持 x86 平台运行，不兼容 amd64
        val mode = System.getProperty("sun.arch.data.model")
        if (mode != "32") {
            logger.warning("当前运行环境 $mode 可能不与 Mirai Native 兼容，推荐使用 32位 JRE 运行 Mirai Native。")
            logger.warning("如果您正在开发或调试其他环境下的 Mirai Native，请忽略此警告。")
        }

        val nativeLib = getResourceAsStream("CQP.dll")!!
        if (!dll.exists()) {
            logger.error("找不到 ${dll.absolutePath}，写出自带的 CQP.dll。")
            val cqp = FileOutputStream(dll)
            nativeLib.copyTo(cqp)
            cqp.close()
        } else if (nativeLib.readBytes().checksum() != dll.readBytes().checksum()) {
            logger.warning("${dll.absolutePath} 与 Mirai Native 内置的 CQP.dll 的校验和不同。")
            logger.warning("如运行时出现问题，请尝试删除 ${dll.absolutePath} 并重启 mirai。")
        }

        initDataDir()
    }

    private fun initDataDir() {
        File(imageDataPath, "MIRAI_NATIVE_IMAGE_DATA").createNewFile()
        File(recDataPath, "MIRAI_NATIVE_RECORD_DATA").createNewFile()
    }

    fun getDataFile(type: String, name: String): File? {
        arrayOf(
            "data" + File.separatorChar + type + File.separatorChar,
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
        Tray.create()
        FloatingWindow.create()

        checkNativeLibs()
        PluginManager.loadPlugins()

        nativeLaunch {
            while (isActive) {
                Bridge.processMessage()
                delay(10)
            }
        }

        PluginManager.registerCommands()
        EventManager.registerEvents()

        if (Bot.botInstances.isNotEmpty() && Bot.botInstances.first().isOnline) {
            setBotOnline()
        }
    }

    override fun onDisable() {
        ConfigMan.save()
        CacheManager.clear()
        Tray.close()
        FloatingWindow.close()
        runBlocking {
            PluginManager.unloadPlugins().join()
            nativeLaunch { Bridge.shutdown() }.join()
            dispatcher.cancel()
            dispatcher[Job]?.join()
        }
    }

    fun nativeLaunch(b: suspend CoroutineScope.() -> Unit) = launch(context = dispatcher, block = b)

    fun getVersion(): String {
        var version = description.version.value
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
