# Mirai Native

__强大的 `mirai` 原生插件加载器__

Mirai Native 支持所有`stdcall`方式导出方法的`DLL`与 [mirai](https://github.com/mamoe/mirai) 交互。

与**大部分**`酷Q`插件兼容，**不支持**`CPK`和解包的`DLL`，需获取`DLL`和`JSON`原文件，`JSON`文件**不支持**注释。

## `Mirai Native` 仅支持 `Windows 32位 Java`

1. 可前往 [Temurin](https://adoptium.net/temurin/releases/) 下载
2. 选择 `Windows` -> `x86` 下载 `Windows 32位 Java`
3. 支持 `Java 11` 之后的版本

## [欢迎参与建设`Mirai Native`插件中心](https://github.com/iTXTech/mirai-native/discussions/121)

## [Wiki - 开发者和用户必读](https://github.com/iTXTech/mirai-native/wiki)

## [下载 `Mirai Native`](https://github.com/iTXTech/mirai-native/releases)

## 使用 [Mirai Console Loader](https://github.com/iTXTech/mirai-console-loader) 安装`Mirai Native`

* `MCL` 支持自动更新插件，支持设置插件更新频道等功能

`.\mcl --update-package org.itxtech:mirai-native --channel stable --type plugin`

## `Mirai Native Tray`

* 右键`流泪猫猫头`打开 `Mirai Native` 托盘菜单。
* 左键`流泪猫猫头`显示悬浮窗。

## `mirai Native Plugin Manager`

```
> npm
Mirai Native 插件管理器

/disable <插件Id>   停用指定 Mirai Native 插件
/enable <插件Id>   启用指定 Mirai Native 插件
/info <插件Id>   查看指定 Mirai Native 插件的详细信息
/list    列出所有 Mirai Native 插件
/load <DLL文件名>   加载指定DLL文件
/menu <插件Id> <方法名>   调用指定 Mirai Native 插件的菜单方法
/reload <插件Id>   重新载入指定 Mirai Native 插件
/unload <插件Id>   卸载指定 Mirai Native 插件
```


## 开源许可证

    iTXTech Mirai Native
    Copyright (C) 2020-2022 iTX Technologies

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
