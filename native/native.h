#pragma once

#define CQAPI(ReturnType) extern "C" __declspec(dllexport) ReturnType __stdcall

typedef int32_t (__stdcall* IntMethod)();
typedef const char* (__stdcall* StringMethod)();

typedef int32_t (__stdcall* FuncInitialize)(int32_t);
typedef const char* (__stdcall* FuncAppInfo)();

typedef int32_t (__stdcall* EvStartup)();
typedef int32_t (__stdcall* EvExit)();
typedef int32_t (__stdcall* EvEnable)();
typedef int32_t (__stdcall* EvDisable)();
typedef int32_t (__stdcall* EvPriMsg)(int32_t, int32_t, int64_t, const char*, int32_t);
typedef int32_t (__stdcall* EvGroupMsg)(int32_t, int32_t, int64_t, int64_t, const char*, const char*, int32_t);
typedef int32_t (__stdcall* EvGroupAdmin)(int32_t, int32_t, int64_t, int64_t);
typedef int32_t (__stdcall* EvGroupMemberLeave)(int32_t, int32_t, int64_t, int64_t, int64_t);
typedef int32_t (__stdcall* EvGroupBan)(int32_t, int32_t, int64_t, int64_t, int64_t, int64_t);

CQAPI(int32_t) CQ_addLog(int32_t, int32_t, const char*, const char*);
CQAPI(int32_t) CQ_canSendImage(int32_t);
CQAPI(int32_t) CQ_canSendRecord(int32_t);
CQAPI(int32_t) CQ_sendPrivateMsg(int32_t, int64_t, const char*);
CQAPI(int32_t) CQ_sendGroupMsg(int32_t, int64_t, const char*);
CQAPI(int32_t) CQ_setFatal(int32_t, const char*);
CQAPI(const char*) CQ_getAppDirectory(int32_t);
CQAPI(int64_t) CQ_getLoginQQ(int32_t);
CQAPI(const char*) CQ_getLoginNick(int32_t);
