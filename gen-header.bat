@echo off
cd build\classes
"C:\Program Files (x86)\Java\jdk1.8.0_241\bin\javah" -classpath ".\java\main;.\kotlin\main" org.itxtech.mirainative.Bridge
copy /y org_itxtech_mirainative_Bridge.h ..\..\native
