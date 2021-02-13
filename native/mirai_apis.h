#pragma once

#include "native.h"

CQAPI(int32_t, isMiraiNative, 0)()
{
	return 1;
}

CQAPI(int32_t, mQuoteMessage, 12)(int32_t plugin_id, int32_t msg_id, const char* msg)
{
	auto env = attach_java();
	auto m = CharsToByteArray(env, msg);
	auto method = env->GetStaticMethodID(bclz, "quoteMessage", "(II[B)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, msg_id, m);
	env->DeleteLocalRef(m);
	detach_java();
	return result;
}

CQAPI(int32_t, mForwardMessage, 24)(int32_t plugin_id, int32_t type, int64_t id, const char* strategy, const char* msg)
{
	auto env = attach_java();
	auto s = CharsToByteArray(env, strategy);
	auto m = CharsToByteArray(env, msg);
	auto method = env->GetStaticMethodID(bclz, "forwardMessage", "(IIJ[B[B)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, type, id, s, m);
	env->DeleteLocalRef(s);
	env->DeleteLocalRef(m);
	detach_java();
	return result;
}

CQAPI(int32_t, mSetGroupKick, 28)(int32_t plugin_id, int64_t group, int64_t member, BOOL reject, const char* msg)
{
	auto env = attach_java();
	auto m = CharsToByteArray(env, msg);
	auto method = env->GetStaticMethodID(bclz, "setGroupKick", "(IJJZ[B)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, member, reject != FALSE, msg);
	detach_java();
	env->DeleteLocalRef(m);
	return result;
}

CQAPI(int32_t, CQ_setGroupKick, 24)(int32_t plugin_id, int64_t group, int64_t member, BOOL reject)
{
	return mSetGroupKick(plugin_id, group, member, reject, nullptr);
}

CQAPI(const char*, mGetGroupEntranceAnnouncement, 12)(int32_t plugin_id, int64_t group)
{
	auto env = attach_java();
	auto method = env->GetStaticMethodID(bclz, "getGroupEntranceAnnouncement", "(IJ)[B");
	auto result = jbyteArray(env->CallStaticObjectMethod(bclz, method, plugin_id, group));
	auto r = ByteArrayToChars(env, result);
	env->DeleteLocalRef(result);
	detach_java();
	return delay_mem_free(r);
}

CQAPI(int32_t, mSetGroupEntranceAnnouncement, 16)(int32_t plugin_id, int64_t group, const char* a)
{
	auto env = attach_java();
	auto an = CharsToByteArray(env, a);
	auto method = env->GetStaticMethodID(bclz, "setGroupEntranceAnnouncement", "(IJ[B)I");
	auto result = env->CallStaticIntMethod(bclz, method, plugin_id, group, an);
	detach_java();
	env->DeleteLocalRef(an);
	return result;
}
