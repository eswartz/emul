@echo off
cd %~dp0%

set VMARGS=-Xmx256M -Dlog4j.configuration=jar:file:./v9t9j.jar!/log4j.properties 

rem set VMARGS=%VMARGS% -Dlog4j.configuration=jar:file:./v9t9j.jar!/debug.properties

java %VMARGS% -jar v9t9j.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
