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

package org.itxtech.mirainative.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.manager.PluginManager
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextArea

object FloatingWindow {
    private var window: JFrame? = null
    private var text: JTextArea? = null

    val visible: Boolean
        get() = window?.isVisible ?: false

    fun create() {
        try {
            val panel = JPanel()

            window = JFrame("Mirai Native 悬浮窗").apply {
                setSize(250, 150)
                isResizable = false
                isAlwaysOnTop = true

                val rect =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
                setLocation(
                    (rect.maxX / 20 * 19.5 - width).toInt(),
                    (rect.maxY / 20 * 19 - height).toInt()
                )

                add(panel)

                addComponentListener(object : ComponentAdapter() {
                    override fun componentHidden(e: ComponentEvent) {
                        Tray.update()
                    }
                })
            }

            text = JTextArea().apply {
                setLocation(0, 0)
                setSize(250, 150)
                isEditable = false
                panel.add(this)
            }

            MiraiNative.launch {
                while (isActive) {
                    update()
                    delay(100)
                }
            }
        } catch (e: Throwable) {
            MiraiNative.logger.error(e)
        }
    }

    fun close() {
        if (window?.isVisible == true) {
            window!!.isVisible = false
        }
        window = null
        text = null
    }

    private fun update() {
        if (text != null) {
            val t = StringBuilder()
            PluginManager.plugins.values.forEach { p ->
                if (p.entries.isNotEmpty()) {
                    p.entries.forEach { e ->
                        if (p.enabled && e.visible) {
                            t.append(e.status.name).append(": ").append(e.data).append(" ").appendLine(e.unit)
                        }
                    }
                }
            }
            text!!.text = t.toString()
        }
    }

    fun toggle() {
        if (window != null) {
            window!!.isVisible = !window!!.isVisible
        }
    }
}
