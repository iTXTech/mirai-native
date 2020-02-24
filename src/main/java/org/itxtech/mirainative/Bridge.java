package org.itxtech.mirainative;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.utils.MiraiLogger;
import org.itxtech.mirainative.plugin.NativePlugin;

import java.io.File;

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
    public native void loadNativePlugin(String file, int id);

    public native void disablePlugin(int id);

    public native void enablePlugin(int id);

    // Events
    public native void eventStartup();

    public native void eventExit();

    public native void eventEnable();

    public native void eventDisable();

    public native void eventPrivateMessage(int subType, int msgId, long fromAccount, String msg, int font);

    public native void eventGroupMessage(int subType, int msgId, long fromGroup, long fromAccount, String fromAnonymous, String msg, int font);

    public native void eventGroupAdmin(int subType, int time, long fromGroup, long beingOperateAccount);

    public native void eventGroupMemberLeave(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount);

    public native void eventGroupBan(int subType, int time, long fromGroup, long fromAccount, long beingOperateAccount, long duration);

    // Helper

    private static NativePlugin getPlugin(int pluginId) {
        return MiraiNative._instance.getPlugins().get(pluginId);
    }

    private static MiraiLogger getLogger() {
        return MiraiNative._instance.getLogger();
    }

    private static Bot getBot() {
        return MiraiNative._instance.getBot();
    }

    // Bridge

    @SuppressWarnings("unused")
    public static int sendMessageToFriend(int pluginId, long account, String msg) {
        try {
            MessageReceipt<QQ> receipt = BridgeHelper.sendMessageToFriend(account, msg);
            //TODO: message id
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @SuppressWarnings("unused")
    public static int sendMessageToGroup(int pluginId, long group, String msg) {
        try {
            MessageReceipt<Group> receipt = BridgeHelper.sendMessageToGroup(group, msg);
            //TODO: message id
            return 0;
        } catch (Exception e) {
            getLogger().error("[NP " + getPlugin(pluginId).getIdentifier() + "] ", e);
            return -1;
        }
    }

    @SuppressWarnings("unused")
    public static void updatePluginInfo(int pluginId, String info) {
        NativePlugin plugin = getPlugin(pluginId);
        if (plugin != null) {
            plugin.setInfo(info);
        }
        System.out.println("Plugin Id " + pluginId + " Info: " + info);
    }

    @SuppressWarnings("unused")
    public static void addLog(int pluginId, int priority, String type, String content) {
        NativeLoggerHelper.log(getPlugin(pluginId), priority, type, content);
    }

    @SuppressWarnings("unused")
    public static String getPluginDataDir(int pluginId) {
        return getPlugin(pluginId).getAppDir().getAbsolutePath() + File.separatorChar;
    }

    @SuppressWarnings("unused")
    public static long getLoginQQ(int pluginId) {
        return getBot().getUin();
    }

    @SuppressWarnings("unused")
    public static String getLoginNick(int pluginId) {
        return getBot().getNick();
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
