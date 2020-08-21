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

package org.itxtech.mirainative.plugin

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.MiraiBridge
import org.itxtech.mirainative.ui.FloatingWindow
import java.io.File

data class NativePlugin(val file: File, val id: Int) {
    var autoEnable = true
    var loaded = false
    var enabled = false
    var api = -1
    var identifier: String = file.name
    val appDir: File by lazy {
        File(MiraiNative.dataFolder.absolutePath + File.separatorChar + "data" + File.separatorChar + identifier).also { it.mkdirs() }
    }
    var pluginInfo: PluginInfo? = null
        set(v) {
            v!!.event.forEach {
                events[it.type] = it.function
            }
            if (v.status.isNotEmpty()) {
                registerFws(v.status)
            }
            field = v
        }
    private val events = hashMapOf<Int, String>()
    val entries = arrayListOf<FloatingWindowEntry>()

    private fun registerFws(fws: ArrayList<Status>) {
        fws.forEach {
            val entry = FloatingWindowEntry(it)
            entries.add(entry)
            MiraiNative.nativeLaunch {
                while (isActive) {
                    if (enabled && entry.visible && FloatingWindow.isVisible()) {
                        MiraiBridge.updateFwe(id, entry)
                    }
                    delay(it.period.toLong())
                }
            }
        }
    }

    fun getName(): String {
        return pluginInfo?.name ?: identifier
    }

    fun setInfo(i: String) {
        val parts = i.split(",")
        if (parts.size == 2) {
            api = parts[0].toInt()
            identifier = parts[1]
        }
    }

    fun getEventOrDefault(key: Int, default: String): String {
        return events.getOrDefault(key, default)
    }

    @JvmOverloads
    fun shouldCallEvent(key: Int, ignoreState: Boolean = false): Boolean {
        if (!enabled && !ignoreState) {
            return false
        }
        return events.containsKey(key)
    }

    fun processMessage(key: Int, msg: String): String {
        //TODO: regex
        return msg
    }

    fun verifyMenuFunc(name: String): Boolean {
        if (pluginInfo == null) {
            return true
        }
        pluginInfo!!.menu.forEach {
            if (it.function == name) {
                return true
            }
        }
        return false
    }
}
