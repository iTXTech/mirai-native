# Mirai Native

__强大的 `Mirai` 原生插件加载器__

Mirai Native 支持所有`stdcall`方式导出方法的`DLL`与 [Mirai](https://github.com/mamoe/mirai) 交互。

与**大部分**`酷Q`插件兼容，**不支持**`CPK`和解包的`DLL`，需获取`DLL`和`JSON`原文件，`JSON`文件**不支持**注释。

## 写在前面

* 本项目欢迎一切形式的贡献，详见 [API 文档](Docs.md)
* [Wiki](https://github.com/iTXTech/mirai-native/wiki) - `开发文档`, `搭建指南`（如`CQHTTP`）等文档
* 本项目将继续维护

## 关于托盘菜单

* 右键流泪猫猫头打开菜单
* 插画由作者女朋友提供

## Mirai Native 插件管理器 `npm`

1. 在 `mirai-console` 中键入 `npm` 获得帮助
1. `npm` (`Native Plugin Manager`) 可`列出插件`、`启用或停用插件`以及`执行指定方法`（比如调用`menu`）

`npm [list|enable|disable|menu|info|load|unload] (插件 Id / 路径) (方法名)`

## 如何获取酷Q插件

**请联系作者提供`DLL`和`JSON`文件**

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
