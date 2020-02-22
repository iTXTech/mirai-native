@echo off
echo 生成用于JNI的头文件，请确保已经Build
javac -h .\native .\src\main\java\org\itxtech\mirainative\Bridge.java
