#pragma once

#include "native.h"

CQAPI(int32_t, CQ_addLog, 16)(int32_t plugin_id, int32_t priority, const char* type, const char* content)
{
	auto env = attach_java();
	auto t = CharsToByteArray(env, type);
	auto c = CharsToByteArray(env, content);
	auto method = env->GetStaticMethodID(bclz, "addLog", "(II[B[B)V");
	env->CallStaticVoidMethod(bclz, method, plugin_id, priority, t, c);
	env->DeleteLocalRef(t);
	env->DeleteLocalRef(c);
	detach_java();
	return 0;
}

CQAPI(int32_t, CQ_canSendImage, 4)(int32_t)
{
	return 1;
}

CQAPI(int32_t, CQ_canSendRecord, 4)(int32_t)
{
	return 1;
}

int32_t sendMsg(int32_t plugin_id, int64_t acc, const char* msg, const char* m)
{
	auto env = attach_java();
	auto jstr = CharsToByteArray(env, msg);
	auto method = env->GetStaticMethodID(bclz, m, "(IJ[B)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, acc, jstr);
	env->DeleteLocalRef(jstr);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_sendPrivateMsg, 16)(int32_t plugin_id, int64_t account, const char* msg)
{
	return sendMsg(plugin_id, account, msg, "sendPrivateMessage");
}

CQAPI(int32_t, CQ_sendGroupMsg, 16)(int32_t plugin_id, int64_t group, const char* msg)
{
	return sendMsg(plugin_id, group, msg, "sendGroupMessage");
}

CQAPI(int32_t, CQ_setFatal, 8)(int32_t plugin_id, const char* info)
{
	CQ_addLog(plugin_id, 22, "", info);
	return 0;
}

CQAPI(const char*, CQ_getAppDirectory, 4)(int32_t plugin_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getPluginDataDir", "(I)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(int64_t, CQ_getLoginQQ, 4)(int32_t plugin_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getLoginQQ", "(I)J");
	auto result = env->CallStaticLongMethod(bclz, method, plugin_id);
	detach_java();
	return result;
}

CQAPI(const char*, CQ_getLoginNick, 4)(int32_t plugin_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getLoginNick", "(I)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(int32_t, CQ_setGroupAnonymous, 16)(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupAnonymous", "(IJZ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, enable != FALSE);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupBan, 28)(int32_t plugin_id, int64_t group, int64_t member, int64_t duration)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupBan", "(IJJJ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, member, duration);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupCard, 24)(int32_t plugin_id, int64_t group, int64_t member, const char* card)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupCard", "(IJJ[B)I");
	auto jstr = CharsToByteArray(env, card);
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, member, jstr);
	env->DeleteLocalRef(jstr);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupLeave, 16)(int32_t plugin_id, int64_t group, BOOL dismiss)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupLeave", "(IJZ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, dismiss != FALSE);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupSpecialTitle, 32)(int32_t plugin_id, int64_t group, int64_t member,
                                            const char* title, int64_t duration)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupSpecialTitle", "(IJJ[BJ)I");
	auto jstr = CharsToByteArray(env, title);
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, member, jstr, duration);
	env->DeleteLocalRef(jstr);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupWholeBan, 16)(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupWholeBan", "(IJZ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, enable != FALSE);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_deleteMsg, 12)(int32_t plugin_id, int64_t msg_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "recallMsg", "(IJ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, msg_id);
	detach_java();
	return result;
}

CQAPI(const char*, CQ_getFriendList, 8)(int32_t plugin_id, BOOL reserved)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getFriendList", "(IZ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, reserved != FALSE));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getGroupInfo, 16)(int32_t plugin_id, int64_t group, BOOL cache)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getGroupInfo", "(IJZ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, group, cache != FALSE));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getGroupList, 4)(int32_t plugin_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getGroupList", "(I)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getGroupMemberInfoV2, 24)(int32_t plugin_id, int64_t group, int64_t account, BOOL cache)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getGroupMemberInfo", "(IJJZ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, group, account, cache != FALSE));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getGroupMemberList, 12)(int32_t plugin_id, int64_t group)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getGroupMemberList", "(IJ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, group));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getCookiesV2, 8)(int32_t plugin_id, const char* domain)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getCookies", "(I[B)[B");
	auto jstr = CharsToByteArray(env, domain);
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, jstr));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	env->DeleteLocalRef(jstr);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getCsrfToken, 4)(int32_t plugin_id)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getCsrfToken", "(I)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getImage, 8)(int32_t plugin_id, const char* image)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getImage", "(I[B)[B");
	auto jstr = CharsToByteArray(env, image);
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, jstr));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	env->DeleteLocalRef(jstr);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getRecordV2, 12)(int32_t plugin_id, const char* file, const char* format)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getRecord", "(I[B[B)[B");
	auto f = CharsToByteArray(env, file);
	auto fmt = CharsToByteArray(env, format);
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, f, fmt));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(f);
	env->DeleteLocalRef(fmt);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(const char*, CQ_getStrangerInfo, 16)(int32_t plugin_id, int64_t account, BOOL cache)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getStrangerInfo", "(IJZ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, account, cache != FALSE));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(int32_t, CQ_sendDiscussMsg, 16)(int32_t plugin_id, int64_t group, const char* msg)
{
	return sendMsg(plugin_id, group, msg, "sendDiscussMessage");
}

CQAPI(int32_t, CQ_sendLikeV2, 16)(int32_t plugin_id, int64_t account, int32_t times)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "sendLike", "(IJI)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, account, times);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setDiscussLeave, 12)(int32_t plugin_id, int64_t group)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setDiscussLeave", "(IJ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setFriendAddRequest, 16)(int32_t plugin_id, const char* id, int32_t type, const char* remark)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setFriendAddRequest", "(I[BI[B)I");
	auto i = CharsToByteArray(env, id);
	auto r = CharsToByteArray(env, remark);
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, i, type, r);
	env->DeleteLocalRef(i);
	env->DeleteLocalRef(r);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupAddRequestV2, 20)(int32_t plugin_id, const char* id, int32_t req_type, int32_t fb_type,
                                            const char* reason)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupAddRequest", "(I[BII[B)I");
	auto i = CharsToByteArray(env, id);
	auto r = CharsToByteArray(env, reason);
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, i, req_type, fb_type, r);
	env->DeleteLocalRef(i);
	env->DeleteLocalRef(r);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupAdmin, 24)(int32_t plugin_id, int64_t group, int64_t account, BOOL admin)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupAdmin", "(IJJZ)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, account, admin != FALSE);
	detach_java();
	return result;
}

CQAPI(int32_t, CQ_setGroupAnonymousBan, 24)(int32_t plugin_id, int64_t group, const char* id, int64_t duration)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "setGroupAnonymousBan", "(IJ[BJ)I");
	auto i = CharsToByteArray(env, id);
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, i, duration);
	env->DeleteLocalRef(i);
	detach_java();
	return result;
}

// Legacy

CQAPI(const char*, CQ_getCookies, 4)(int32_t plugin_id)
{
	return CQ_getCookiesV2(plugin_id, "");
}

CQAPI(int32_t, CQ_setGroupAddRequest, 16)(int32_t plugin_id, const char* id, int32_t req_type, int32_t fb_type)
{
	return CQ_setGroupAddRequestV2(plugin_id, id, req_type, fb_type, "");
}

CQAPI(int32_t, CQ_sendLike, 12)(int32_t plugin_id, int64_t account)
{
	return CQ_sendLikeV2(plugin_id, account, 1);
}

CQAPI(int32_t, CQ_setFunctionMark, 8)(int32_t plugin_id, const char* name)
{
	return 0;
}

CQAPI(const char*, CQ_getRecord, 12)(int32_t plugin_id, const char* file, const char* format)
{
	return CQ_getRecordV2(plugin_id, file, format);
}
