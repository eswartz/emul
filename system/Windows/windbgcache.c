/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * This module provides access to Windows Portable Executable debug information.
 * Current implementation delegates all its job to DBGHELP.DLL.
 */

#include <config.h>

#if defined(WIN32) && !ENABLE_ELF

#include <assert.h>
#include <stdio.h>
#include <wchar.h>
#include <system/Windows/windbgcache.h>

static HINSTANCE dbghelp_dll = NULL;

static wchar_t * pathes[] = {
    L"%\\Debugging Tools for Windows (x86)\\dbghelp.dll",
    L"%\\Debugging Tools for Windows\\dbghelp.dll",
    L".\\dbghelp.dll",
    L"dbghelp.dll",
    NULL
};

static FARPROC GetProc(char * name) {
    if (dbghelp_dll == NULL) {
        wchar_t ** p = pathes;
        while (dbghelp_dll == NULL && *p != NULL) {
            if (**p == '%') {
                HKEY key;
                if (RegOpenKeyExW(HKEY_LOCAL_MACHINE,
                        L"SOFTWARE\\Microsoft\\Windows\\CurrentVersion",
                        0, KEY_READ, &key) == ERROR_SUCCESS) {
                    wchar_t buf[FILE_PATH_SIZE];
                    DWORD size = sizeof(buf);
                    memset(buf, 0, sizeof(buf));
                    if (RegQueryValueExW(key,
                            L"ProgramFilesDir",
                            NULL, NULL, (LPBYTE)buf, &size) == ERROR_SUCCESS) {
                        wcsncat(buf, *p + 1, FILE_PATH_SIZE - size / sizeof(wchar_t));
                        dbghelp_dll = LoadLibraryW(buf);
                    }
                    RegCloseKey(key);
                }
            }
            else {
                dbghelp_dll = LoadLibraryW(*p);
            }
            p++;
        }
        if (dbghelp_dll == NULL) {
            assert(GetLastError() != 0);
            return NULL;
        }
    }
    return GetProcAddress(dbghelp_dll, name);
}

BOOL SymInitialize(HANDLE hProcess, PCSTR UserSearchPath, BOOL fInvadeProcess) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PCSTR, BOOL);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymInitialize");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, UserSearchPath, fInvadeProcess);
}

BOOL SymSetOptions(DWORD Options) {
    typedef BOOL (FAR WINAPI * ProcType)(DWORD);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymSetOptions");
        if (proc == NULL) return 0;
    }
    return proc(Options);
}

BOOL SymGetLineFromName(HANDLE hProcess, PCSTR ModuleName, PCSTR FileName, DWORD dwLineNumber, PLONG plDisplacement, PIMAGEHLP_LINE Line) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PCSTR, PCSTR, DWORD, PLONG, PIMAGEHLP_LINE);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetLineFromName");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, ModuleName, FileName, dwLineNumber, plDisplacement, Line);
}

BOOL SymGetLineFromAddr(HANDLE hProcess, DWORD dwAddr, PDWORD pdwDisplacement, PIMAGEHLP_LINE Line) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, DWORD, PDWORD, PIMAGEHLP_LINE);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetLineFromAddr");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, dwAddr, pdwDisplacement, Line);
}

BOOL SymGetLineNext(HANDLE hProcess, PIMAGEHLP_LINE Line) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PIMAGEHLP_LINE);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetLineNext");
        if (proc == NULL) return 0;
    }
    assert(Line != NULL);
    assert(Line->Address != 0);
    return proc(hProcess, Line);
}

BOOL SymGetTypeInfo(HANDLE hProcess, DWORD64 ModBase, ULONG TypeId, IMAGEHLP_SYMBOL_TYPE_INFO GetType, PVOID pInfo) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, DWORD64, ULONG, IMAGEHLP_SYMBOL_TYPE_INFO, PVOID);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetTypeInfo");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, ModBase, TypeId, GetType, pInfo);
}

BOOL SymFromIndex(HANDLE hProcess, ULONG64 BaseOfDll, DWORD Index, PSYMBOL_INFO Symbol) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, ULONG64, DWORD, PSYMBOL_INFO);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymFromIndex");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll, Index, Symbol);
}

BOOL SymSetContext(HANDLE hProcess, PIMAGEHLP_STACK_FRAME StackFrame, PIMAGEHLP_CONTEXT Context) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PIMAGEHLP_STACK_FRAME, PIMAGEHLP_CONTEXT);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymSetContext");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, StackFrame, Context);
}

BOOL SymFromName(HANDLE hProcess, PCSTR Name, PSYMBOL_INFO Symbol) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PCSTR, PSYMBOL_INFO);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymFromName");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, Name, Symbol);
}

BOOL SymEnumSymbols(HANDLE hProcess, ULONG64 BaseOfDll, PCSTR Mask, PSYM_ENUMERATESYMBOLS_CALLBACK EnumSymbolsCallback, PVOID UserContext) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, ULONG64, PCSTR, PSYM_ENUMERATESYMBOLS_CALLBACK, PVOID);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymEnumSymbols");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll, Mask, EnumSymbolsCallback, UserContext);
}

BOOL SymGetTypeFromName(HANDLE hProcess, ULONG64 BaseOfDll, PCSTR Name, PSYMBOL_INFO Symbol) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, ULONG64, PCSTR, PSYMBOL_INFO);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetTypeFromName");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll, Name, Symbol);
}

DWORD64 SymGetModuleBase64(HANDLE hProcess, ULONG64 Address) {
    typedef DWORD64 (FAR WINAPI * ProcType)(HANDLE, ULONG64);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymGetModuleBase64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, Address);
}

BOOL SymLoadModule64(HANDLE hProcess, HANDLE hFile, PCSTR ImageName, PCSTR ModuleName, DWORD64 BaseOfDll, DWORD SizeOfDll) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, HANDLE, PCSTR, PCSTR, DWORD64, DWORD);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymLoadModule64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, hFile, ImageName, ModuleName, BaseOfDll, SizeOfDll);
}

BOOL SymUnloadModule64(HANDLE hProcess, DWORD64 BaseOfDll) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, DWORD64);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymUnloadModule64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll);
}

BOOL SymCleanup(HANDLE hProcess) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("SymCleanup");
        if (proc == NULL) return 0;
    }
    return proc(hProcess);
}

BOOL LocEnumerateLoadedModulesW64(HANDLE hProcess, PENUMLOADED_MODULES_CALLBACKW64 Callback, PVOID UserContext) {
    typedef BOOL (FAR WINAPI * ProcType)(HANDLE, PENUMLOADED_MODULES_CALLBACKW64, PVOID);
    static ProcType proc = NULL;
    if (proc == NULL) {
        proc = (ProcType)GetProc("EnumerateLoadedModulesW64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, Callback, UserContext);
}

#endif /* defined(WIN32) */
