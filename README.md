# Mirai Native

Mirai Native 致力于通过实现 酷Q 的兼容API使 酷Q 的应用（插件）能在 [Mirai](https://github.com/mamoe/mirai) 环境下运行。

## 运行环境

* **JRE 8 32位** - ~~酷Q的插件都是32位哒~~

## 开发环境

* [Java Development Kit 8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
* [Visual Studio](https://visualstudio.microsoft.com/zh-hans/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)

## 如何使用

1. 搭建 `mirai-console` 运行环境
1. 将 `mirai-native.jar` 放入 `plugins` 文件夹
1. 将 `CQP.dll` 和 要加载的酷Q插件 **dll** (不是 **cpk**) 一起放入 `plugins\MiraiNative` 下（如果有插件对应的**json**文件，请使用和`dll`相同的文件名）
1. 启动 `mirai-console`

### Mirai Native 插件管理器 `npm`

1. 在 `mirai-console` 中键入 `npm` 获得帮助
1. `npm` (`Native Plugin Manager`) 可`列出插件`、`启用或停用插件`以及`执行指定方法`（比如调用`menu`）

## 如何获取酷Q插件

1. 启动酷Q
1. 查找路径 `data\tmp\capp\` 的二级目录下的 `cpk` 文件，将后缀修改为 `dll` 即可

## 开源协议

    Copyright (C) 2020 iTX Technologies

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
