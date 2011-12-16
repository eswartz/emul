@echo off
REM The Visual C++ compiler must be in your path
REM This is handled automatically with CDT's Visual C++ integration

if exist msvc-debug goto checkmk
mkdir msvc-debug

:checkmk
cd msvc-debug
if exist Makefile goto dobuild
@echo on
cmake -DCMAKE_BUILD_TYPE=Debug ..\..

:dobuild
@echo on
nmake %*
