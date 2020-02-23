#pragma once

#define CQAPI(ReturnType) extern "C" __declspec(dllexport) ReturnType __stdcall

CQAPI(void) CQ_addLog(int, int, char*, char*);
CQAPI(int) CQ_canSendImage(int);
CQAPI(int) CQ_canSendRecord(int);
