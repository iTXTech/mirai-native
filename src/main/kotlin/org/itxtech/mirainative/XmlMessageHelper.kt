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

package org.itxtech.mirainative

import net.mamoe.mirai.message.data.buildXMLMessage

object XmlMessageHelper {
    fun share(u: String, title: String?, content: String?, image: String?) = buildXMLMessage {
        templateId = 12345
        serviceId = 1
        action = "web"
        brief = "[分享] $title"
        url = u
        item {
            layout = 2
            if (image != null) {
                picture(image)
            }
            if (title != null) {
                title(title)
            }
            if (content != null) {
                summary(content)
            }
        }
    }
}
