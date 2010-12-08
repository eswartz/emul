@echo off
REM The Visual C++ compiler must be in your path
REM This is handled automatically with CDT's Visual C++ integration

if exist msvc-release goto checkmk
mkdir msvc-release

:checkmk
cd msvc-release
if exist Makefile goto dobuild
@echo on
cmake -DCMAKE_BUILD_TYPE=Release ..\..

:dobuild
@echo on
nmake %*
