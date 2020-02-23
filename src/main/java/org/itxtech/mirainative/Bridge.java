package org.itxtech.mirainative;

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

    // Bridge
    public static int sendMessageToFriend(int pluginId, long account, String msg) {
        System.out.println("Send to " + account + " Msg: " + msg);
        //MiraiNative.getINSTANCE().getBot().getFriend()
        return 0;
    }

    public static int sendMessageToGroup(int pluginId, long group, String msg) {
        System.out.println("Send to " + group + " Msg: " + msg);
        return 0;
    }
}
