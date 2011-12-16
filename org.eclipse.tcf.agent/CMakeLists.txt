cmake_minimum_required(VERSION 2.8)

set(CMAKE_COLOR_MAKEFILE OFF)

if (WIN32)
include_directories(system/Windows)
include_directories(machine/x86_64)
set(PLAT_SOURCES
    system/Windows/context-win32.c
    system/Windows/pthreads-win32.c
    system/Windows/windbgcache.c)
endif (WIN32)

include_directories(.)

add_library(tcf STATIC
    framework/asyncreq.c
    framework/base64.c
    framework/cache.c
    framework/channel.c
    framework/channel_pipe.c
    framework/channel_tcp.c
    framework/context.c
    framework/cpudefs.c
    framework/errors.c
    framework/events.c
    framework/exceptions.c
    framework/inputbuf.c
    framework/ip_ifc.c
    framework/json.c
    framework/mdep.c
    framework/myalloc.c
    framework/outputbuf.c
    framework/peer.c
    framework/plugins.c
    framework/protocol.c
    framework/proxy.c
    framework/signames.c
    framework/streams.c
    framework/trace.c
    framework/waitpid.c
    services/breakpoints.c
    services/diagnostics.c
    services/discovery.c
    services/discovery_udp.c
    services/dwarfcache.c
    services/dwarfexpr.c
    services/dwarfframe.c
    services/dwarfio.c
    services/dwarfreloc.c
    services/expressions.c
    services/filesystem.c
    services/linenumbers.c
    services/linenumbers_elf.c
    services/linenumbers_proxy.c
    services/linenumbers_win32.c
    services/memorymap.c
    services/memoryservice.c
    services/pathmap.c
    services/processes.c
    services/registers.c
    services/runctrl.c
    services/stacktrace.c
    services/streamsservice.c
    services/symbols.c
    services/symbols_elf.c
    services/symbols_proxy.c
    services/symbols_win32.c
    services/sysmon.c
    services/tcf_elf.c
    services/terminals.c
    main/cmdline.c
    main/server.c
    main/services.c
    main/test.c
    ${PLAT_SOURCES}
    )

add_executable(agent main/main.c)

target_link_libraries(agent tcf)

if(WIN32)
if(MSVC)
target_link_libraries(agent shell32.lib advapi32.lib Iphlpapi.lib WS2_32.lib version.lib)
else(MSVC) # MinGW
target_link_libraries(agent WS2_32 Iphlpapi)
endif(MSVC)
endif(WIN32)

#./main/main_client.c
#./main/main_log.c
#./main/main_lua.c
#./main/main_reg.c
#./main/main_va.c
#system/Darwin/context-darwin.c
#system/FreeBSD/context-freebsd.c
#system/GNU/Linux/context-linux.c
#system/VxWorks/context-vxworks.c
