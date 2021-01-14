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

package org.itxtech.mirainative.plugin

import kotlinx.serialization.Serializable

@Serializable
data class PluginInfo(
    val ret: Int = 1,
    val apiver: Int,
    val name: String,
    val version: String = "",
    val version_id: Int = 0,
    val author: String = "",
    val description: String = "",
    val event: ArrayList<Event> = ArrayList(),
    val menu: ArrayList<Menu> = ArrayList(),
    val status: ArrayList<Status> = ArrayList(),
    val auth: IntArray = IntArray(0)
)

@Serializable
data class Event(
    val id: Int,
    val type: Int,
    val name: String,
    val function: String,
    val priority: Int,
    val regex: Regex? = null
) {
    companion object {
        const val EVENT_STARTUP = 1001
        const val EVENT_EXIT = 1002
        const val EVENT_ENABLE = 1003
        const val EVENT_DISABLE = 1004

        const val EVENT_PRI_MSG = 21
        const val EVENT_GROUP_MSG = 2
        const val EVENT_DISCUSS_MSG = 4

        const val EVENT_GROUP_UPLOAD = 11
        const val EVENT_GROUP_ADMIN = 101
        const val EVENT_GROUP_MEMBER_DEC = 102
        const val EVENT_GROUP_MEMBER_INC = 103
        const val EVENT_GROUP_BAN = 104

        const val EVENT_FRIEND_ADD = 105

        const val EVENT_REQUEST_FRIEND = 301
        const val EVENT_REQUEST_GROUP = 302
    }
}

@Serializable
data class Menu(
    val name: String,
    val function: String
)

@Serializable
data class Status(
    val id: Int,
    val name: String,
    val title: String,
    val function: String,
    val period: Int
)

@Serializable
data class Regex(
    val key: ArrayList<String>,
    val expression: ArrayList<String>
)

data class FloatingWindowEntry(val status: Status) {
    var data = ""
    var unit = ""
    var color = 0
    var visible = false
    var vaild = true
}
