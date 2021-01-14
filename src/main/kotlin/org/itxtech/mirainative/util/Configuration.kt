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

package org.itxtech.mirainative.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.ui.FloatingWindow
import java.io.File

object ConfigMan {
    private val file = File(MiraiNative.dataFolder.absolutePath + File.separatorChar + "config.json")
    val config: Configuration by lazy {
        if (file.exists()) {
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                allowSpecialFloatingPointValues = true
                useArrayPolymorphism = true
            }.decodeFromString(Configuration.serializer(), file.readText())
        } else {
            Configuration()
        }
    }

    fun init() {
        if (config.fwState && !FloatingWindow.visible) {
            FloatingWindow.toggle()
        }
        config.plugins.forEach { e ->
            val p = PluginManager.getPluginByIdentifier(e.id)
            if (p != null) {
                p.autoEnable = e.enable
                e.visibleFwes.forEach { f ->
                    p.entries.forEach {
                        if (it.status.title == f) {
                            it.visible = true
                        }
                    }
                }
            }
        }
    }

    fun save() {
        config.fwState = FloatingWindow.visible
        config.plugins = ArrayList()
        PluginManager.plugins.values.forEach { p ->
            val entry = PluginEntry()
            entry.id = p.identifier
            if (MiraiNative.botOnline) {
                entry.enable = p.enabled
            }
            p.entries.forEach { e ->
                if (e.visible) {
                    entry.visibleFwes.add(e.status.title)
                }
            }
            config.plugins.add(entry)
        }
        file.writeText(Json.encodeToString(Configuration.serializer(), config))
    }
}

@Serializable
data class Configuration(
    var verboseNativeApiLog: Boolean = false,
    var fwState: Boolean = false,
    var plugins: ArrayList<PluginEntry> = ArrayList()
)

@Serializable
data class PluginEntry(
    var id: String = "",
    var enable: Boolean = true,
    var visibleFwes: ArrayList<String> = ArrayList()
)
