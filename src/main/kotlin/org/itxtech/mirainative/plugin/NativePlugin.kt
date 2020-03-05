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

import io.ktor.util.InternalAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.itxtech.mirainative.BridgeHelper
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.NativeDispatcher
import java.io.File

@InternalAPI
data class NativePlugin(val file: File, val id: Int) {
    var enabled: Boolean = false
    var api: Int = -1
    var identifier: String = file.name
    val appDir: File by lazy {
        File(file.parent + File.separatorChar + identifier).also { it.mkdir() }
    }
    var pluginInfo: PluginInfo? = null
        set(v) {
            events = HashMap()
            v!!.event.forEach {
                events!![it.type] = it.function
            }
            if (v.status.isNotEmpty()) {
                registerFws(v.status)
            }
            field = v
        }
    private var events: HashMap<Int, String>? = null
    private val entries: ArrayList<FloatingWindowEntry> = ArrayList()

    private fun registerFws(fws: ArrayList<Status>) {
        fws.forEach {
            val entry = FloatingWindowEntry(it)
            entries.add(entry)
            MiraiNative.INSTANCE.launch(NativeDispatcher) {
                while (isActive) {
                    if (enabled) {
                        BridgeHelper.updateFwe(id, entry)
                        delay(it.period.toLong())
                    }
                }
            }
        }
    }

    fun setInfo(i: String) {
        val parts = i.split(",")
        if (parts.size == 2) {
            api = parts[0].toInt()
            identifier = parts[1]
        }
    }

    fun getEventOrDefault(key: Int, default: String): String {
        if (events == null) {
            return default
        }
        return events!!.getOrDefault(key, default)
    }

    @JvmOverloads
    fun shouldCallEvent(key: Int, ignoreState: Boolean = false): Boolean {
        if (!enabled && !ignoreState) {
            return false
        }
        if (events == null) {
            return true
        }
        return events!!.containsKey(key)
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
