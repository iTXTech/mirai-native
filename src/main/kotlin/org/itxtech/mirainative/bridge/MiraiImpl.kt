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

package org.itxtech.mirainative.bridge

import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.kind
import net.mamoe.mirai.utils.RemoteFile.Companion.sendFile
import net.mamoe.mirai.utils.RemoteFile.Companion.uploadFile
import org.itxtech.mirainative.MiraiNative
import org.itxtech.mirainative.bridge.MiraiBridge.call
import org.itxtech.mirainative.manager.CacheManager
import org.itxtech.mirainative.message.ChainCodeConverter
import java.io.File

object MiraiImpl {
    fun quoteMessage(pluginId: Int, msgId: Int, message: String) = call("mQuoteMessage", pluginId, 0) {
        val internalId = CacheManager.nextId()
        MiraiNative.launch {
            val src = CacheManager.getMessage(msgId)
            if (src != null) {
                if (src.kind != MessageSourceKind.GROUP) {
                    if (src.fromId != MiraiNative.bot.id) {
                        val f = MiraiNative.bot.getFriend(src.fromId)
                        val chain = src.quote() + ChainCodeConverter.codeToChain(message, f)
                        f?.sendMessage(chain)?.apply {
                            CacheManager.cacheMessage(source, internalId, chain)
                        }
                    }
                } else {
                    val group = MiraiNative.bot.getGroup(src.targetId)
                    if (src.fromId != MiraiNative.bot.id) {
                        val chain = src.quote() + ChainCodeConverter.codeToChain(message, group)
                        group?.sendMessage(chain)?.apply {
                            CacheManager.cacheMessage(source, internalId, chain)
                        }
                    }
                }
            }
        }
        return internalId
    }

    fun forwardMessage(pluginId: Int, type: Int, id: Long, strategy: String, msg: String) =
        call("mForwardMessage", pluginId, 0) {
            val contact = if (type == 0) MiraiNative.bot.getFriend(id) else MiraiNative.bot.getGroup(id)
            val internalId = CacheManager.nextId()
            MiraiNative.launch {
                contact?.sendMessage(ForwardMessageDecoder.decode(contact, strategy, msg))
            }
            return internalId
        }

    fun setGroupKick(pluginId: Int, groupId: Long, memberId: Long, message: String) =
        call(if (message.isBlank()) "CQ_setGroupKick" else "mSetGroupKick", pluginId, 0) {
            MiraiNative.launch {
                MiraiNative.bot.getGroup(groupId)?.get(memberId)?.kick(message)
            }
            return 0
        }

    fun getGroupEntranceAnnouncement(pluginId: Int, id: Long) =
        call("mGetGroupEntranceAnnouncement", pluginId, "") {
            return MiraiNative.bot.getGroup(id)?.settings?.entranceAnnouncement ?: ""
        }

    fun setGroupEntranceAnnouncement(pluginId: Int, id: Long, a: String) =
        call("mSetGroupEntranceAnnouncement", pluginId, 0) {
            MiraiNative.bot.getGroup(id)?.settings?.entranceAnnouncement = a
            return 0
        }

    fun uploadGroupFile(pluginId: Int, id: Long, path: String, file: String) =
        call("mUploadGroupFile", pluginId, 0) {
            runBlocking {
                MiraiNative.bot.getGroup(id)?.sendFile(path, File(file))
            }?.source?.apply { return@call CacheManager.cacheMessage(this) }
            return@call 0
        }

    fun downloadGroupFile(pluginId: Int, id: Long, path: String) =
        call("mGetGroupFile", pluginId, "") {
            var localFile = ""
            runBlocking {
                MiraiNative.bot.getGroup(id)?.filesRoot?.resolve(path)?.apply {
                    if (exists()) {
                        val time = System.currentTimeMillis()
                        getDownloadInfo()?.apply {
                            val file = File(
                                MiraiNative.fileDataPath.absolutePath + File.separatorChar +
                                        "${time}_${id}_${md5}_${filename}"
                            )
                            file.writeChannel().use {
                                MiraiBridge.client.get<HttpResponse>(url).content.copyTo(this)
                            }
                            localFile = file.absolutePath
                        }
                    }
                }
            }
            return localFile
        }

    fun listGroupFile(pluginId: Int, id: Long, path: String) =
        call("mListGroupFile", pluginId, "") {
            //todo
        }
}
