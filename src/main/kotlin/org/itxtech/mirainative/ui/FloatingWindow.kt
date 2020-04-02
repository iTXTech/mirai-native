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

package org.itxtech.mirainative.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.itxtech.mirainative.MiraiNative
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextArea

object FloatingWindow {
    private val window = JFrame("悬浮窗")
    private val text = JTextArea()

    fun create() {
        window.setSize(250, 150)
        window.isResizable = false
        window.isAlwaysOnTop = true

        val rect = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
        window.setLocation(
            (rect.maxX / 20 * 19.5 - window.width).toInt(),
            (rect.maxY / 20 * 19 - window.height).toInt()
        )

        val panel = JPanel()
        window.add(panel)
        text.setLocation(0, 0)
        text.setSize(250, 150)
        text.isEditable = false
        panel.add(text)

        window.addComponentListener(object : ComponentAdapter() {
            override fun componentHidden(e: ComponentEvent) {
                Tray.update()
            }
        })

        MiraiNative.launch {
            while (isActive) {
                update()
                delay(100)
            }
        }
    }

    private fun update() {
        val t = StringBuilder()
        MiraiNative.plugins.values.forEach { p ->
            if (p.entries.isNotEmpty()) {
                p.entries.forEach { e ->
                    if (p.enabled && e.visible) {
                        t.append(e.status.title).append(": ").append(e.data).append(" ").appendln(e.unit)
                    }
                }
            }
        }
        text.text = t.toString()
    }

    fun toggle() {
        window.isVisible = !window.isVisible
    }

    fun isVisible(): Boolean {
        return window.isVisible
    }
}
