package org.itxtech.mirainative;

import org.itxtech.mirainative.bridge.MiraiBridge;
import org.itxtech.mirainative.message.CacheManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
public class Bridge {
    public static final int PRI_MSG_SUBTYPE_FRIEND = 11;
    public static final int PRI_MSG_SUBTYPE_ONLINE_STATE = 1;
    public static final int PRI_MSG_SUBTYPE_GROUP = 2;
    public static final int PRI_MSG_SUBTYPE_DISCUSS = 3;

    public static final int PERM_SUBTYPE_CANCEL_ADMIN = 1;
    public static final int PERM_SUBTYPE_SET_ADMIN = 2;

    public static final int MEMBER_LEAVE_QUIT = 1;
    public static final int MEMBER_LEAVE_KICK = 2;

    public static final int MEMBER_JOIN_PERMITTED = 1;
    public static final int MEMBER_JOIN_INVITED_BY_ADMIN = 2;

    public static final int REQUEST_GROUP_APPLY = 1; //他人申请
    public static final int REQUEST_GROUP_INVITED = 2; //受邀

    public static final int GROUP_UNMUTE = 1;
    public static final int GROUP_MUTE = 2;

    // Native

    public static native int loadNativePlugin(String file, int id);

    public static native int freeNativePlugin(int id);

    public static native int pEvPrivateMessage(int pluginId, String name, int subType, int msgId, long fromAccount, String msg, int font);

    public static native int pEvGroupMessage(int pluginId, String name, int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font);

    public static native int pEvGroupAdmin(int pluginId, String name, int subType, int time, long fromGroup, long beingOperateAccount);

    public static native int pEvGroupMember(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount);

    public static native int pEvGroupBan(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration);

    public static native int pEvRequestAddGroup(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, String msg, String flag);

    public static native int pEvRequestAddFriend(int pluginId, String name, int subType, int time, long fromAccount, String msg, String flag);

    public static native int pEvFriendAdd(int pluginId, String name, int subType, int time, long fromAccount);

    public static native int callIntMethod(int pluginId, String name);

    public static native String callStringMethod(int pluginId, String name);

    public static native void processMessage();

    // Bridge

    @NativeBridgeMethod
    public static int sendPrivateMessage(int pluginId, long account, String msg) {
        return MiraiBridge.sendPrivateMessage(account, msg);
    }

    @NativeBridgeMethod
    public static int sendGroupMessage(int pluginId, long group, String msg) {
        return MiraiBridge.sendGroupMessage(group, msg);
    }

    @NativeBridgeMethod
    public static void addLog(int pluginId, int priority, String type, String content) {
        MiraiBridge.addLog(pluginId, priority, type, content);
    }

    @NativeBridgeMethod
    public static String getPluginDataDir(int pluginId) {
        return MiraiBridge.getPluginDataDir(pluginId);
    }

    @NativeBridgeMethod
    public static long getLoginQQ(int pluginId) {
        return MiraiBridge.getLoginQQ();
    }

    @NativeBridgeMethod
    public static String getLoginNick(int pluginId) {
        return MiraiBridge.getLoginNick();
    }

    @NativeBridgeMethod
    public static int setGroupBan(int pluginId, long group, long member, long duration) {
        return MiraiBridge.setGroupBan(group, member, (int) duration);
    }

    @NativeBridgeMethod
    public static int setGroupCard(int pluginId, long group, long member, String card) {
        return MiraiBridge.setGroupCard(group, member, card);
    }

    @NativeBridgeMethod
    public static int setGroupKick(int pluginId, long group, long member, boolean reject) {
        return MiraiBridge.setGroupKick(group, member);
    }

    @NativeBridgeMethod
    public static int setGroupLeave(int pluginId, long group, boolean dismiss) {
        return MiraiBridge.setGroupLeave(group);
    }

    @NativeBridgeMethod
    public static int setGroupSpecialTitle(int pluginId, long group, long member, String title, long duration) {
        return MiraiBridge.setGroupSpecialTitle(group, member, title, duration);
    }

    @NativeBridgeMethod
    public static int setGroupWholeBan(int pluginId, long group, boolean enable) {
        return MiraiBridge.setGroupWholeBan(group, enable);
    }

    @NativeBridgeMethod
    public static int recallMsg(int pluginId, long msgId) {
        return CacheManager.INSTANCE.recall(Long.valueOf(msgId).intValue()) ? 0 : -1;
    }

    @NativeBridgeMethod
    public static String getFriendList(int pluginId, boolean reserved) {
        return MiraiBridge.getFriendList();
    }

    @NativeBridgeMethod
    public static String getGroupInfo(int pluginId, long groupId, boolean cache) {
        return MiraiBridge.getGroupInfo(groupId);
    }

    @NativeBridgeMethod
    public static String getGroupList(int pluginId) {
        return MiraiBridge.getGroupList();
    }

    @NativeBridgeMethod
    public static String getGroupMemberInfo(int pluginId, long group, long member, boolean cache) {
        return MiraiBridge.getGroupMemberInfo(group, member);
    }

    @NativeBridgeMethod
    public static String getGroupMemberList(int pluginId, long group) {
        return MiraiBridge.getGroupMemberList(group);
    }

    @NativeBridgeMethod
    public static int setGroupAddRequest(int pluginId, String requestId, int reqType, int fbType, String reason) {
        return MiraiBridge.setGroupAddRequest(requestId, reqType, fbType, reason);
    }

    @NativeBridgeMethod
    public static int setFriendAddRequest(int pluginId, String requestId, int type, String remark) {
        return MiraiBridge.setFriendAddRequest(requestId, type, remark);
    }

    @NativeBridgeMethod
    public static String getStrangerInfo(int pluginId, long account, boolean cache) {
        return MiraiBridge.getStrangerInfo(account);
    }

    // Placeholder methods which mirai hasn't supported yet

    @NativeBridgeMethod
    public static int setGroupAnonymous(int pluginId, long group, boolean enable) {
        return 0;
    }

    @NativeBridgeMethod
    public static String getImage(int pluginId, String image) {
        return "";
    }

    @NativeBridgeMethod
    public static String getRecord(int pluginId, String file, String format) {
        return "";
    }

    @NativeBridgeMethod
    public static int sendDiscussMessage(int pluginId, long group, String msg) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setDiscussLeave(int pluginId, long group) {
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupAdmin(int pluginId, long group, long account, boolean admin) {
        //true => set, false => revoke
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupAnonymousBan(int pluginId, long group, String id, long duration) {
        return 0;
    }

    // Wont' Implement

    @NativeBridgeMethod
    public static int sendLike(int pluginId, long account, int times) {
        return 0;
    }

    @NativeBridgeMethod
    public static String getCookies(int pluginId, String domain) {
        return "";
    }

    @NativeBridgeMethod
    public static String getCsrfToken(int pluginId) {
        return "";
    }

    // Mirai Unique Methods

    @NativeBridgeMethod
    public static int quoteMessage(int pluginId, int msgId, String msg) {
        return MiraiBridge.quoteMessage(msgId, msg);
    }

    // Annotation

    /**
     * Indicates the method is called from native code
     */
    @Retention(value = RetentionPolicy.SOURCE)
    @interface NativeBridgeMethod {
    }
}
