@echo off
cd %~dp0%

set VMARGS=-Xmx256M 
rem set VMARGS=%VMARGS% -Dlog4j.configuration=jar:file:./v9t9j.jar!/debug.properties

java %VMARGS% -jar v9t9j.jar
