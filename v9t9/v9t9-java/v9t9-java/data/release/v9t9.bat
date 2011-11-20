@echo off
cd %~dp0%
java -Djava.library.path=lwjgl/win32 -Xmx256M -jar v9t9j.jar --swtgl
