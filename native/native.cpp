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
	HINSTANCE dll;
	native_plugin(int i, char* f)
	{
		id = i;
		file = f;
		dll = nullptr;
	}
};

vector<native_plugin> plugins;

// Bridge Events

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_loadNativePlugin(
	JNIEnv* env, jobject obj, jstring file, jint id)
{
	native_plugin plugin = {id, const_cast<char*>((env)->GetStringUTFChars(file, nullptr))};
	auto dll = LoadLibraryA(plugin.file);
	plugin.dll = dll;
	plugins.push_back(plugin);
	printf("Loading %s with id %d\n\nDD", plugin.file, plugin.id);
	typedef int(__stdcall *initfunc)();
	auto func = static_cast<initfunc>(GetProcAddress(dll, "_eventStartup"));
	if(func)
	{
		int result = func();
	}
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventStartup(JNIEnv* env, jobject obj)
{
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventExit(JNIEnv* env, jobject obj)
{
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventEnable(JNIEnv* env, jobject obj)
{
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventDisable(JNIEnv* env, jobject obj)
{
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventPrivateMessage(
	JNIEnv* env, jobject obj, jint sub_type, jint msg_id, jlong from_account, jstring msg, jint font)
{
}

// CQ APIs

void __stdcall CQ_addLog(int auth_code, int priority, char* type, char* content)
{
}

int __stdcall CQ_canSendImage(int)
{
	return 1;
}

int __stdcall CQ_canSendRecord(int)
{
	return 1;
}
