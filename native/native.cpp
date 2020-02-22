#include <jni.h>
#include "org_itxtech_mirainative_Bridge.h"
#include "native.h"

// Bridge Events

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_loadDynamicLibraries(JNIEnv* env, jobject obj, jstring dir)
{
	printf("GG");
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

JNIEXPORT void JNICALL Java_org_itxtech_mirainative_Bridge_eventPrivateMessage(JNIEnv* env, jobject obj, jint sub_type, jint msg_id, jlong from_account, jstring msg, jint font)
{
	
}

// CQ APIs

void __stdcall CQ_addLog(int auth_code, int priority, char* type, char* content)
{
	
}

int __stdcall CQ_canSendImage(int auth_code)
{
	return 1;
}
