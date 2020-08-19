# 文档 + TODO

1. 带有 `[Incomplete]` 的代表该方法实现不完整
1. 带有 `[Placeholder]` 的代表该方法仅占位，无法实现具体功能
1. 带有 `[Won't Implement]` 的代表该方法不会实现    

## 功能

- [x] 应用 `JSON`
- [x] `CQ码` `[Incomplete]`
- [x] 悬浮窗
- [x] 菜单
- [ ] API请求日志

## CQ码

- [x] At
- [x] 表情
- [x] Emoji
- [x] 图片
- [x] 名片 `[Incomplete]`

## 扩展CQ码详解

 - 发送网络图片以 `.mnimg` 结尾，该文件不落地，例：`[CQ:image,file={247E6A8B-ED3A-1B98-15E8-07E2277C787A}.jpg.mnimg]`
 - 发送本地图片支持绝对和相对路径，相对路径支持java.exe目录下`data\image`和运行目录下`data\image`，例：`[CQ:image,file=2.png]`
 - 发送URL图片使用扩展CQ码，例：`[CQ:image,url=https://xxxx/xxxx.jpg]`
 - 支持发送和接收闪照，额外参数 `type=flash`
 - 发送抖一抖 `[CQ:shake]`
 - 发送各类戳一戳 `[CQ:poke,id=xxx,type=xxx]`，ID和Type见 `mirai` 的 `HummerMessage.kt`，必须在该文件中定义的id和type才能发送
 - 接收`VipFace` `[CQ:vipface,id=xxx,name=xxx,count=xxx]`
 - 发送 `XML` 消息 `[CQ:xml,data=xxxx]`
 - 发送 `JSON` 消息 `[CQ:json,data=xxxx]`，少部分 JSON 消息为此类型
 - 发送 `LightApp` 消息 `[CQ:app,data=xxxx]`，大部分 JSON 消息为此类型
 - 接收未知类型 `Rich` 消息 `[CQ:rich,data=xxxx,id=xx]`
 - 支持接收语音，并**仅能向群**发送语音，`getRecord`不支持指定格式，发送支持`silk`和`amr`格式
 - 发送、接收网络语音以`.mnrec`结尾且该文件不落地 `[CQ:record,file=xxxx.mnrec]`
 - 发送本地语音和URL语音同发送图片

## 酷Q API

- [x] `CQ_addLog`
- [x] `CQ_canSendImage`
- [x] `CQ_canSendRecord`
- [x] `CQ_deleteMsg`
- [x] `CQ_getAppDirectory`
- [x] `CQ_getCookiesV2` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_getCsrfToken` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_getFriendList`
- [x] `CQ_getGroupInfo`
- [x] `CQ_getGroupList`
- [x] `CQ_getGroupMemberInfoV2` `[Incomplete]`
- [x] `CQ_getGroupMemberList` `[Incomplete]`
- [x] `CQ_getImage`
- [x] `CQ_getLoginNick`
- [x] `CQ_getLoginQQ`
- [x] `CQ_getRecordV2` `[Placeholder]`
- [x] `CQ_getStrangerInfo` `[Incomplete]`
- [x] `CQ_sendDiscussMsg` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_sendGroupMsg`
- [x] `CQ_sendLikeV2` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_sendPrivateMsg`
- [x] `CQ_setDiscussLeave` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_setFatal`
- [x] `CQ_setFriendAddRequest`
- [x] `CQ_setGroupAddRequestV2`
- [x] `CQ_setGroupAdmin` `[Placeholder]`
- [x] `CQ_setGroupAnonymous` `[Placeholder]`
- [x] `CQ_setGroupAnonymousBan` `[Placeholder]`
- [x] `CQ_setGroupBan`
- [x] `CQ_setGroupCard`
- [x] `CQ_setGroupKick`
- [x] `CQ_setGroupLeave`
- [x] `CQ_setGroupSpecialTitle`
- [x] `CQ_setGroupWholeBan`

### 过时 酷Q API

- [x] `CQ_getCookies` `[Placeholder]` `[Won't Implement]`
- [ ] `CQ_getGroupMemberInfo`
- [x] `CQ_setGroupAddRequest`
- [ ] `CQ_getRecord`
- [x] `CQ_sendLike` `[Placeholder]` `[Won't Implement]`

## 事件

- [x] `_eventEnable`
- [x] `_eventDisable`
- [x] `_eventStartup`
- [x] `_eventExit`
- [ ] `_eventDiscussMsg` `[Won't Implement]`
- [x] `_eventFriend_Add`
- [x] `_eventGroupMsg`
- [ ] `_eventGroupUpload`
- [x] `_eventPrivateMsg`
- [x] `_eventRequest_AddFriend`
- [x] `_eventRequest_AddGroup`
- [x] `_eventSystem_GroupAdmin`
- [x] `_eventSystem_GroupBan`
- [x] `_eventSystem_GroupMemberDecrease`
- [x] `_eventSystem_GroupMemberIncrease`
