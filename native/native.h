#pragma once

#define CQAPI(ReturnType) extern "C" __declspec(dllexport) ReturnType __stdcall

typedef int (__stdcall* FuncInitialize)(int32_t);
typedef int (__stdcall* EvStartup)();
typedef int (__stdcall* EvExit)();
typedef int (__stdcall* EvEnable)();
typedef int (__stdcall* EvDisable)();
typedef int (__stdcall* EvPriMsg)(int32_t, int32_t, int64_t, const char*, int32_t);
typedef int (__stdcall* EvGroupMsg)(int32_t, int32_t, int64_t, int64_t, const char*, const char*, int32_t);

CQAPI(int32_t) CQ_addLog(int32_t, int32_t, const char*, const char*);
CQAPI(int32_t) CQ_canSendImage(int32_t);
CQAPI(int32_t) CQ_canSendRecord(int32_t);
CQAPI(int32_t) CQ_sendPrivateMsg(int32_t, int64_t, const char*);
CQAPI(int32_t) CQ_sendGroupMsg(int32_t, int64_t, const char*);
