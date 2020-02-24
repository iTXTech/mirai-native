package org.itxtech.mirainative

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.message.MessageReceipt

object BridgeHelper {


    @JvmStatic
    fun sendMessageToFriend(id: Long, message: String): MessageReceipt<QQ> = runBlocking {
        MiraiNative.INSTANCE.bot.getFriend(id).sendMessage(message)
    }

    @JvmStatic
    fun sendMessageToGroup(id: Long, message: String): MessageReceipt<Group> = runBlocking {
        MiraiNative.INSTANCE.bot.getGroup(id).sendMessage(message)
    }
}