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
            icon = TrayIcon(ImageIO.read(MiraiNative.getResources("icon.jpg")), "Mirai Native 插件菜单")
            icon!!.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    if (e?.clickCount == 2) {
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
            })
            icon!!.popupMenu = PopupMenu()
            SystemTray.getSystemTray().add(icon)
        }
    }

    fun refresh() {
        if (icon != null) {
            val popupMenu = PopupMenu()
            icon!!.popupMenu = popupMenu

            val m = MenuItem("NPM")
            m.isEnabled = false
            popupMenu.add(m)
            val npm = Menu("插件管理")
            popupMenu.add(npm)
            val load = MenuItem("加载 DLL")
            load.addActionListener {
                val file = JOptionPane.showInputDialog("请输入位于 MiraiNative 目录下的 DLL文件名")
                if (file != null) {
                    if (!MiraiNative.loadPluginFromFile(file)) {
                        JOptionPane.showMessageDialog(null, "加载 DLL 文件出错 “$file”。", "错误", JOptionPane.ERROR_MESSAGE)
                    }
                }
            }
            popupMenu.add(load)

            popupMenu.addSeparator()
            val pl = MenuItem("插件菜单")
            pl.isEnabled = false
            popupMenu.add(pl)

            MiraiNative.plugins.values.forEach { plugin ->
                if (plugin.loaded) {
                    val p = Menu(if (plugin.pluginInfo != null) plugin.pluginInfo!!.name else plugin.identifier)
                    npm.add(p)
                    val ver =
                        MenuItem("Id：" + plugin.id + " 版本：" + if (plugin.pluginInfo != null) plugin.pluginInfo!!.version else "未知")
                    ver.isEnabled = false
                    p.add(ver)
                    val status =
                        MenuItem("状态：" + (if (plugin.enabled) "已启用 " else "已禁用 ") + (if (plugin.loaded) "已加载" else "已卸载"))
                    status.isEnabled = false
                    p.add(status)
                    val unload = MenuItem("卸载")
                    unload.addActionListener {
                        MiraiNative.unloadPlugin(plugin)
                    }
                    p.add(unload)
                    val en = MenuItem(if (plugin.enabled) "禁用" else "启用")
                    en.isEnabled = MiraiNative.botOnline
                    en.addActionListener {
                        if (plugin.enabled) {
                            MiraiNative.disablePlugin(plugin)
                        } else {
                            MiraiNative.enablePlugin(plugin)
                        }
                    }
                    p.add(en)

                    if (plugin.pluginInfo != null && plugin.pluginInfo!!.menu.count() > 0) {
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
            }
        }
    }
}
