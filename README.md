# Mirai Native

Mirai Native 致力于通过实现 酷Q 的兼容API使 酷Q 的应用（插件）能在 [Mirai](https://github.com/mamoe/mirai) 环境下运行。

## 运行环境

* **JRE 8 32位** - ~~酷Q的插件都是32位哒~~

## 开发环境

* JDK 8
* Visual Studio 2019

## 如何使用

1. 搭建 `mirai-console` 运行环境
1. 将 `mirai-native.jar` 放入 `plugins` 文件夹
1. 将 `CQP.dll` 和 要加载的酷Q插件 **DLL** (不是 **CPK**) 一起放入 `plugins\MiraiNative` 下
1. 启动 `mirai-console`

## 如何获取酷Q插件的DLL

1. 启动酷Q
1. 查找路径 `data\tmp\capp\` 的二级目录下的 `CPK` 文件，将后缀修改为 `DLL` 即可

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
