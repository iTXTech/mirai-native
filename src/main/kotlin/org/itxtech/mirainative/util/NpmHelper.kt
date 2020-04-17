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

import org.itxtech.mirainative.plugin.NativePlugin

object NpmHelper {
    fun state(p: NativePlugin, h: Boolean = true): String {
        return (if (h) " 状态：" else "") + (if (p.enabled) "已启用 " else "已禁用 ") + (if (p.loaded) "已加载" else "已卸载")
    }

    fun name(p: NativePlugin): String {
        return if (p.pluginInfo != null) p.pluginInfo!!.name else p.identifier
    }

    fun summary(p: NativePlugin): String {
        val d = StringBuilder()
        val i = p.pluginInfo
        d.appendln("标识符：" + p.identifier)
        d.appendln("状态：" + state(p, false))
        if (i == null) {
            d.appendln("Id：" + p.id + " （JSON文件缺失）")
            d.appendln("CQ API：" + p.api)
        } else {
            d.appendln("Id：" + p.id)
            d.appendln("CQ API：" + p.api + " CQ API（JSON）：" + i.apiver)
            d.appendln("名称：" + i.name)
            d.appendln("版本：" + i.version + " 版本号：" + i.version_id)
            d.appendln("描述：" + i.description)
            d.appendln("作者：" + i.author)
            d.appendln("注册了 " + i.event.size + " 个事件")
            i.event.forEach { ev ->
                d.appendln("类型：" + ev.type + " 描述：" + ev.name + " 方法名：" + ev.function)
            }
            d.appendln("注册了 " + i.status.size + " 个悬浮窗项目")
            i.status.forEach { s ->
                d.appendln("名称：" + s.name + " 标题：" + s.title + " 方法名：" + s.function)
            }
            d.appendln("注册了 " + i.menu.size + " 个菜单入口")
            i.menu.forEach { m ->
                d.appendln("名称：" + m.name + " 方法名：" + m.function)
            }
        }
        return d.toString()
    }
}
