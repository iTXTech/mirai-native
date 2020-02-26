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
	auto clen = WideCharToMultiByte(CP_ACP, 0, LPCWSTR(jcstr), length, nullptr, 0, nullptr, nullptr);
	auto rtn = static_cast<char*>(malloc(clen));
	int size = 0;
	size = WideCharToMultiByte(CP_ACP, 0, LPCWSTR(jcstr), length, rtn, clen, nullptr, nullptr);
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
	jstring rtn = 0;
	int slen = strlen(str);
	unsigned short* buffer = 0;
	if (slen == 0)
	{
		rtn = env->NewStringUTF(str);
	}
	else
	{
		int length = MultiByteToWideChar(CP_ACP, 0, LPCSTR(str), slen, nullptr, 0);
		buffer = static_cast<unsigned short*>(malloc(length * 2 + 1));
		if (MultiByteToWideChar(CP_ACP, 0, LPCSTR(str), slen, LPWSTR(buffer), length) > 0)
		{
			rtn = env->NewString(static_cast<jchar*>(buffer), length);
		}
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
		int getEnvStat = jvm->GetEnv((void**)&java, JNI_VERSION_1_8);
		if (getEnvStat == JNI_EDETACHED)
		{
			JavaVMAttachArgs args = {JNI_VERSION_1_8, 0, 0};
			jvm->AttachCurrentThread((void**)&java, &args);
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
			const auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
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

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_callIntMethod(
	JNIEnv* env, jobject obj, jint id, jstring method)
{
	const auto m = IntMethod(GetMethod(env, id, method));
	if (m)
	{
		return m();
	}
	return -1;
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
	return GbToJstring(env, rtn);
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

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_pEvGroupMemberLeave(
	JNIEnv* env, jobject obj, jint id, jstring method, jint type, jint time, jlong grp, jlong acct, jlong mbr)
{
	const auto m = EvGroupMemberLeave(GetMethod(env, id, method));
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

int32_t __stdcall CQ_addLog(int32_t plugin_id, int32_t priority, const char* type, const char* content)
{
	auto env = AttachJava();
	auto t = GbToJstring(env, type);
	auto c = GbToJstring(env, content);
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "addLog", "(IILjava/lang/String;Ljava/lang/String;)V");
	jint result = env->CallStaticIntMethod(clazz, method, plugin_id, priority, t, c);
	env->DeleteLocalRef(t);
	env->DeleteLocalRef(c);
	return 0;
}

int32_t __stdcall CQ_canSendImage(int32_t)
{
	return 1;
}

int32_t __stdcall CQ_canSendRecord(int32_t)
{
	return 1;
}

int32_t __stdcall sendMsg(int32_t plugin_id, int64_t acc, const char* msg, const char* m)
{
	auto env = AttachJava();
	auto jstr = GbToJstring(env, msg);
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, m, "(IJLjava/lang/String;)I");
	jint result = env->CallStaticIntMethod(clazz, method, plugin_id, acc, jstr);
	env->DeleteLocalRef(jstr);
	return result;
}

int32_t __stdcall CQ_sendPrivateMsg(int32_t plugin_id, int64_t account, const char* msg)
{
	return sendMsg(plugin_id, account, msg, "sendFriendMessage");
}

int32_t __stdcall CQ_sendGroupMsg(int32_t plugin_id, int64_t group, const char* msg)
{
	return sendMsg(plugin_id, group, msg, "sendGroupMessage");
}

int32_t __stdcall CQ_setFatal(int32_t plugin_id, const char* info)
{
	CQ_addLog(plugin_id, 22, "", info);
	return 0;
}

const char* __stdcall CQ_getAppDirectory(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "getPluginDataDir", "(I)Ljava/lang/String;");
	jstring result = jstring(env->CallStaticObjectMethod(clazz, method, plugin_id));
	auto r = JstringToGb(env, result);
	env->DeleteLocalRef(result);
	return r;
}

int64_t __stdcall CQ_getLoginQQ(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "getLoginQQ", "(I)J");
	return env->CallStaticLongMethod(clazz, method, plugin_id);
}

const char* __stdcall CQ_getLoginNick(int32_t plugin_id)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "getLoginNick", "(I)Ljava/lang/String;");
	auto result = jstring(env->CallStaticObjectMethod(clazz, method, plugin_id));
	return JstringToGb(env, result);
}

int32_t __stdcall CQ_setGroupAnonymous(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupAnonymous", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, enable != FALSE);
}

int32_t __stdcall CQ_setGroupBan(int32_t plugin_id, int64_t group, int64_t member, int64_t duration)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupBan", "(IJJJ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, member, duration);
}

int32_t __stdcall CQ_setGroupCard(int32_t plugin_id, int64_t group, int64_t member, const char* card)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupCard", "(IJJLjava/lang/String;)I");
	auto jstr = GbToJstring(env, card);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, group, member, jstr);
	env->DeleteLocalRef(jstr);
	return result;
}

int32_t __stdcall CQ_setGroupKick(int32_t plugin_id, int64_t group, int64_t member, BOOL reject)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupKick", "(IJJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, member, reject != FALSE);
}

int32_t __stdcall CQ_setGroupLeave(int32_t plugin_id, int64_t group, BOOL dismiss)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupLeave", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, dismiss != FALSE);
}

int32_t __stdcall CQ_setGroupSpecialTitle(int32_t plugin_id, int64_t group, int64_t member,
                                          const char* title, int64_t duration)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupSpecialTitle", "(IJZ)I");
	auto jstr = GbToJstring(env, title);
	auto result = env->CallStaticIntMethod(clazz, method, plugin_id, group, member, jstr, duration);
	env->DeleteLocalRef(jstr);
	return result;
}

int32_t __stdcall CQ_setGroupWholeBan(int32_t plugin_id, int64_t group, BOOL enable)
{
	auto env = AttachJava();
	auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
	auto method = env->GetStaticMethodID(clazz, "setGroupWholeBan", "(IJZ)I");
	return env->CallStaticIntMethod(clazz, method, plugin_id, group, enable != FALSE);
}
