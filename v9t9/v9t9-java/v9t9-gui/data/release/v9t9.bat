@echo off

set VMARGS=-Xmx256M -Dlog4j.configuration="jar:file:%~dp0%/v9t9j.jar!/log4j.properties"

rem set VMARGS=%VMARGS% -Dlog4j.configuration="jar:file:%~dp0%/v9t9j.jar!/debug.properties"

java -version
if ERRORLEVEL 1 (
	@echo Java not installed.  Please install it and try again.
	pause
	start http://java.com
) else (
	rem apparently Win7 does not like this format anymore... 
	rem java %VMARGS% -jar "%~dp0%\v9t9j.jar" %*
	
	java %VMARGS% -jar "%~dp0%\v9t9j.jar"
	pause
)