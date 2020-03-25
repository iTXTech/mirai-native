# Mirai Native

Mirai Native 致力于通过实现 酷Q 的兼容API使 酷Q 的应用（插件）能在 [Mirai](https://github.com/mamoe/mirai) 环境下运行。

## 贡献

本项目欢迎一切形式的贡献，详见 [Docs](Docs.md)

## 运行环境 - ！非常重要！

* **JRE 8 32位** - ~~酷Q的插件都是32位哒~~
* 可能需要管理员权限，因为需要在Java目录下创建数据文件夹，如果不想授权管理员权限，请将JRE移动到不需要管理员权限的目录下

## 开发环境

* [Java Development Kit 8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html)
* [Visual Studio](https://visualstudio.microsoft.com/zh-hans/)
* [IntelliJ IDEA](https://www.jetbrains.com/idea/)

## 如何使用

1. 搭建 `mirai-console` 运行环境
1. 将 `mirai-native.jar` 放入 `plugins` 文件夹
1. 将 `CQP.dll` 和 要加载的 Native 插件 **dll** (不是 **cpk**) 一起放入 `plugins\MiraiNative` 下（如果有插件对应的**json**文件，请使用和`dll`相同的文件名）
1. 如果 Native 插件有依赖的 DLL，请放入 `plugins\MiraiNative\libraries` 下
1. 启动 `mirai-console`

-----

### 关于托盘菜单

* 双击流泪猫猫头查看关于信息
* 右键流泪猫猫头打开菜单
* 插画由作者女朋友提供

### Mirai Native 插件管理器 `npm`

1. 在 `mirai-console` 中键入 `npm` 获得帮助
1. `npm` (`Native Plugin Manager`) 可`列出插件`、`启用或停用插件`以及`执行指定方法`（比如调用`menu`）

`npm [list|enable|disable|menu|info|load|unload] (插件 Id / 路径) (方法名)`

## 使用须知

1. 可使用`酷Q SDK编写`，部分API暂不可用，见上文 `Docs`
1. 提供 `Mirai` 独有API，见 `Mirai Native Advance SDK`（建设中，见 `epl-test`文件夹）
1. `DLL` 字符串编码采用 `GB18030`
1. 待 `Bot` 上线后才会调用插件的 `Enable` 事件，托班菜单内才可以禁用插件

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
