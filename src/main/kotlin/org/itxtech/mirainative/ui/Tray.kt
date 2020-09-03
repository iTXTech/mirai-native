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

import kotlinx.coroutines.launch
import org.itxtech.mirainative.Bridge
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.manager.PluginManager
import org.itxtech.mirainative.toNative
import org.itxtech.mirainative.util.ConfigMan
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
                update()
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
            icon!!.popupMenu = PopupMenu().apply {
                add(MenuItem().apply {
                    fun lbl() = if (FloatingWindow.isVisible()) "隐藏悬浮窗" else "显示悬浮窗"
                    label = lbl()
                    addActionListener {
                        FloatingWindow.toggle()
                        label = lbl()
                    }
                })

                addSeparator()

                add(MenuItem("NPM").apply { isEnabled = false })

                val npm = Menu("插件管理")
                add(npm)

                add(MenuItem("加载 DLL").apply {
                    addActionListener {
                        val file = JOptionPane.showInputDialog("请输入位于 MiraiNative 目录下的 DLL文件名。")
                        if (file != null) {
                            if (!PluginManager.readPluginFromFile(file)) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "加载 DLL 文件出错 “$file”。",
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE
                                )
                            }
                        }
                    }
                })

                addSeparator()

                add(MenuItem("开发").apply { isEnabled = false })
                add(MenuItem().apply {
                    fun lbl() = (if (ConfigMan.config.verboseNativeApiLog) "禁用" else "启用") + "输出插件调用日志"
                    label = lbl()
                    addActionListener {
                        ConfigMan.config.verboseNativeApiLog = !ConfigMan.config.verboseNativeApiLog
                        label = lbl()
                    }
                })

                addSeparator()

                add(MenuItem("插件菜单").apply { isEnabled = false })

                PluginManager.plugins.values.forEach { plugin ->
                    if (plugin.loaded) {
                        val p = Menu(NpmHelper.name(plugin))
                        npm.add(p)

                        p.add(MenuItem("Id：" + plugin.id + " 版本：" + if (plugin.pluginInfo != null) plugin.pluginInfo!!.version else "未知").apply {
                            isEnabled = false
                        })

                        p.add(MenuItem(NpmHelper.state(plugin)).apply { isEnabled = false })

                        if (plugin.entries.isNotEmpty()) {
                            p.add(Menu("状态").apply {
                                plugin.entries.forEach { e ->
                                    add(MenuItem().apply {
                                        fun lbl() = e.status.name + "：" + if (e.visible) "显示" else "隐藏"
                                        label = lbl()
                                        addActionListener {
                                            e.visible = !e.visible
                                            label = lbl()
                                        }
                                    })
                                }
                            })
                        }

                        p.add(MenuItem("信息").apply {
                            addActionListener {
                                JOptionPane.showMessageDialog(
                                    null,
                                    NpmHelper.summary(plugin),
                                    "插件信息 " + NpmHelper.name(plugin),
                                    JOptionPane.INFORMATION_MESSAGE
                                )
                            }
                        })

                        p.add(MenuItem("卸载").apply {
                            addActionListener {
                                MiraiNative.nativeLaunch {
                                    PluginManager.unloadPlugin(plugin)
                                }
                            }
                        })


                        if (plugin.reloadable) {
                            p.add(MenuItem("重新加载").apply {
                                addActionListener {
                                    MiraiNative.nativeLaunch {
                                        PluginManager.reloadPlugin(plugin)
                                    }
                                }
                            })
                        }

                        p.add(MenuItem(if (plugin.enabled) "禁用" else "启用").apply {
                            isEnabled = MiraiNative.botOnline
                            addActionListener {
                                MiraiNative.nativeLaunch {
                                    if (plugin.enabled) {
                                        PluginManager.disablePlugin(plugin)
                                    } else {
                                        PluginManager.enablePlugin(plugin)
                                    }
                                }
                            }
                        })

                        if (plugin.pluginInfo != null && plugin.pluginInfo!!.menu.count() > 0) {
                            add(Menu(plugin.pluginInfo!!.name).apply {
                                plugin.pluginInfo!!.menu.forEach { m ->
                                    val item = MenuItem(m.name)
                                    item.addActionListener {
                                        MiraiNative.launch(MiraiNative.menuDispatcher) {
                                            Bridge.callIntMethod(plugin.id, m.function.toNative())
                                        }
                                    }
                                    add(item)
                                }
                            })
                        }
                    }
                }

                addSeparator()

                add(MenuItem("关于").apply {
                    addActionListener {
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
                })
            }
        }
    }
}
