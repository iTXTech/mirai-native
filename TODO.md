# TODO

1. 带有 `[Incomplete]` 的代表该方法实现不完整
1. 带有 `[Placeholder]` 的代表该方法仅占位，无法实现具体功能
1. 带有 `[Won't Implement]` 的代表该方法不会实现    

## 功能

- [x] 应用 `JSON`
- [x] `CQ码` `[Incomplete]`
- [x] 悬浮窗 `[Incomplete]`
- [x] 菜单 `[Incomplete]`
- [ ] 权限验证
- [ ] **直接加载 酷Q CPK 文件**

## CQ码

- [x] At
- [x] 表情
- [x] Emoji
- [x] 图片 `[Incomplete]`
- [x] 名片 `[Incomplete]`

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
- [x] `CQ_getImage` `[Placeholder]`
- [x] `CQ_getLoginNick` `[Incomplete]`
- [x] `CQ_getLoginQQ`
- [x] `CQ_getRecordV2` `[Placeholder]`
- [x] `CQ_getStrangerInfo` `[Placeholder]`
- [x] `CQ_sendDiscussMsg` `[Placeholder]`
- [x] `CQ_sendGroupMsg`
- [x] `CQ_sendLikeV2` `[Placeholder]` `[Won't Implement]`
- [x] `CQ_sendPrivateMsg`
- [x] `CQ_setDiscussLeave` `[Placeholder]`
- [x] `CQ_setFatal`
- [x] `CQ_setFriendAddRequest` `[Placeholder]`
- [x] `CQ_setGroupAddRequestV2` `[Placeholder]`
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

- [x] `CQ_getCookies`
- [ ] `CQ_getGroupMemberInfo`
- [x] `CQ_setGroupAddRequest`
- [ ] `CQ_getRecord`
- [x] `CQ_sendLike` `[Won't Implement]`

## 事件

- [x] `_eventEnable`
- [x] `_eventDisable`
- [x] `_eventStartup`
- [x] `_eventExit`
- [ ] `_eventDiscussMsg`
- [ ] `_eventFriend_Add`
- [x] `_eventGroupMsg`
- [ ] `_eventGroupUpload`
- [x] `_eventPrivateMsg`
- [ ] `_eventRequest_AddFriend`
- [ ] `_eventRequest_AddGroup`
- [x] `_eventSystem_GroupAdmin`
- [x] `_eventSystem_GroupBan`
- [x] `_eventSystem_GroupMemberDecrease`
- [x] `_eventSystem_GroupMemberIncrease` `[Incomplete]`
