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

// Load

JavaVM* javaVM = nullptr;
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK)
	{
		return -1;
	}
	javaVM = vm;
	return JNI_VERSION_1_8;
}

JNIEnv* AttachJava()
{
	JNIEnv* java = nullptr;
	if (javaVM)
	{
		int getEnvStat = javaVM->GetEnv((void**)&java, JNI_VERSION_1_8);
		if (getEnvStat == JNI_EDETACHED)
		{
			JavaVMAttachArgs args = {JNI_VERSION_1_8, 0, 0};
			javaVM->AttachCurrentThread((void**)&java, &args);
		}
		else if (getEnvStat == JNI_EVERSION)
		{
		}
	}
	return java;
}

// Bridge Events

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_loadNativePlugin(
	JNIEnv* env, jobject obj, jstring file, jint id)
{
	native_plugin plugin = {id, const_cast<char*>(env->GetStringUTFChars(file, nullptr))};
	const auto dll = LoadLibraryA(plugin.file);
	plugin.dll = dll;
	plugins.push_back(plugin);

	//调用 Initialize
	const auto init = FuncInitialize(GetProcAddress(dll, "Initialize"));
	if (init)
	{
		init(plugin.id);
	}

	const auto info = FuncAppInfo(GetProcAddress(dll, "AppInfo"));
	if (info)
	{
		auto jstr = GbToJstring(env, info());
		auto clazz = env->FindClass("org/itxtech/mirainative/Bridge");
		auto method = env->GetStaticMethodID(clazz, "updatePluginInfo", "(ILjava/lang/String;)V");
		env->CallStaticVoidMethod(clazz, method, plugin.id, jstr);
		env->DeleteLocalRef(jstr);
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventStartup(JNIEnv* env, jobject obj)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = static_cast<EvStartup>(GetProcAddress(plugin.dll, "_eventStartup"));
			if (ev)
			{
				ev();
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventExit(JNIEnv* env, jobject obj)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = static_cast<EvExit>(GetProcAddress(plugin.dll, "_eventExit"));
			if (ev)
			{
				ev();
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventEnable(JNIEnv* env, jobject obj)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = static_cast<EvEnable>(GetProcAddress(plugin.dll, "_eventEnable"));
			if (ev)
			{
				ev();
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventDisable(JNIEnv* env, jobject obj)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = static_cast<EvDisable>(GetProcAddress(plugin.dll, "_eventDisable"));
			if (ev)
			{
				ev();
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_disablePlugin(JNIEnv* env, jobject obj, jint id)
{
	for (auto& plugin : plugins)
	{
		if (plugin.id == id)
		{
			plugin.enabled = false;
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_enablePlugin(JNIEnv* env, jobject obj, jint id)
{
	for (auto& plugin : plugins)
	{
		if (plugin.id == id)
		{
			plugin.enabled = true;
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventPrivateMessage(
	JNIEnv* env, jobject obj, jint sub_type, jint msg_id, jlong from_account, jstring msg, jint font)
{
	char* m = JstringToGb(env, msg);
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = EvPriMsg(GetProcAddress(plugin.dll, "_eventPrivateMsg"));
			if (ev)
			{
				if (ev(sub_type, msg_id, from_account, m, font) == 1) //插件拦截事件
				{
					break;
				}
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventGroupMessage(
	JNIEnv* env, jobject obj, jint sub_type, jint msg_id, jlong from_group, jlong from_account, jstring from_anonymous,
	jstring msg, jint font)
{
	char* a = JstringToGb(env, from_anonymous);
	char* m = JstringToGb(env, msg);
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = EvGroupMsg(GetProcAddress(plugin.dll, "_eventGroupMsg"));
			if (ev)
			{
				if (ev(sub_type, msg_id, from_group, from_account, a, m, font) == 1) //插件拦截事件
				{
					break;
				}
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventGroupAdmin(
	JNIEnv* env, jobject obj, jint sub_type, jint timestamp, jlong group, jlong member)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = EvGroupAdmin(GetProcAddress(plugin.dll, "_eventSystem_GroupAdmin"));
			if (ev)
			{
				if (ev(sub_type, timestamp, group, member) == 1) //插件拦截事件
				{
					break;
				}
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventGroupMemberLeave(
	JNIEnv* env, jobject obj, jint sub_type, jint timestamp, jlong group, jlong admin, jlong member)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = EvGroupMemberLeave(GetProcAddress(plugin.dll, "_eventSystem_GroupMemberDecrease"));
			if (ev)
			{
				if (ev(sub_type, timestamp, group, admin, member) == 1) //插件拦截事件
				{
					break;
				}
			}
		}
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventGroupBan(
	JNIEnv* env, jobject obj, jint sub_type, jint timestamp, jlong group, jlong admin, jlong member, jlong duration)
{
	for (auto const& plugin : plugins)
	{
		if (plugin.enabled)
		{
			const auto ev = EvGroupBan(GetProcAddress(plugin.dll, "_eventSystem_GroupBan"));
			if (ev)
			{
				if (ev(sub_type, timestamp, group, admin, member, duration) == 1) //插件拦截事件
				{
					break;
				}
			}
		}
	}
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
	return sendMsg(plugin_id, account, msg, "sendMessageToFriend");
}

int32_t __stdcall CQ_sendGroupMsg(int32_t plugin_id, int64_t group, const char* msg)
{
	return sendMsg(plugin_id, group, msg, "sendMessageToGroup");
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
	jstring result = jstring(env->CallStaticObjectMethod(clazz, method, plugin_id));
	return JstringToGb(env, result);
}
