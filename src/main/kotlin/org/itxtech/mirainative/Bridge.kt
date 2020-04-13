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

@file:Suppress("UNUSED_PARAMETER", "unused")

package org.itxtech.mirainative

import org.itxtech.mirainative.bridge.MiraiBridge
import org.itxtech.mirainative.bridge.MiraiBridge.getFriendList
import org.itxtech.mirainative.bridge.MiraiBridge.getGroupInfo
import org.itxtech.mirainative.bridge.MiraiBridge.getGroupList
import org.itxtech.mirainative.bridge.MiraiBridge.getGroupMemberInfo
import org.itxtech.mirainative.bridge.MiraiBridge.getGroupMemberList
import org.itxtech.mirainative.bridge.MiraiBridge.getLoginNick
import org.itxtech.mirainative.bridge.MiraiBridge.getLoginQQ
import org.itxtech.mirainative.bridge.MiraiBridge.getStrangerInfo
import org.itxtech.mirainative.bridge.MiraiBridge.quoteMessage
import org.itxtech.mirainative.bridge.MiraiBridge.sendGroupMessage
import org.itxtech.mirainative.bridge.MiraiBridge.sendPrivateMessage
import org.itxtech.mirainative.bridge.MiraiBridge.setFriendAddRequest
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupAddRequest
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupBan
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupCard
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupKick
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupLeave
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupSpecialTitle
import org.itxtech.mirainative.bridge.MiraiBridge.setGroupWholeBan
import org.itxtech.mirainative.manager.CacheManager.recall

object Bridge {
    const val PRI_MSG_SUBTYPE_FRIEND = 11
    const val PRI_MSG_SUBTYPE_ONLINE_STATE = 1
    const val PRI_MSG_SUBTYPE_GROUP = 2
    const val PRI_MSG_SUBTYPE_DISCUSS = 3

    const val PERM_SUBTYPE_CANCEL_ADMIN = 1
    const val PERM_SUBTYPE_SET_ADMIN = 2

    const val MEMBER_LEAVE_QUIT = 1
    const val MEMBER_LEAVE_KICK = 2

    const val MEMBER_JOIN_PERMITTED = 1
    const val MEMBER_JOIN_INVITED_BY_ADMIN = 2

    const val REQUEST_GROUP_APPLY = 1 //他人申请
    const val REQUEST_GROUP_INVITED = 2 //受邀

    const val GROUP_UNMUTE = 1
    const val GROUP_MUTE = 2

    // Native

    @JvmStatic
    external fun loadNativePlugin(file: String, id: Int): Int

    @JvmStatic
    external fun freeNativePlugin(id: Int): Int

    @JvmStatic
    external fun pEvPrivateMessage(
        pluginId: Int,
        name: String,
        subType: Int,
        msgId: Int,
        fromAccount: Long,
        msg: String,
        font: Int
    ): Int

    @JvmStatic
    external fun pEvGroupMessage(
        pluginId: Int,
        name: String,
        subType: Int,
        msgId: Int,
        fromGroup: Long,
        fromAccount: Long,
        fromAnonymous: String,
        msg: String,
        font: Int
    ): Int

    @JvmStatic
    external fun pEvGroupAdmin(
        pluginId: Int,
        name: String,
        subType: Int,
        time: Int,
        fromGroup: Long,
        beingOperateAccount: Long
    ): Int

    @JvmStatic
    external fun pEvGroupMember(
        pluginId: Int,
        name: String,
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long
    ): Int

    @JvmStatic
    external fun pEvGroupBan(
        pluginId: Int,
        name: String,
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long,
        duration: Long
    ): Int

    @JvmStatic
    external fun pEvRequestAddGroup(
        pluginId: Int,
        name: String,
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        msg: String,
        flag: String
    ): Int

    @JvmStatic
    external fun pEvRequestAddFriend(
        pluginId: Int,
        name: String,
        subType: Int,
        time: Int,
        fromAccount: Long,
        msg: String,
        flag: String
    ): Int

    @JvmStatic
    external fun pEvFriendAdd(pluginId: Int, name: String, subType: Int, time: Int, fromAccount: Long): Int

    @JvmStatic
    external fun callIntMethod(pluginId: Int, name: String): Int

    @JvmStatic
    external fun callStringMethod(pluginId: Int, name: String): String

    @JvmStatic
    external fun processMessage()

    // Bridge

    @JvmStatic
    fun sendPrivateMessage(pluginId: Int, account: Long, msg: String): Int {
        return sendPrivateMessage(account, msg)
    }

    @JvmStatic
    fun sendGroupMessage(pluginId: Int, group: Long, msg: String): Int {
        return sendGroupMessage(group, msg)
    }

    @JvmStatic
    fun addLog(pluginId: Int, priority: Int, type: String, content: String) {
        MiraiBridge.addLog(pluginId, priority, type, content)
    }

    @JvmStatic
    fun getPluginDataDir(pluginId: Int): String {
        return MiraiBridge.getPluginDataDir(pluginId)
    }

    @JvmStatic
    fun getLoginQQ(pluginId: Int): Long {
        return getLoginQQ()
    }

    @JvmStatic
    fun getLoginNick(pluginId: Int): String {
        return getLoginNick()
    }

    @JvmStatic
    fun setGroupBan(pluginId: Int, group: Long, member: Long, duration: Long): Int {
        return setGroupBan(group, member, duration.toInt())
    }

    @JvmStatic
    fun setGroupCard(pluginId: Int, group: Long, member: Long, card: String): Int {
        return setGroupCard(group, member, card)
    }

    @JvmStatic
    fun setGroupKick(pluginId: Int, group: Long, member: Long, reject: Boolean): Int {
        return setGroupKick(group, member)
    }

    @JvmStatic
    fun setGroupLeave(pluginId: Int, group: Long, dismiss: Boolean): Int {
        return setGroupLeave(group)
    }

    @JvmStatic
    fun setGroupSpecialTitle(pluginId: Int, group: Long, member: Long, title: String, duration: Long): Int {
        return setGroupSpecialTitle(group, member, title, duration)
    }

    @JvmStatic
    fun setGroupWholeBan(pluginId: Int, group: Long, enable: Boolean): Int {
        return setGroupWholeBan(group, enable)
    }

    @JvmStatic
    fun recallMsg(pluginId: Int, msgId: Long): Int {
        return if (recall(java.lang.Long.valueOf(msgId).toInt())) 0 else -1
    }

    @JvmStatic
    fun getFriendList(pluginId: Int, reserved: Boolean): String {
        return getFriendList()
    }

    @JvmStatic
    fun getGroupInfo(pluginId: Int, groupId: Long, cache: Boolean): String {
        return getGroupInfo(groupId)
    }

    @JvmStatic
    fun getGroupList(pluginId: Int): String {
        return getGroupList()
    }

    @JvmStatic
    fun getGroupMemberInfo(pluginId: Int, group: Long, member: Long, cache: Boolean): String {
        return getGroupMemberInfo(group, member)
    }

    @JvmStatic
    fun getGroupMemberList(pluginId: Int, group: Long): String {
        return getGroupMemberList(group)
    }

    @JvmStatic
    fun setGroupAddRequest(
        pluginId: Int,
        requestId: String,
        reqType: Int,
        fbType: Int,
        reason: String
    ): Int {
        return setGroupAddRequest(requestId, reqType, fbType, reason)
    }

    @JvmStatic
    fun setFriendAddRequest(pluginId: Int, requestId: String, type: Int, remark: String): Int {
        return setFriendAddRequest(requestId, type, remark)
    }

    @JvmStatic
    fun getStrangerInfo(pluginId: Int, account: Long, cache: Boolean): String {
        return getStrangerInfo(account)
    }

    // Placeholder methods which mirai hasn't supported yet
    @JvmStatic
    fun setGroupAnonymous(pluginId: Int, group: Long, enable: Boolean): Int {
        return 0
    }

    @JvmStatic
    fun getImage(pluginId: Int, image: String): String {
        return ""
    }

    @JvmStatic
    fun getRecord(pluginId: Int, file: String, format: String): String {
        return ""
    }

    @JvmStatic
    fun sendDiscussMessage(pluginId: Int, group: Long, msg: String): Int {
        return 0
    }

    @JvmStatic
    fun setDiscussLeave(pluginId: Int, group: Long): Int {
        return 0
    }

    @JvmStatic
    fun setGroupAdmin(pluginId: Int, group: Long, account: Long, admin: Boolean): Int {
        //true => set, false => revoke
        return 0
    }

    @JvmStatic
    fun setGroupAnonymousBan(pluginId: Int, group: Long, id: String, duration: Long): Int {
        return 0
    }

    // Wont' Implement
    @JvmStatic
    fun sendLike(pluginId: Int, account: Long, times: Int): Int {
        return 0
    }

    @JvmStatic
    fun getCookies(pluginId: Int, domain: String): String {
        return ""
    }

    @JvmStatic
    fun getCsrfToken(pluginId: Int): String {
        return ""
    }

    // Mirai Unique Methods
    @JvmStatic
    fun quoteMessage(pluginId: Int, msgId: Int, msg: String): Int {
        return quoteMessage(msgId, msg)
    }
}
