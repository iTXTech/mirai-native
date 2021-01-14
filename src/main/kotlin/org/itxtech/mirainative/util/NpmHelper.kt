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

import org.itxtech.mirainative.plugin.NativePlugin

object NpmHelper {
    fun state(p: NativePlugin, h: Boolean = true) =
        (if (h) "状态：" else "") + (if (p.enabled) "已启用 " else "已禁用 ") + (if (p.loaded) "已加载" else "已卸载")

    fun name(p: NativePlugin) = if (p.pluginInfo != null) p.pluginInfo!!.name else p.identifier

    fun summary(p: NativePlugin) = buildString {
        val i = p.pluginInfo
        appendLine("标识符：${p.identifier}")
        appendLine("状态：${state(p, false)}")
        if (i == null) {
            appendLine("Id：${p.id} （插件信息缺失）")
            appendLine("CQ API：${p.api}")
        } else {
            appendLine("Id：${p.id}")
            appendLine("CQ API：${p.api} CQ API（JSON）：${i.apiver}")
            appendLine("名称：${i.name}")
            appendLine("版本：${i.version} 版本号：${i.version_id}")
            appendLine("描述：${i.description}")
            appendLine("作者：${i.author}")
            appendLine("注册了 ${i.event.size} 个事件")
            i.event.forEach { ev ->
                appendLine("类型：${ev.type} 描述：${ev.name} 方法名：${ev.function}")
            }
            appendLine("注册了 ${i.status.size} 个悬浮窗项目")
            i.status.forEach { s ->
                appendLine("名称：${s.name} 标题：${s.title} 方法名：${s.function}")
            }
            appendLine("注册了 ${i.menu.size} 个菜单入口")
            i.menu.forEach { m ->
                appendLine("名称：${m.name} 方法名：${m.function}")
            }
        }
    }
}
