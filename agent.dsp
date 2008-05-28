# Microsoft Developer Studio Project File - Name="agent" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 5.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Console Application" 0x0103

CFG=agent - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "agent.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "agent.mak" CFG="agent - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "agent - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "agent - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 

# Begin Project
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "agent - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /machine:I386
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib WS2_32.lib Iphlpapi.lib dbghelp.lib /nologo /subsystem:console /machine:I386

!ELSEIF  "$(CFG)" == "agent - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /Zi /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /YX /FD /c
# ADD BASE RSC /l 0x409 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /debug /machine:I386 /pdbtype:sept
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib WS2_32.lib Iphlpapi.lib  dbghelp.lib /nologo /subsystem:console /debug /machine:I386 /pdbtype:sept

!ENDIF 

# Begin Target

# Name "agent - Win32 Release"
# Name "agent - Win32 Debug"
# Begin Source File

SOURCE=.\asyncreq.c
# End Source File
# Begin Source File

SOURCE=.\asyncreq.h
# End Source File
# Begin Source File

SOURCE=.\base64.c
# End Source File
# Begin Source File

SOURCE=.\base64.h
# End Source File
# Begin Source File

SOURCE=.\breakpoints.c
# End Source File
# Begin Source File

SOURCE=.\breakpoints.h
# End Source File
# Begin Source File

SOURCE=.\channel.c
# End Source File
# Begin Source File

SOURCE=.\channel.h
# End Source File
# Begin Source File

SOURCE=.\channel_tcp.c
# End Source File
# Begin Source File

SOURCE=.\channel_tcp.h
# End Source File
# Begin Source File

SOURCE=.\cmdline.c
# End Source File
# Begin Source File

SOURCE=.\cmdline.h
# End Source File
# Begin Source File

SOURCE=.\config.h
# End Source File
# Begin Source File

SOURCE=.\context.c
# End Source File
# Begin Source File

SOURCE=.\context.h
# End Source File
# Begin Source File

SOURCE=.\diagnostics.c
# End Source File
# Begin Source File

SOURCE=.\diagnostics.h
# End Source File
# Begin Source File

SOURCE=.\discovery.c
# End Source File
# Begin Source File

SOURCE=.\discovery.h
# End Source File
# Begin Source File

SOURCE=.\discovery_help.c
# End Source File
# Begin Source File

SOURCE=.\discovery_help.h
# End Source File
# Begin Source File

SOURCE=.\discovery_udp.c
# End Source File
# Begin Source File

SOURCE=.\discovery_udp.h
# End Source File
# Begin Source File

SOURCE=.\dwarf.h
# End Source File
# Begin Source File

SOURCE=.\dwarfio.c
# End Source File
# Begin Source File

SOURCE=.\dwarfio.h
# End Source File
# Begin Source File

SOURCE=.\elf.c
# End Source File
# Begin Source File

SOURCE=.\elf.h
# End Source File
# Begin Source File

SOURCE=.\errors.c
# End Source File
# Begin Source File

SOURCE=.\errors.h
# End Source File
# Begin Source File

SOURCE=.\events.c
# End Source File
# Begin Source File

SOURCE=.\events.h
# End Source File
# Begin Source File

SOURCE=.\exceptions.c
# End Source File
# Begin Source File

SOURCE=.\exceptions.h
# End Source File
# Begin Source File

SOURCE=.\expressions.c
# End Source File
# Begin Source File

SOURCE=.\expressions.h
# End Source File
# Begin Source File

SOURCE=.\filesystem.c
# End Source File
# Begin Source File

SOURCE=.\filesystem.h
# End Source File
# Begin Source File

SOURCE=.\inputbuf.c
# End Source File
# Begin Source File

SOURCE=.\inputbuf.h
# End Source File
# Begin Source File

SOURCE=.\ip_ifc.c
# End Source File
# Begin Source File

SOURCE=.\ip_ifc.h
# End Source File
# Begin Source File

SOURCE=.\json.c
# End Source File
# Begin Source File

SOURCE=.\json.h
# End Source File
# Begin Source File

SOURCE=.\linenumbers.h
# End Source File
# Begin Source File

SOURCE=.\linenumbers_elf.c
# End Source File
# Begin Source File

SOURCE=.\linenumbers_win32.c
# End Source File
# Begin Source File

SOURCE=.\link.h
# End Source File
# Begin Source File

SOURCE=.\main.c
# End Source File
# Begin Source File

SOURCE=.\mdep.c
# End Source File
# Begin Source File

SOURCE=.\mdep.h
# End Source File
# Begin Source File

SOURCE=.\memoryservice.c
# End Source File
# Begin Source File

SOURCE=.\memoryservice.h
# End Source File
# Begin Source File

SOURCE=.\myalloc.c
# End Source File
# Begin Source File

SOURCE=.\myalloc.h
# End Source File
# Begin Source File

SOURCE=.\peer.c
# End Source File
# Begin Source File

SOURCE=.\peer.h
# End Source File
# Begin Source File

SOURCE=.\processes.c
# End Source File
# Begin Source File

SOURCE=.\processes.h
# End Source File
# Begin Source File

SOURCE=.\protocol.c
# End Source File
# Begin Source File

SOURCE=.\protocol.h
# End Source File
# Begin Source File

SOURCE=.\proxy.c
# End Source File
# Begin Source File

SOURCE=.\proxy.h
# End Source File
# Begin Source File

SOURCE=.\registers.c
# End Source File
# Begin Source File

SOURCE=.\registers.h
# End Source File
# Begin Source File

SOURCE=.\runctrl.c
# End Source File
# Begin Source File

SOURCE=.\runctrl.h
# End Source File
# Begin Source File

SOURCE=.\stacktrace.c
# End Source File
# Begin Source File

SOURCE=.\stacktrace.h
# End Source File
# Begin Source File

SOURCE=.\streams.c
# End Source File
# Begin Source File

SOURCE=.\streams.h
# End Source File
# Begin Source File

SOURCE=.\symbols.h
# End Source File
# Begin Source File

SOURCE=.\symbols_elf.c
# End Source File
# Begin Source File

SOURCE=.\symbols_win32.c
# End Source File
# Begin Source File

SOURCE=.\sysmon.c
# End Source File
# Begin Source File

SOURCE=.\sysmon.h
# End Source File
# Begin Source File

SOURCE=.\tcf.h
# End Source File
# Begin Source File

SOURCE=.\test.c
# End Source File
# Begin Source File

SOURCE=.\test.h
# End Source File
# Begin Source File

SOURCE=.\TODO.txt
# End Source File
# Begin Source File

SOURCE=.\trace.c
# End Source File
# Begin Source File

SOURCE=.\trace.h
# End Source File
# End Target
# End Project
