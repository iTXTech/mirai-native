#pragma once

#include <thread>
#include <map>
#include <mutex>
#include <queue>
#include "org_itxtech_mirainative_Bridge.h"
#include <Windows.h>
#include <jni.h>
#include <string>

using namespace std;

#define CQAPI(ReturnType, Name, Size) __pragma(comment(linker, "/EXPORT:" #Name "=_" #Name "@" #Size))\
 extern "C" __declspec(dllexport) ReturnType __stdcall Name

typedef int32_t (__stdcall* IntMethod)();
typedef const char* (__stdcall* StringMethod)();
typedef int32_t (__stdcall* FuncInitialize)(int32_t);

typedef int32_t (__stdcall* EvPriMsg)(int32_t, int32_t, int64_t, const char*, int32_t);
typedef int32_t (__stdcall* EvGroupMsg)(int32_t, int32_t, int64_t, int64_t, const char*, const char*, int32_t);
typedef int32_t (__stdcall* EvGroupAdmin)(int32_t, int32_t, int64_t, int64_t);
typedef int32_t (__stdcall* EvGroupMember)(int32_t, int32_t, int64_t, int64_t, int64_t);
typedef int32_t (__stdcall* EvGroupBan)(int32_t, int32_t, int64_t, int64_t, int64_t, int64_t);
typedef int32_t (__stdcall* EvRequestAddGroup)(int32_t, int32_t, int64_t, int64_t, const char*, const char*);
typedef int32_t (__stdcall* EvRequestAddFriend)(int32_t, int32_t, int64_t, const char*, const char*);
typedef int32_t (__stdcall* EvFriendAdd)(int32_t, int32_t, int64_t);

struct native_plugin
{
	int id;
	const char* file;
	HMODULE dll;
	bool enabled;

	native_plugin()
	{
		id = -1;
		file = "";
		dll = nullptr;
		enabled = false;
	}

	native_plugin(int i, char* f)
	{
		id = i;
		file = f;
		dll = nullptr;
		enabled = true;
	}
};

// Global var

map<int, native_plugin> plugins;
priority_queue<pair<time_t, const char*>> mem_queue;
mutex mem_mutex;
bool running = true;

JavaVM* jvm = nullptr;
jclass bclz = nullptr;


thread mem_thread([]
{
	while (running)
	{
		{
			unique_lock<mutex> lock(mem_mutex);
			while (!mem_queue.empty() && time(nullptr) - mem_queue.top().first > 300)
			{
				free((void*)mem_queue.top().second);
				mem_queue.pop();
			}
		}
		this_thread::sleep_for(500ms);
	}
});

const char* delay_mem_free(const char* str)
{
	unique_lock<mutex> lock(mem_mutex);
	mem_queue.push({time(nullptr), str});
	return str;
}

// Strings

// Memory returned from this function need to be freed using free()
const char* ByteArrayToChars(JNIEnv* env, jbyteArray arr)
{
	jsize len = env->GetArrayLength(arr);
	char* buffer = (char*)malloc(len + 1);
	jbyte* elements = env->GetByteArrayElements(arr, nullptr);
	memcpy(buffer, elements, len);
	buffer[len] = '\0';
	env->ReleaseByteArrayElements(arr, elements, JNI_ABORT);
	return buffer;
}

string ByteArrayToString(JNIEnv* env, jbyteArray arr)
{
	const auto* buf = ByteArrayToChars(env, arr);
	string s(buf);
	free((void*)buf);
	return s;
}

jbyteArray CharsToByteArray(JNIEnv* env, const char* str)
{
	if (str == nullptr)
	{
		str = "\0";
	}
	auto len = strlen(str);
	auto arr = env->NewByteArray(len);
	env->SetByteArrayRegion(arr, 0, len, reinterpret_cast<const jbyte*>(str));
	return arr;
}

FARPROC GetMethod(JNIEnv* env, jint id, jbyteArray method)
{
	return GetProcAddress(plugins[id].dll, ByteArrayToString(env, method).c_str());
}

JNIEnv* attach_java()
{
	JNIEnv* env = nullptr;
	if (jvm)
	{
		const int stat = jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8);
		if (stat == JNI_EDETACHED)
		{
			JavaVMAttachArgs args = {JNI_VERSION_1_8, nullptr, nullptr};
			jvm->AttachCurrentThread(reinterpret_cast<void**>(&env), &args);
		}
	}
	return env;
}

void detach_java()
{
	jvm->DetachCurrentThread();
}
