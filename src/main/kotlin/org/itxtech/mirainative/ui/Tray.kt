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

import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.toNative
import org.itxtech.mirainative.util.NpmHelper
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.imageio.ImageIO
import javax.swing.JOptionPane

object Tray {
    private var icon: TrayIcon? = null

    fun create() {
        try {
            if (SystemTray.isSupported()) {
                icon = TrayIcon(ImageIO.read(MiraiNative.getResourceAsStream("icon.jpg")), "Mirai Native 插件菜单").apply {
                    addMouseListener(object : MouseAdapter() {
                        override fun mouseClicked(e: MouseEvent?) {
                            if (e?.button == 1 && !FloatingWindow.isVisible()) {
                                FloatingWindow.toggle()
                            }
                        }
                    })
                    popupMenu = PopupMenu()
                    SystemTray.getSystemTray().add(this)
                }
            }
        } catch (e: Throwable) {
            MiraiNative.logger.error(e)
        }
    }

    fun close() {
        if (icon != null) {
            SystemTray.getSystemTray().remove(icon)
        }
    }

    fun update() {
        if (icon != null) {
            val popupMenu = PopupMenu()
            icon!!.popupMenu = popupMenu

            val fw = MenuItem(if (FloatingWindow.isVisible()) "隐藏悬浮窗" else "显示悬浮窗")
            fw.addActionListener {
                FloatingWindow.toggle()
                update()
            }
            popupMenu.add(fw)

            popupMenu.addSeparator()

            val m = MenuItem("NPM")
            m.isEnabled = false
            popupMenu.add(m)

            val npm = Menu("插件管理")
            popupMenu.add(npm)

            val load = MenuItem("加载 DLL")
            load.addActionListener {
                val file = JOptionPane.showInputDialog("请输入位于 MiraiNative 目录下的 DLL文件名。")
                if (file != null) {
                    if (!PluginManager.loadPluginFromFile(file)) {
                        JOptionPane.showMessageDialog(null, "加载 DLL 文件出错 “$file”。", "错误", JOptionPane.ERROR_MESSAGE)
                    }
                }
            }
            popupMenu.add(load)

            popupMenu.addSeparator()

            val pl = MenuItem("插件菜单")
            pl.isEnabled = false
            popupMenu.add(pl)

            PluginManager.plugins.values.forEach { plugin ->
                if (plugin.loaded) {
                    val p = Menu(NpmHelper.name(plugin))
                    npm.add(p)

                    val ver =
                        MenuItem("Id：" + plugin.id + " 版本：" + if (plugin.pluginInfo != null) plugin.pluginInfo!!.version else "未知")
                    ver.isEnabled = false
                    p.add(ver)

                    val status = MenuItem(NpmHelper.state(plugin))
                    status.isEnabled = false
                    p.add(status)

                    if (plugin.entries.isNotEmpty()) {
                        //悬浮窗状态项
                        val fwes = Menu("状态")
                        plugin.entries.forEach { e ->
                            val entry = MenuItem(e.status.name + "：" + if (e.visible) "显示" else "隐藏")
                            entry.addActionListener {
                                e.visible = !e.visible
                                update()
                            }
                            fwes.add(entry)
                        }
                        p.add(fwes)
                    }

                    val summary = MenuItem("信息")
                    summary.addActionListener {
                        JOptionPane.showMessageDialog(
                            null,
                            NpmHelper.summary(plugin),
                            "插件信息 " + NpmHelper.name(plugin),
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    }
                    p.add(summary)

                    val unload = MenuItem("卸载")
                    unload.addActionListener {
                        PluginManager.unloadPlugin(plugin)
                    }
                    p.add(unload)

                    val en = MenuItem(if (plugin.enabled) "禁用" else "启用")
                    en.isEnabled = MiraiNative.botOnline
                    en.addActionListener {
                        if (plugin.enabled) {
                            PluginManager.disablePlugin(plugin)
                        } else {
                            PluginManager.enablePlugin(plugin)
                        }
                    }
                    p.add(en)

                    if (plugin.pluginInfo != null && plugin.pluginInfo!!.menu.count() > 0) {
                        val menu = Menu(plugin.pluginInfo!!.name)
                        popupMenu.add(menu)
                        plugin.pluginInfo!!.menu.forEach { m ->
                            val item = MenuItem(m.name)
                            item.addActionListener {
                                MiraiNative.nativeLaunch {
                                    Bridge.callIntMethod(plugin.id, m.function.toNative())
                                }
                            }
                            menu.add(item)
                        }
                    }
                }
            }

            popupMenu.addSeparator()

            val about = MenuItem("关于")
            popupMenu.add(about)
            about.addActionListener {
                JOptionPane.showMessageDialog(
                    null, "Mirai Native " + MiraiNative.getVersion() +
                            "\nhttps://github.com/iTXTech/mirai-native" +
                            "\n遵循 AGPL-3.0 协议开源" +
                            "\n作者 PeratX@iTXTech.org" +
                            "\n“流泪猫猫头”图标版权所有" +
                            "\n版权所有 (C) 2020 iTX Technologies",
                    "关于 Mirai Native", JOptionPane.INFORMATION_MESSAGE
                )
            }

        }
    }
}
