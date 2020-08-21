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
import org.itxtech.mirainative.manager.CacheManager
import java.nio.charset.Charset

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
    external fun shutdown(): Int

    @JvmStatic
    external fun loadNativePlugin(file: ByteArray, id: Int): Int

    @JvmStatic
    external fun freeNativePlugin(id: Int): Int

    @JvmStatic
    external fun pEvPrivateMessage(
        pluginId: Int,
        method: ByteArray,
        subType: Int,
        msgId: Int,
        fromAccount: Long,
        msg: ByteArray,
        font: Int
    ): Int

    @JvmStatic
    external fun pEvGroupMessage(
        pluginId: Int,
        method: ByteArray,
        subType: Int,
        msgId: Int,
        fromGroup: Long,
        fromAccount: Long,
        fromAnonymous: ByteArray,
        msg: ByteArray,
        font: Int
    ): Int

    @JvmStatic
    external fun pEvGroupAdmin(
        pluginId: Int,
        method: ByteArray,
        subType: Int,
        time: Int,
        fromGroup: Long,
        beingOperateAccount: Long
    ): Int

    @JvmStatic
    external fun pEvGroupMember(
        pluginId: Int,
        method: ByteArray,
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        beingOperateAccount: Long
    ): Int

    @JvmStatic
    external fun pEvGroupBan(
        pluginId: Int,
        method: ByteArray,
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
        method: ByteArray,
        subType: Int,
        time: Int,
        fromGroup: Long,
        fromAccount: Long,
        msg: ByteArray,
        flag: ByteArray
    ): Int

    @JvmStatic
    external fun pEvRequestAddFriend(
        pluginId: Int,
        method: ByteArray,
        subType: Int,
        time: Int,
        fromAccount: Long,
        msg: ByteArray,
        flag: ByteArray
    ): Int

    @JvmStatic
    external fun pEvFriendAdd(pluginId: Int, method: ByteArray, subType: Int, time: Int, fromAccount: Long): Int

    @JvmStatic
    external fun callIntMethod(pluginId: Int, method: ByteArray): Int

    @JvmStatic
    external fun callStringMethod(pluginId: Int, method: ByteArray): ByteArray

    @JvmStatic
    external fun processMessage()

    // Bridge

    @JvmStatic
    fun sendPrivateMessage(pluginId: Int, account: Long, msg: ByteArray): Int {
        return MiraiBridge.sendPrivateMessage(pluginId, account, msg.fromNative())
    }

    @JvmStatic
    fun sendGroupMessage(pluginId: Int, group: Long, msg: ByteArray): Int {
        return MiraiBridge.sendGroupMessage(pluginId, group, msg.fromNative())
    }

    @JvmStatic
    fun addLog(pluginId: Int, priority: Int, type: ByteArray, content: ByteArray) {
        MiraiBridge.addLog(pluginId, priority, type.fromNative(), content.fromNative())
    }

    @JvmStatic
    fun getPluginDataDir(pluginId: Int): ByteArray {
        return MiraiBridge.getPluginDataDir(pluginId).toNative()
    }

    @JvmStatic
    fun getLoginQQ(pluginId: Int): Long {
        return MiraiBridge.getLoginQQ(pluginId)
    }

    @JvmStatic
    fun getLoginNick(pluginId: Int): ByteArray {
        return MiraiBridge.getLoginNick(pluginId).toNative()
    }

    @JvmStatic
    fun setGroupBan(pluginId: Int, group: Long, member: Long, duration: Long): Int {
        return MiraiBridge.setGroupBan(pluginId, group, member, duration.toInt())
    }

    @JvmStatic
    fun setGroupCard(pluginId: Int, group: Long, member: Long, card: ByteArray): Int {
        return MiraiBridge.setGroupCard(pluginId, group, member, card.fromNative())
    }

    @JvmStatic
    fun setGroupKick(pluginId: Int, group: Long, member: Long, reject: Boolean): Int {
        return MiraiBridge.setGroupKick(pluginId, group, member)
    }

    @JvmStatic
    fun setGroupLeave(pluginId: Int, group: Long, dismiss: Boolean): Int {
        return MiraiBridge.setGroupLeave(pluginId, group)
    }

    @JvmStatic
    fun setGroupSpecialTitle(pluginId: Int, group: Long, member: Long, title: ByteArray, duration: Long): Int {
        return MiraiBridge.setGroupSpecialTitle(pluginId, group, member, title.fromNative(), duration)
    }

    @JvmStatic
    fun setGroupWholeBan(pluginId: Int, group: Long, enable: Boolean): Int {
        return MiraiBridge.setGroupWholeBan(pluginId, group, enable)
    }

    @JvmStatic
    fun recallMsg(pluginId: Int, msgId: Long): Int {
        return if (CacheManager.recall(msgId.toInt())) 0 else -1
    }

    @JvmStatic
    fun getFriendList(pluginId: Int, reserved: Boolean): ByteArray {
        return MiraiBridge.getFriendList(pluginId).toNative()
    }

    @JvmStatic
    fun getGroupInfo(pluginId: Int, groupId: Long, cache: Boolean): ByteArray {
        return MiraiBridge.getGroupInfo(pluginId, groupId).toNative()
    }

    @JvmStatic
    fun getGroupList(pluginId: Int): ByteArray {
        return MiraiBridge.getGroupList(pluginId).toNative()
    }

    @JvmStatic
    fun getGroupMemberInfo(pluginId: Int, group: Long, member: Long, cache: Boolean): ByteArray {
        return MiraiBridge.getGroupMemberInfo(pluginId, group, member).toNative()
    }

    @JvmStatic
    fun getGroupMemberList(pluginId: Int, group: Long): ByteArray {
        return MiraiBridge.getGroupMemberList(pluginId, group).toNative()
    }

    @JvmStatic
    fun setGroupAddRequest(
        pluginId: Int,
        requestId: ByteArray,
        reqType: Int,
        fbType: Int,
        reason: ByteArray
    ): Int {
        return MiraiBridge.setGroupAddRequest(pluginId, requestId.fromNative(), reqType, fbType, reason.fromNative())
    }

    @JvmStatic
    fun setFriendAddRequest(pluginId: Int, requestId: ByteArray, type: Int, remark: ByteArray): Int {
        return MiraiBridge.setFriendAddRequest(pluginId, requestId.fromNative(), type, remark.fromNative())
    }

    @JvmStatic
    fun getStrangerInfo(pluginId: Int, account: Long, cache: Boolean): ByteArray {
        return MiraiBridge.getStrangerInfo(pluginId, account).toNative()
    }

    @JvmStatic
    fun getImage(pluginId: Int, image: ByteArray): ByteArray {
        return MiraiBridge.getImage(pluginId, image.fromNative()).toNative()
    }

    @JvmStatic
    fun getRecord(pluginId: Int, file: ByteArray, format: ByteArray): ByteArray {
        return MiraiBridge.getRecord(pluginId, file.fromNative(), format.fromNative()).toNative()
    }


    // Placeholder methods which mirai hasn't supported yet

    @JvmStatic
    fun setGroupAnonymous(pluginId: Int, group: Long, enable: Boolean): Int {
        return 0
    }

    @JvmStatic
    fun setGroupAdmin(pluginId: Int, group: Long, account: Long, admin: Boolean): Int {
        //true => set, false => revoke
        return 0
    }

    @JvmStatic
    fun setGroupAnonymousBan(pluginId: Int, group: Long, id: ByteArray, duration: Long): Int {
        return 0
    }

    // Wont' Implement

    @JvmStatic
    fun sendLike(pluginId: Int, account: Long, times: Int): Int {
        return 0
    }

    @JvmStatic
    fun getCookies(pluginId: Int, domain: ByteArray): ByteArray {
        return "".toNative()
    }

    @JvmStatic
    fun getCsrfToken(pluginId: Int): ByteArray {
        return "".toNative()
    }

    @JvmStatic
    fun sendDiscussMessage(pluginId: Int, group: Long, msg: ByteArray): Int {
        return 0
    }

    @JvmStatic
    fun setDiscussLeave(pluginId: Int, group: Long): Int {
        return 0
    }

    // Mirai Unique Methods

    @JvmStatic
    fun quoteMessage(pluginId: Int, msgId: Int, msg: ByteArray): Int {
        return MiraiBridge.quoteMessage(pluginId, msgId, msg.fromNative())
    }

    @JvmStatic
    fun forwardMessage(pluginId: Int, type: Int, id: Long, strategy: ByteArray, msg: ByteArray): Int {
        return MiraiBridge.forwardMessage(pluginId, type, id, strategy.fromNative(), msg.fromNative())
    }
}

fun String.toNative() = toByteArray(Charset.forName("GB18030"))

fun ByteArray.fromNative() = String(this, Charset.forName("GB18030"))
