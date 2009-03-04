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

#include "mdep.h"
#include "config.h"

#include <assert.h>
#include <stdio.h>

#if defined(WIN32)

#include "windbgcache.h"

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
        if (dbghelp_dll == NULL) return NULL;
    }
    return GetProcAddress(dbghelp_dll, name);
}
 
extern BOOL SymInitialize(HANDLE hProcess, PCSTR UserSearchPath, BOOL fInvadeProcess) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymInitialize");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, UserSearchPath, fInvadeProcess);
}

extern BOOL SymSetOptions(DWORD Options) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymSetOptions");
        if (proc == NULL) return 0;
    }
    return proc(Options);
}

extern BOOL SymGetLineFromName(HANDLE hProcess, PCSTR ModuleName, PCSTR FileName, DWORD dwLineNumber, PLONG plDisplacement, PIMAGEHLP_LINE Line) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymGetLineFromName");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, ModuleName, FileName, dwLineNumber, plDisplacement, Line);
}

extern BOOL SymGetLineFromAddr(HANDLE hProcess, DWORD dwAddr, PDWORD pdwDisplacement, PIMAGEHLP_LINE Line) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymGetLineFromAddr");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, dwAddr, pdwDisplacement, Line);
}

extern BOOL SymGetLineNext(HANDLE hProcess, PIMAGEHLP_LINE Line) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymGetLineNext");
        if (proc == NULL) return 0;
    }
    assert(Line != NULL);
    assert(Line->Address != 0);
    return proc(hProcess, Line);
}

extern BOOL SymGetTypeInfo(HANDLE hProcess, DWORD64 ModBase, ULONG TypeId, IMAGEHLP_SYMBOL_TYPE_INFO GetType, PVOID pInfo) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymGetTypeInfo");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, ModBase, TypeId, GetType, pInfo);
}

extern BOOL SymFromIndex(HANDLE hProcess, ULONG64 BaseOfDll, DWORD Index, PSYMBOL_INFO Symbol) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymFromIndex");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll, Index, Symbol);
}

extern BOOL SymSetContext(HANDLE hProcess, PIMAGEHLP_STACK_FRAME StackFrame, PIMAGEHLP_CONTEXT Context) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymSetContext");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, StackFrame, Context);
}

extern BOOL SymFromName(HANDLE hProcess, PCSTR Name, PSYMBOL_INFO Symbol) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymFromName");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, Name, Symbol);
}

extern BOOL SymEnumSymbols(HANDLE hProcess, ULONG64 BaseOfDll, PCSTR Mask, PSYM_ENUMERATESYMBOLS_CALLBACK EnumSymbolsCallback, PVOID UserContext) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymEnumSymbols");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll, Mask, EnumSymbolsCallback, UserContext);
}

extern BOOL SymLoadModule64(HANDLE hProcess, HANDLE hFile, PCSTR ImageName, PCSTR ModuleName, DWORD64 BaseOfDll, DWORD SizeOfDll) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymLoadModule64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, hFile, ImageName, ModuleName, BaseOfDll, SizeOfDll);
}

extern BOOL SymUnloadModule64(HANDLE hProcess, DWORD64 BaseOfDll) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymUnloadModule64");
        if (proc == NULL) return 0;
    }
    return proc(hProcess, BaseOfDll);
}

extern BOOL SymCleanup(HANDLE hProcess) {
    static FARPROC proc = NULL;
    if (proc == NULL) {
        proc = GetProc("SymCleanup");
        if (proc == NULL) return 0;
    }
    return proc(hProcess);
}


#endif
