#include <jni.h>
#include <vector>
#include <Windows.h>
#include "org_itxtech_mirainative_Bridge.h"
#include "native.h"

using namespace std;

struct native_plugin
{
	int id;
	char* file;
	HMODULE dll;
	bool enabled;

	native_plugin(int i, char* f)
	{
		id = i;
		file = f;
		dll = nullptr;
		enabled = true;
	}
};

// Global var

vector<native_plugin> plugins;

// Helper
char* JstringToGb(JNIEnv* env, jstring jstr)
{
	int length = env->GetStringLength(jstr);
	auto jcstr = env->GetStringChars(jstr, nullptr);
	auto clen = WideCharToMultiByte(GB18030, 0, LPCWSTR(jcstr), length, nullptr, 0, nullptr, nullptr);
	auto rtn = static_cast<char*>(malloc(clen));
	int size = 0;
	size = WideCharToMultiByte(GB18030, 0, LPCWSTR(jcstr), length, rtn, clen, nullptr, nullptr);
	if (size <= 0)
	{
		return nullptr;
	}
	env->ReleaseStringChars(jstr, jcstr);
	rtn[size] = 0;
	return rtn;
}

jstring GbToJstring(JNIEnv* env, const char* str)
{
	if (str == nullptr)
	{
		str = "";
	}
	int slen = strlen(str);
	if (slen == 0)
	{
		return env->NewStringUTF(str);
	}
	jstring rtn = nullptr;
	unsigned short* buffer = 0;
	const auto length = MultiByteToWideChar(GB18030, 0, LPCSTR(str), slen, nullptr, 0);
	buffer = static_cast<unsigned short*>(malloc(length * 2 + 1));
	if (MultiByteToWideChar(GB18030, 0, LPCSTR(str), slen, LPWSTR(buffer), length) > 0)
	{
		rtn = env->NewString(static_cast<jchar*>(buffer), length);
	}
	if (buffer)
	{
		free(buffer);
	}
	return rtn;
}

FARPROC GetMethod(JNIEnv* env, jint id, jstring method)
{
	return GetProcAddress(plugins[id].dll, env->GetStringUTFChars(method, nullptr));
}

// Load

JavaVM* jvm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK)
	{
		return -1;
	}
	jvm = vm;

	return JNI_VERSION_1_8;
}

JNIEnv* AttachJava()
{
	JNIEnv* java = nullptr;
	if (jvm)
	{
		const int getEnvStat = jvm->GetEnv(reinterpret_cast<void**>(&java), JNI_VERSION_1_8);
		if (getEnvStat == JNI_EDETACHED)
		{
			JavaVMAttachArgs args = {JNI_VERSION_1_8, nullptr, nullptr};
			jvm->AttachCurrentThread(reinterpret_cast<void**>(&java), &args);
		}
		else if (getEnvStat == JNI_EVERSION)
		{
		}
	}
	return java;
}

// Plugin

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_loadNativePlugin(
	JNIEnv* env, jobject obj, jstring file, jint id)
{
	native_plugin plugin = {id, const_cast<char*>(env->GetStringUTFChars(file, nullptr))};
	const auto dll = LoadLibraryA(plugin.file);

	if (dll != nullptr)
	{
		plugin.dll = dll;
		plugins.push_back(plugin);

		const auto init = FuncInitialize(GetProcAddress(dll, "Initialize"));
		if (init)
		{
			init(plugin.id);
		}

		const auto info = StringMethod(GetProcAddress(dll, "AppInfo"));
		if (info)
		{
			const auto jstr = GbToJstring(env, info());
			const auto clazz = env->FindClass(BRIDGE);
			const auto method = env->GetStaticMethodID(clazz, "updatePluginInfo", "(ILjava/lang/String;)V");
			env->CallStaticVoidMethod(clazz, method, plugin.id, jstr);
			env->DeleteLocalRef(jstr);
		}

		return 0;
	}
	return GetLastError();
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_freeNativePlugin(
	JNIEnv* env, jobject obj, jint id)
{
	auto r = FreeLibrary(plugins[id].dll);
	if (r != FALSE)
	{
		return 0;
	}
	return GetLastError();
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_processMessage(JNIEnv* env, jobject obj)
{
	MSG msg;
	while (PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE) > 0)
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_callIntMethod(
	JNIEnv* env, jobject obj, jint id, jstring method)
{
	const auto m = IntMethod(GetMethod(env, id, method));
	if (m)
	{
		return m();
	}
	return 0;
}

JNIEXPORT jstring JNICALL Java_org_itxtech_mirainative_Bridge_callStringMethod(
	JNIEnv* env, jobject obj, jint id, jstring method)
{
	const char* rtn = "";
	const auto m = StringMethod(GetMethod(env, id, method));
	if (m)
	{
		rtn = m();
	}
	return env->NewStringUTF(rtn);
}

// Event

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvPrivateMessage(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint msg_id, jlong acct, jstring msg, jint font)
{
	const auto m = EvPriMsg(GetMethod(env, id, method));
	if (m)
	{
		return m(type, msg_id, acct, JstringToGb(env, msg), font);
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvGroupMessage(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint msg_id, jlong grp,
	jlong acct, jstring anon, jstring msg, jint font)
{
	const auto m = EvGroupMsg(GetMethod(env, id, method));
	if (m)
	{
		return m(type, msg_id, grp, acct, JstringToGb(env, anon), JstringToGb(env, msg), font);
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvGroupAdmin(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint time, jlong grp, jlong acct)
{
	const auto m = EvGroupAdmin(GetMethod(env, id, method));
	if (m)
	{
		return m(type, time, grp, acct);
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvGroupMember(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint time, jlong grp, jlong acct, jlong mbr)
{
	const auto m = EvGroupMember(GetMethod(env, id, method));
	if (m)
	{
		return m(type, time, grp, acct, mbr);
	}
	return 0;
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvGroupBan(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint time, jlong grp,
	jlong acct, jlong mbr, jlong dur)
{
	const auto m = EvGroupBan(GetMethod(env, id, method));
	if (m)
	{
		return m(type, time, grp, acct, mbr, dur);
	}
	return 0;
}

// CQ APIs

CQAPI(int32_t, CQ_addLog, 16)(int32_t plugin_id, int32_t priority, const char* type, const char* content)
{
	auto env = AttachJava();
	auto t = GbToJstring(env, type);
	auto c = GbToJstring(env, content);
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "addLog", "(IILjava/lang/String;Ljava/lang/String;)V");
	jint result = env->CallStaticIntMethod(clazz, method, plugin_id, priority, t, c);
	env->DeleteLocalRef(t);
	env->DeleteLocalRef(c);
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
	auto env = AttachJava();
	auto jstr = GbToJstring(env, msg);
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, m, "(IJLjava/lang/String;)I");
	jint result = env->CallStaticIntMethod(clazz, method, plugin_id, acc, jstr);
	env->DeleteLocalRef(jstr);
	return result;
}

CQAPI(int32_t, CQ_sendPrivateMsg, 16)(int32_t plugin_id, int64_t account, const char* msg)
{
	return sendMsg(plugin_id, account, msg, "sendFriendMessage");
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
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getPluginDataDir", "(I)Ljava/lang/String;");
	jstring result = jstring(env->CallStaticObjectMethod(clazz, method, plugin_id));
	auto r = JstringToGb(env, result);
	env->DeleteLocalRef(result);
	return r;
}

CQAPI(int64_t, CQ_getLoginQQ, 4)(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getLoginQQ", "(I)J");
	return env->CallStaticLongMethod(clazz, method, plugin_id);
}

CQAPI(const char*, CQ_getLoginNick, 4)(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getLoginNick", "(I)Ljava/lang/String;");
	return JstringToGb(env, jstring(env->CallStaticObjectMethod(clazz, method, plugin_id)));
}

CQAPI(int32_t, CQ_setGroupAnonymous, 16)(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupAnonymous", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, enable != FALSE);
}

CQAPI(int32_t, CQ_setGroupBan, 28)(int32_t plugin_id, int64_t group, int64_t member, int64_t duration)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupBan", "(IJJJ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, member, duration);
}

CQAPI(int32_t, CQ_setGroupCard, 24)(int32_t plugin_id, int64_t group, int64_t member, const char* card)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupCard", "(IJJLjava/lang/String;)I");
	auto jstr = GbToJstring(env, card);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, group, member, jstr);
	env->DeleteLocalRef(jstr);
	return result;
}

CQAPI(int32_t, CQ_setGroupKick, 24)(int32_t plugin_id, int64_t group, int64_t member, BOOL reject)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupKick", "(IJJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, member, reject != FALSE);
}

CQAPI(int32_t, CQ_setGroupLeave, 16)(int32_t plugin_id, int64_t group, BOOL dismiss)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupLeave", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, dismiss != FALSE);
}

CQAPI(int32_t, CQ_setGroupSpecialTitle, 32)(int32_t plugin_id, int64_t group, int64_t member,
                                            const char* title, int64_t duration)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupSpecialTitle", "(IJJLjava/lang/String;J)I");
	auto jstr = GbToJstring(env, title);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, group, member, jstr, duration);
	env->DeleteLocalRef(jstr);
	return result;
}

CQAPI(int32_t, CQ_setGroupWholeBan, 16)(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupWholeBan", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, enable != FALSE);
}

CQAPI(int32_t, CQ_deleteMsg, 12)(int32_t plugin_id, int64_t msg_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "recallMsg", "(IJ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, msg_id);
}

CQAPI(const char*, CQ_getFriendList, 8)(int32_t plugin_id, BOOL reserved)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getFriendList", "(IZ)Ljava/lang/String;");
	return env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, reserved != FALSE)),
	                              nullptr);
}

CQAPI(const char*, CQ_getGroupInfo, 16)(int32_t plugin_id, int64_t group, BOOL cache)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getGroupInfo", "(IJZ)Ljava/lang/String;");
	return env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, group, cache != FALSE)),
	                              nullptr);
}

CQAPI(const char*, CQ_getGroupList, 4)(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getGroupList", "(I)Ljava/lang/String;");
	return env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id)), nullptr);
}

CQAPI(const char*, CQ_getGroupMemberInfoV2, 24)(int32_t plugin_id, int64_t group, int64_t account, BOOL cache)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getGroupMemberInfo", "(IJJZ)Ljava/lang/String;");
	return env->GetStringUTFChars(
		jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, group, account, cache != FALSE)), nullptr);
}

CQAPI(const char*, CQ_getGroupMemberList, 12)(int32_t plugin_id, int64_t group)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getGroupMemberList", "(IJ)Ljava/lang/String;");
	return env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, group)), nullptr);
}

CQAPI(const char*, CQ_getCookiesV2, 8)(int32_t plugin_id, const char* domain)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getCookies", "(ILjava/lang/String;)Ljava/lang/String;");
	auto jstr = GbToJstring(env, domain);
	auto result = env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, jstr)), nullptr);
	env->DeleteLocalRef(jstr);
	return result;
}

CQAPI(const char*, CQ_getCsrfToken, 4)(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getCsrfToken", "(I)Ljava/lang/String;");
	return env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id)), nullptr);
}

CQAPI(const char*, CQ_getImage, 8)(int32_t plugin_id, const char* image)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getImage", "(ILjava/lang/String;)Ljava/lang/String;");
	auto jstr = GbToJstring(env, image);
	auto result = env->GetStringUTFChars(jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, jstr)), nullptr);
	env->DeleteLocalRef(jstr);
	return result;
}

CQAPI(const char*, CQ_getRecordV2, 12)(int32_t plugin_id, const char* file, const char* format)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getRecord",
	                                     "(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
	auto f = GbToJstring(env, file);
	auto fmt = GbToJstring(env, format);
	auto result = env->GetStringUTFChars(
		jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, f, fmt)), nullptr);
	env->DeleteLocalRef(f);
	env->DeleteLocalRef(fmt);
	return result;
}

CQAPI(const char*, CQ_getStrangerInfo, 16)(int32_t plugin_id, int64_t account, BOOL cache)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "getStrangerInfo", "(IJZ)Ljava/lang/String;");
	return env->GetStringUTFChars(
		jstring(env->CallStaticObjectMethod(clazz, method, plugin_id, account, cache != FALSE)), nullptr);
}

CQAPI(int32_t, CQ_sendDiscussMsg, 16)(int32_t plugin_id, int64_t group, const char* msg)
{
	return sendMsg(plugin_id, group, msg, "sendDiscussMessage");
}

CQAPI(int32_t, CQ_sendLikeV2, 16)(int32_t plugin_id, int64_t account, int32_t times)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "sendLike", "(IJI)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, account, times);
}

CQAPI(int32_t, CQ_setDiscussLeave, 12)(int32_t plugin_id, int64_t group)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setDiscussLeave", "(IJ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group);
}

CQAPI(int32_t, CQ_setFriendAddRequest, 16)(int32_t plugin_id, const char* id, int32_t type, const char* remark)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setFriendAddRequest", "(ILjava/lang/String;ILjava/lang/String;)I");
	auto i = env->NewStringUTF(id);
	auto r = GbToJstring(env, remark);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, i, type, r);
	env->DeleteLocalRef(i);
	env->DeleteLocalRef(r);
	return result;
}

CQAPI(int32_t, CQ_setGroupAddRequestV2, 20)(int32_t plugin_id, const char* id, int32_t req_type, int32_t fb_type,
                                            const char* reason)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupAddRequest", "(ILjava/lang/String;IILjava/lang/String;)I");
	auto i = env->NewStringUTF(id);
	auto r = GbToJstring(env, reason);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, i, req_type, fb_type, r);
	env->DeleteLocalRef(i);
	env->DeleteLocalRef(r);
	return result;
}

CQAPI(int32_t, CQ_setGroupAdmin, 24)(int32_t plugin_id, int64_t group, int64_t account, BOOL admin)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupAdmin", "(IJJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, account, admin != FALSE);
}

CQAPI(int32_t, CQ_setGroupAnonymousBan, 24)(int32_t plugin_id, int64_t group, const char* id, int64_t duration)
{
	auto env = AttachJava();
	auto clazz = env->FindClass(BRIDGE);
	auto method = env->GetStaticMethodID(clazz, "setGroupAnonymousBan", "(IJLjava/lang/String;J)I");
	auto i = env->NewStringUTF(id);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, group, i, duration);
	env->DeleteLocalRef(i);
	return result;
}
