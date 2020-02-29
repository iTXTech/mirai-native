package org.itxtech.mirainative;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.utils.MiraiLogger;
import org.itxtech.mirainative.plugin.Event;
import org.itxtech.mirainative.plugin.NativePlugin;
import org.itxtech.mirainative.plugin.PluginInfo;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

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
class Bridge {
    public static final int PRI_MSG_SUBTYPE_FRIEND = 11;

    public static final int PERM_SUBTYPE_CANCEL_ADMIN = 1;
    public static final int PERM_SUBTYPE_SET_ADMIN = 2;

    public static final int MEMBER_LEAVE_QUIT = 1;
    public static final int MEMBER_LEAVE_KICK = 2;

    public static final int GROUP_UNMUTE = 1;
    public static final int GROUP_MUTE = 2;

    // Plugin
    public int loadPlugin(NativePlugin plugin) {
        int code = loadNativePlugin(plugin.getFile().getAbsolutePath().replace("\\", "\\\\"), plugin.getId());
        if (plugin.getPluginInfo() != null) {
            PluginInfo info = plugin.getPluginInfo();
            getLogger().info("Native Plugin (w json) " + info.getName() + " loaded with code " + code);
        } else {
            getLogger().info("Native Plugin (w/o json) " + plugin.getFile().getName() + " loaded with code " + code);
        }
        return code;
    }

    public void disablePlugin(NativePlugin plugin) {
        if (plugin.getEnabled()) {
            plugin.setEnabled(false);
            if (plugin.shouldCallEvent(Event.EVENT_DISABLE, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_DISABLE, "_eventDisable"));
            }
        }
    }

    public void enablePlugin(NativePlugin plugin) {
        if (!plugin.getEnabled()) {
            plugin.setEnabled(true);
            if (plugin.shouldCallEvent(Event.EVENT_ENABLE, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_ENABLE, "_eventEnable"));
            }
        }
    }

    // Events

    public void eventStartup() {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_STARTUP, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_STARTUP, "_eventStartup"));
            }
        }
    }

    public void eventExit() {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_EXIT, true)) {
                callIntMethod(plugin.getId(), plugin.getEventOrDefault(Event.EVENT_EXIT, "_eventExit"));
            }
        }
    }

    public void eventEnable() {
        for (NativePlugin plugin : getPlugins().values()) {
            enablePlugin(plugin);
        }
    }

    public void eventDisable() {
        for (NativePlugin plugin : getPlugins().values()) {
            disablePlugin(plugin);
        }
    }

    public void eventPrivateMessage(int subType, int msgId, long fromAccount, String msg, int font) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_PRI_MSG) && pEvPrivateMessage(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_PRI_MSG, "_eventPrivateMsg"),
                    subType, msgId, fromAccount, plugin.processMessage(Event.EVENT_PRI_MSG, msg), font) == 1) {
                break;
            }
        }
    }

    public void eventGroupMessage(int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MSG) && pEvGroupMessage(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MSG, "_eventGroupMsg"),
                    subType, msgId, fromGroup, fromAccount, fromAnonymous, plugin.processMessage(Event.EVENT_GROUP_MSG, msg), font) == 1) {
                break;
            }
        }
    }

    public void eventGroupAdmin(int subType, int time, long fromGroup, long beingOperateAccount) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_ADMIN) && pEvGroupAdmin(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_ADMIN, "_eventSystem_GroupAdmin"),
                    subType, time, fromGroup, beingOperateAccount) == 1) {
                break;
            }
        }
    }

    public void eventGroupMemberLeave(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_MEMBER_DEC) && pEvGroupMemberLeave(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_MEMBER_DEC, "_eventSystem_GroupMemberDecrease"),
                    subType, time, fromGroup, fromAccount, beingOperateAccount) == 1) {
                break;
            }
        }
    }

    public void eventGroupBan(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration) {
        for (NativePlugin plugin : getPlugins().values()) {
            if (plugin.shouldCallEvent(Event.EVENT_GROUP_BAN) && pEvGroupBan(plugin.getId(),
                    plugin.getEventOrDefault(Event.EVENT_GROUP_BAN, "_eventSystem_GroupBan"),
                    subType, time, fromGroup, fromAccount, beingOperateAccount, duration) == 1) {
                break;
            }
        }
    }

    // Native

    public native int loadNativePlugin(String file, int id);

    public native int freeNativePlugin(int id);

    public native int pEvPrivateMessage(int pluginId, String name, int subType, int msgId, long fromAccount, String msg, int font);

    public native int pEvGroupMessage(int pluginId, String name, int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font);

    public native int pEvGroupAdmin(int pluginId, String name, int subType, int time, long fromGroup, long beingOperateAccount);

    public native int pEvGroupMemberLeave(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount);

    public native int pEvGroupBan(int pluginId, String name, int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration);

    public native int callIntMethod(int pluginId, String name);

    public native String callStringMethod(int pluginId, String name);

    // Helper

    private static HashMap<Integer, NativePlugin> getPlugins() {
        return MiraiNative._instance.getPlugins();
    }

    private static NativePlugin getPlugin(int pluginId) {
        return getPlugins().get(pluginId);
    }

    private static MiraiLogger getLogger() {
        return MiraiNative._instance.getLogger();
    }

    private static Bot getBot() {
        return MiraiNative._instance.getBot();
    }

    // Bridge

    @NativeBridgeMethod
    public static int sendFriendMessage(int pluginId, long account, String msg) {
        try {
            return BridgeHelper.sendFriendMessage(account, msg);
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int sendGroupMessage(int pluginId, long group, String msg) {
        try {
            return BridgeHelper.sendGroupMessage(group, msg);
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static void updatePluginInfo(int pluginId, String info) {
        NativePlugin plugin = getPlugin(pluginId);
        if (plugin != null) {
            plugin.setInfo(info);
        }
        System.out.println("Plugin Id " + pluginId + " Info: " + info);
    }

    @NativeBridgeMethod
    public static void addLog(int pluginId, int priority, String type, String content) {
        NativeLoggerHelper.log(getPlugin(pluginId), priority, type, content);
    }

    @NativeBridgeMethod
    public static String getPluginDataDir(int pluginId) {
        return getPlugin(pluginId).getAppDir().getAbsolutePath() + File.separatorChar;
    }

    @NativeBridgeMethod
    public static long getLoginQQ(int pluginId) {
        return getBot().getUin();
    }

    @NativeBridgeMethod
    public static String getLoginNick(int pluginId) {
        return getBot().getNick();
    }

    @NativeBridgeMethod
    public static int setGroupAnonymous(int pluginId, long group, boolean enable) {
        // TODO: 2020/2/28 core 还不支持
        return 0;
    }

    @NativeBridgeMethod
    public static int setGroupBan(int pluginId, long group, long member, long duration) {
        try {
            BridgeHelper.setGroupBan(group, member, (int) duration);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int setGroupCard(int pluginId, long group, long member, String card) {
        try {
            getBot().getGroup(pluginId).get(member).setNameCard(card);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int setGroupKick(int pluginId, long group, long member, boolean reject) {
        try {
            BridgeHelper.setGroupKick(group, member);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int setGroupLeave(int pluginId, long group, boolean dismiss) {
        try {
            BridgeHelper.setGroupLeave(group);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int setGroupSpecialTitle(int pluginId, long group, long member, String title, long duration) {
        try {
            getBot().getGroup(pluginId).get(member).setSpecialTitle(title);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int setGroupWholeBan(int pluginId, long group, boolean enable) {
        try {
            getBot().getGroup(group).setMuteAll(enable);
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @NativeBridgeMethod
    public static int recallMsg(int pluginId, long msgId) {
        return MessageCache.INSTANCE.recall(Long.valueOf(msgId).intValue()) ? 0 : -1;
    }

    /**
     * Indicates the method is called from native code
     */
    @Retention(value = RetentionPolicy.SOURCE)
    @interface NativeBridgeMethod {
    }

    static class NativeLoggerHelper {
        public static final int LOG_DEBUG = 0;
        public static final int LOG_INFO = 10;
        public static final int LOG_INFO_SUCC = 11;
        public static final int LOG_INFO_RECV = 12;
        public static final int LOG_INFO_SEND = 13;
        public static final int LOG_WARNING = 20;
        public static final int LOG_ERROR = 21;
        public static final int LOG_FATAL = 22;

        static void log(NativePlugin plugin, int priority, String type, String content) {
            String c = "[NP " + plugin.getIdentifier();
            if (!"".equals(type)) {
                c += " " + type;
            }
            c += "] " + content;
            switch (priority) {
                case LOG_DEBUG:
                    getLogger().debug(c);
                    break;
                case LOG_INFO:
                case LOG_INFO_RECV:
                case LOG_INFO_SUCC:
                case LOG_INFO_SEND:
                    getLogger().info(c);
                    break;
                case LOG_WARNING:
                    getLogger().warning(c);
                    break;
                case LOG_ERROR:
                    getLogger().error(c);
                    break;
                case LOG_FATAL:
                    getLogger().error("[FATAL]" + c);
                    break;
            }
        }
    }
}
