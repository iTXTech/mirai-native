package org.itxtech.mirainative;

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

    private static NativePlugin getPlugin(int pluginId) {
        return MiraiNative.getINSTANCE().getPlugins().get(pluginId);
    }

    // Bridge
    @SuppressWarnings("unused")
    public static int sendMessageToFriend(int pluginId, long account, String msg) {
        System.out.println("Send to " + account + " Msg: " + msg);
        BridgeHelper.sendMessageToFriend(account, msg);
        return 0;
    }

    @SuppressWarnings("unused")
    public static int sendMessageToGroup(int pluginId, long group, String msg) {
        System.out.println("Send to " + group + " Msg: " + msg);
        BridgeHelper.sendMessageToGroup(group, msg);
        return 0;
    }

    @SuppressWarnings("unused")
    public static void updatePluginInfo(int pluginId, String info) {
        NativePlugin plugin = getPlugin(pluginId);
        if (plugin != null) {
            plugin.setInfo(info);
        }
        System.out.println("Plugin Id " + pluginId + " Info: " + info);
    }

    public static void addLog(int pluginId, int priority, String type, String content) {
        NativeLoggerHelper.log(getPlugin(pluginId), priority, type, content);
    }

    @SuppressWarnings("unused")
    public static String getPluginDataDir(int pluginId) {
        addLog(pluginId, 0, "", getPlugin(pluginId).getAppDir().getAbsolutePath() + File.separatorChar);
        return getPlugin(pluginId).getAppDir().getAbsolutePath() + File.separatorChar;
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
            String c = "[NativePlugin " + plugin.getIdentifier();
            if (!"".equals(type)) {
                c += " " + type;
            }
            c += "] " + content;
            MiraiLogger logger = MiraiNative.getINSTANCE().getLogger();
            switch (priority) {
                case LOG_DEBUG:
                    logger.debug(c);
                    break;
                case LOG_INFO:
                case LOG_INFO_RECV:
                case LOG_INFO_SUCC:
                case LOG_INFO_SEND:
                    logger.info(c);
                    break;
                case LOG_WARNING:
                    logger.warning(c);
                    break;
                case LOG_ERROR:
                    logger.error(c);
                    break;
                case LOG_FATAL:
                    logger.error("[FATAL]" + c);
                    break;
            }
        }
    }
}
