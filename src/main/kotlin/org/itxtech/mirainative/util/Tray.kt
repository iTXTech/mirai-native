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

package org.itxtech.mirainative.util

import kotlinx.coroutines.launch
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.NativeDispatcher
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.imageio.ImageIO
import javax.swing.JOptionPane

object Tray {
    private var icon: TrayIcon? = null

    fun create() {
        if (SystemTray.isSupported()) {
            icon = TrayIcon(ImageIO.read(this.javaClass.classLoader.getResource("icon.jpg")), "Mirai Native 插件菜单")
            icon!!.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e?.clickCount == 2) {
                        JOptionPane.showMessageDialog(
                            null, "Mirai Native\nCopyright (C) 2020 iTX Technologies",
                            "关于 Mirai Native", JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                }
            })
            icon!!.popupMenu = PopupMenu()
            SystemTray.getSystemTray().add(icon)
        }
    }

    fun refresh() {
        if (icon != null) {
            val popupMenu = PopupMenu()
            val m = MenuItem("NPM")
            m.isEnabled = false
            popupMenu.add(m)
            val listPlugins = MenuItem("列出插件")
            popupMenu.add(listPlugins)

            popupMenu.addSeparator()
            val pl = MenuItem("插件")
            pl.isEnabled = false
            popupMenu.add(pl)
            MiraiNative.plugins.values.forEach { plugin ->
                if (plugin.loaded && plugin.pluginInfo != null && plugin.pluginInfo!!.menu.count() > 0) {
                    val menu = Menu(plugin.pluginInfo!!.name)
                    popupMenu.add(menu)
                    plugin.pluginInfo!!.menu.forEach { m ->
                        val item = MenuItem(m.name)
                        item.addActionListener {
                            MiraiNative.launch(NativeDispatcher) {
                                MiraiNative.bridge.callIntMethod(plugin.id, m.function)
                            }
                        }
                        menu.add(item)
                    }
                }
            }
            icon!!.popupMenu = popupMenu
        }
    }
}
