#include "native.h"

#include "cq_events.h"
#include "cq_apis.h"
#include "mirai_apis.h"

// Load

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_8) != JNI_OK)
	{
		return -1;
	}
	jvm = vm;
	auto b = env->FindClass("org/itxtech/mirainative/Bridge");
	bclz = jclass(env->NewGlobalRef(b));

	return JNI_VERSION_1_8;
}

// Utilities

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_shutdown(JNIEnv* env, jclass clz)
{
	env->DeleteGlobalRef(bclz);
	running = false;
	mem_thread.join();
	return 0;
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_setCurrentDirectory(JNIEnv* env, jclass clz, jbyteArray dir)
{
	SetCurrentDirectoryA(ByteArrayToString(env, dir).c_str());
	return 0;
}

// Plugin

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_loadNativePlugin(
	JNIEnv* env, jclass clz, jbyteArray file, jint id)
{
	native_plugin plugin = {id, const_cast<char*>(ByteArrayToChars(env, file))};
	const auto dll = LoadLibraryA(plugin.file);

	if (dll != nullptr)
	{
		plugin.dll = dll;
		plugins[id] = plugin;

		const auto init = FuncInitialize(GetProcAddress(dll, "Initialize"));
		if (init)
		{
			init(plugin.id);
		}

		return 0;
	}
	return GetLastError();
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_freeNativePlugin(
	JNIEnv* env, jclass clz, jint id)
{
	auto r = FreeLibrary(plugins[id].dll);
	free((void*)plugins[id].file);
	if (r != FALSE)
	{
		return 0;
	}
	return GetLastError();
}

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_processMessage(JNIEnv* env, jclass clz)
{
	MSG msg;
	while (PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE) > 0)
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}
}

JNIEXPORT jint JNICALL Java_org_itxtech_mirainative_Bridge_callIntMethod(
	JNIEnv* env, jclass clz, jint id, jbyteArray method)
{
	const auto m = IntMethod(GetMethod(env, id, method));
	if (m)
	{
		return m();
	}
	return 0;
}

JNIEXPORT jbyteArray JNICALL Java_org_itxtech_mirainative_Bridge_callStringMethod(
	JNIEnv* env, jclass clz, jint id, jbyteArray method)
{
	const char* rtn = "";
	const auto m = StringMethod(GetMethod(env, id, method));
	if (m)
	{
		rtn = m();
	}
	return CharsToByteArray(env, rtn);
}
