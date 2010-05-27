/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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

#include <config.h>

#if SERVICE_SysMonitor

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <assert.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <framework/protocol.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <framework/json.h>
#include <framework/context.h>
#include <framework/errors.h>
#include <services/sysmon.h>

static const char SYS_MON[] = "SysMonitor";

#if defined(_WRS_KERNEL)

#  error "SysMonitor service is not supported for VxWorks"

#elif defined(__FreeBSD__) || defined(__NetBSD__)

#  error "SysMonitor service is not supported for BSD"

#elif defined(__APPLE__)

#include <unistd.h>
#include <dirent.h>
#include <pwd.h>
#include <grp.h>
#include <stdbool.h>
#include <sys/sysctl.h>
#include <mach/mach.h>
#include <mach/task_info.h>
#include <mach/thread_info.h>

typedef struct kinfo_proc kinfo_proc;

static void write_string_array(OutputStream * out, char **ap, int len) {
    int cnt;

    write_stream(out, '[');
    for (cnt = 0; cnt < len; cnt++) {
        if (cnt > 0) write_stream(out, ',');
        json_write_string(out, ap[cnt]);
    }
    write_stream(out, ']');
}

static void free_array(char **ap, int len) {
    int c;
    for (c = 0; c < len; c++) {
        free(*ap++);
    }
    free(ap);
}

/*
 * Get kernel process information for all processes.
 */
static int get_allprocesses(kinfo_proc **kprocs, int *nprocs)
{
    size_t          len;
    kinfo_proc *    kp;
    int             mib_name[] = {CTL_KERN, KERN_PROC, KERN_PROC_ALL};
    int             mib_len = 3;

    if (sysctl(mib_name, mib_len, NULL, &len, NULL, 0) < 0) {
        return -1;
    }

    kp = (struct kinfo_proc *)malloc(len);

    if (sysctl(mib_name, mib_len, kp, &len, NULL, 0) < 0) {
        free(kp);
        return -1;
    }

    *kprocs = kp;
    *nprocs = len / sizeof(kinfo_proc);
    return 0;
}

/*
 * Get kernel process information for a specified pid.
 */
static kinfo_proc *get_process(pid_t pid)
{
    kinfo_proc *        kp;
    int                 mib_name[] = {CTL_KERN, KERN_PROC, KERN_PROC_PID, 0};
    int                 mib_len = 4;
    size_t              len = sizeof(kinfo_proc);

    mib_name[3] = pid;

    kp = malloc(len);
    if (kp == NULL) {
        return NULL;
    }

    if (sysctl(mib_name, mib_len, kp, &len, NULL, 0) < 0) {
        free(kp);
        return NULL;
    }

    return kp;
}

static void write_context(OutputStream * out, char * id, char * parent_id, kinfo_proc * p) {
    struct passwd * pwd;
    struct group *  grp;

    write_stream(out, '{');

    json_write_string(out, "UID");
    write_stream(out, ':');
    json_write_long(out, p->kp_eproc.e_ucred.cr_uid);
    write_stream(out, ',');

    json_write_string(out, "UGID");
    write_stream(out, ':');
    json_write_long(out, p->kp_eproc.e_pcred.p_rgid);
    write_stream(out, ',');

    pwd = getpwuid(p->kp_eproc.e_ucred.cr_uid);
    if (pwd != NULL) {
        json_write_string(out, "UserName");
        write_stream(out, ':');
        json_write_string(out, pwd->pw_name);
        write_stream(out, ',');
    }

    grp = getgrgid(p->kp_eproc.e_pcred.p_rgid);
    if (grp != NULL) {
        json_write_string(out, "GroupName");
        write_stream(out, ':');
        json_write_string(out, grp->gr_name);
        write_stream(out, ',');
    }

    json_write_string(out, "File");
    write_stream(out, ':');
    json_write_string(out, p->kp_proc.p_comm);
    write_stream(out, ',');

    json_write_string(out, "PID");
    write_stream(out, ':');
    json_write_long(out, p->kp_proc.p_pid);
    write_stream(out, ',');

    json_write_string(out, "State");
    write_stream(out, ':');
    write_stream(out, '"');
    json_write_char(out, p->kp_proc.p_stat);
    write_stream(out, '"');
    write_stream(out, ',');

    if (p->kp_eproc.e_ppid > 0) {
        json_write_string(out, "PPID");
        write_stream(out, ':');
        json_write_long(out, p->kp_eproc.e_ppid);
        write_stream(out, ',');
    }

    json_write_string(out, "PGRP");
    write_stream(out, ':');
    json_write_long(out, p->kp_eproc.e_pgid);
    write_stream(out, ',');

    if (p->kp_eproc.e_tpgid > 0) {
        json_write_string(out, "TGID");
        write_stream(out, ':');
        json_write_long(out, p->kp_eproc.e_tpgid);
        write_stream(out, ',');
    }

    json_write_string(out, "Flags");
    write_stream(out, ':');
    json_write_long(out, p->kp_proc.p_flag);
    write_stream(out, ',');

    json_write_string(out, "UTime");
    write_stream(out, ':');
    json_write_uint64(out, p->kp_proc.p_uticks);
    write_stream(out, ',');

    json_write_string(out, "STime");
    write_stream(out, ':');
    json_write_uint64(out, p->kp_proc.p_sticks);
    write_stream(out, ',');

    json_write_string(out, "Priority");
    write_stream(out, ':');
    json_write_long(out, (long)p->kp_proc.p_priority);
    write_stream(out, ',');

    if (p->kp_proc.p_nice != 0) {
        json_write_string(out, "Nice");
        write_stream(out, ':');
        json_write_long(out, (long)p->kp_proc.p_nice);
        write_stream(out, ',');
    }

    if (parent_id != NULL && parent_id[0] != 0) {
        json_write_string(out, "ParentID");
        write_stream(out, ':');
        json_write_string(out, parent_id);
        write_stream(out, ',');
    }

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    char            id[256];
    pid_t           pid = 0;
    pid_t           parent = 0;
    int             err = 0;
    kinfo_proc *    p;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    p = get_process(pid);
    if (p == NULL) err = errno;

    write_errno(&c->out, err);

    if (err == 0 && pid != 0) {
        char *parent_id;
        asprintf(&parent_id, "%d", parent);
        write_context(&c->out, id, parent == 0 ? NULL : parent_id, p);
        write_stream(&c->out, 0);
        free(parent_id);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    int     err = 0;
    char    id[256];
    pid_t   pid = 0;
    pid_t   parent = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);

    if (parent != 0) {
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
        if (pid == 0) {
            int             np;
            int             i;
            int             n;
            kinfo_proc *    p;

            if (get_allprocesses(&p, &np) < 0) {
                write_errno(&c->out, errno);
                write_stringz(&c->out, "null");
            }
            else {
                write_errno(&c->out, 0);
                write_stream(&c->out, '[');
                for (n = 0, i = 0; i < np; i++) {
                    if (p->kp_proc.p_pid != 0) {
                        if (n > 0) write_stream(&c->out, ',');
                        json_write_string(&c->out, pid2id(p->kp_proc.p_pid, 0));
                        n++;
                    }
                    p++;
                }
                write_stream(&c->out, ']');
                write_stream(&c->out, 0);
            }
        }
        else {
            kinfo_proc *    p;

            p = get_process(pid);
            if (p == NULL) {
                write_errno(&c->out, errno);
                write_stringz(&c->out, "null");
            }
            else {
                task_port_t task;

                if (task_for_pid(mach_task_self(), pid, &task) != KERN_SUCCESS) {
                    /*
                     * User is probably not in procmod group
                     */
                    write_errno(&c->out, 0);
                    write_stringz(&c->out, "[]");
                }
                else {
                    unsigned int        thread_count;
                    thread_port_array_t thread_list;

                    if (task_threads(task, &thread_list, &thread_count) != KERN_SUCCESS) {
                        write_errno(&c->out, errno);
                        write_stringz(&c->out, "null");
                    }
                    else {
                        int cnt;
                        write_errno(&c->out, 0);
                        write_stream(&c->out, '[');
                        for (cnt = 0; cnt < thread_count; cnt++) {
                            if (cnt > 0) write_stream(&c->out, ',');
                            json_write_string(&c->out, pid2id(thread_list[cnt], pid));
                        }
                        write_stream(&c->out, ']');
                        write_stream(&c->out, 0);
                    }
                }
            }
        }
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_command_line(char * token, Channel * c) {
    int             err;
    char            id[256];
    pid_t           pid;
    pid_t           parent;
    kinfo_proc *    p;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        p = get_process(pid);
        if (p == NULL) err = errno;
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    write_errno(&c->out, err);

    if (err != 0) {
        write_stringz(&c->out, "null");
    } else {
        write_stringz(&c->out, p->kp_proc.p_comm);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_environment(char * token, Channel * c) {
    char            id[256];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);
    write_errno(&c->out, 0);
    write_stringz(&c->out, "[]");

    write_stream(&c->out, MARKER_EOM);
}

#elif defined(WIN32)

#include <windows.h>
#include <wchar.h>

typedef struct _UNICODE_STRING {
    USHORT Length;
    USHORT MaximumLength;
    PWSTR  Buffer;
} UNICODE_STRING;

typedef enum _PROCESSINFOCLASS {
    ProcessBasicInformation = 0,
    ProcessWow64Information = 26
} PROCESSINFOCLASS;

typedef struct _RTL_DRIVE_LETTER_CURDIR {
    USHORT                  Flags;
    USHORT                  Length;
    ULONG                   TimeStamp;
    UNICODE_STRING          DosPath;
} RTL_DRIVE_LETTER_CURDIR, *PRTL_DRIVE_LETTER_CURDIR;

typedef struct _RTL_USER_PROCESS_PARAMETERS {
    ULONG                   MaximumLength;
    ULONG                   Length;
    ULONG                   Flags;
    ULONG                   DebugFlags;
    PVOID                   ConsoleHandle;
    ULONG                   ConsoleFlags;
    HANDLE                  StdInputHandle;
    HANDLE                  StdOutputHandle;
    HANDLE                  StdErrorHandle;
    UNICODE_STRING          CurrentDirectoryPath;
    HANDLE                  CurrentDirectoryHandle;
    UNICODE_STRING          DllPath;
    UNICODE_STRING          ImagePathName;
    UNICODE_STRING          CommandLine;
    PVOID                   Environment;
    ULONG                   StartingPositionLeft;
    ULONG                   StartingPositionTop;
    ULONG                   Width;
    ULONG                   Height;
    ULONG                   CharWidth;
    ULONG                   CharHeight;
    ULONG                   ConsoleTextAttributes;
    ULONG                   WindowFlags;
    ULONG                   ShowWindowFlags;
    UNICODE_STRING          WindowTitle;
    UNICODE_STRING          DesktopName;
    UNICODE_STRING          ShellInfo;
    UNICODE_STRING          RuntimeData;
    RTL_DRIVE_LETTER_CURDIR DLCurrentDirectory[0x20];
} RTL_USER_PROCESS_PARAMETERS, *PRTL_USER_PROCESS_PARAMETERS;

typedef struct _PEB_LDR_DATA * PPEB_LDR_DATA;
typedef struct _PEBLOCKROUTINE * PPEBLOCKROUTINE;
typedef struct _PEB_FREE_BLOCK * PPEB_FREE_BLOCK;
typedef PVOID * PPVOID;

typedef struct _PROCESS_ENVIRONMENT_BLOCK {
    BOOLEAN                 InheritedAddressSpace;
    BOOLEAN                 ReadImageFileExecOptions;
    BOOLEAN                 BeingDebugged;
    BOOLEAN                 Spare;
    HANDLE                  Mutant;
    PVOID                   ImageBaseAddress;
    PPEB_LDR_DATA           LoaderData;
    PRTL_USER_PROCESS_PARAMETERS ProcessParameters;
    PVOID                   SubSystemData;
    PVOID                   ProcessHeap;
    PVOID                   FastPebLock;
    PPEBLOCKROUTINE         FastPebLockRoutine;
    PPEBLOCKROUTINE         FastPebUnlockRoutine;
    ULONG                   EnvironmentUpdateCount;
    PPVOID                  KernelCallbackTable;
    PVOID                   EventLogSection;
    PVOID                   EventLog;
    PPEB_FREE_BLOCK         FreeList;
    ULONG                   TlsExpansionCounter;
    PVOID                   TlsBitmap;
    ULONG                   TlsBitmapBits[0x2];
    PVOID                   ReadOnlySharedMemoryBase;
    PVOID                   ReadOnlySharedMemoryHeap;
    PPVOID                  ReadOnlyStaticServerData;
    PVOID                   AnsiCodePageData;
    PVOID                   OemCodePageData;
    PVOID                   UnicodeCaseTableData;
    ULONG                   NumberOfProcessors;
    ULONG                   NtGlobalFlag;
    BYTE                    Spare2[0x4];
    LARGE_INTEGER           CriticalSectionTimeout;
    ULONG                   HeapSegmentReserve;
    ULONG                   HeapSegmentCommit;
    ULONG                   HeapDeCommitTotalFreeThreshold;
    ULONG                   HeapDeCommitFreeBlockThreshold;
    ULONG                   NumberOfHeaps;
    ULONG                   MaximumNumberOfHeaps;
    PPVOID                  *ProcessHeaps;
    PVOID                   GdiSharedHandleTable;
    PVOID                   ProcessStarterHelper;
    PVOID                   GdiDCAttributeList;
    PVOID                   LoaderLock;
    ULONG                   OSMajorVersion;
    ULONG                   OSMinorVersion;
    ULONG                   OSBuildNumber;
    ULONG                   OSPlatformId;
    ULONG                   ImageSubSystem;
    ULONG                   ImageSubSystemMajorVersion;
    ULONG                   ImageSubSystemMinorVersion;
    ULONG                   GdiHandleBuffer[0x22];
    ULONG                   PostProcessInitRoutine;
    ULONG                   TlsExpansionBitmap;
    BYTE                    TlsExpansionBitmapBits[0x80];
    ULONG                   SessionId;
} loc_PEB, *loc_PPEB;

typedef struct loc_PROCESS_BASIC_INFORMATION {
    LONG                    ExitStatus;
    loc_PPEB                PebBaseAddress;
    ULONG_PTR               AffinityMask;
    LONG                    BasePriority;
    ULONG_PTR               UniqueProcessId;
    ULONG_PTR               InheritedFromUniqueProcessId;
} PBI;

static PBI pbi;
static loc_PEB peb;
static RTL_USER_PROCESS_PARAMETERS upa;

static int get_process_info(HANDLE prs) {
    static LONG (NTAPI * QueryInformationProcessProc)(HANDLE, PROCESSINFOCLASS, PVOID, ULONG, PULONG) = NULL;
    SIZE_T len = 0;

    memset(&pbi, 0, sizeof(pbi));
    memset(&peb, 0, sizeof(peb));
    memset(&upa, 0, sizeof(upa));

    if (QueryInformationProcessProc == NULL) {
        *(FARPROC *)&QueryInformationProcessProc = GetProcAddress(GetModuleHandle("NTDLL.DLL"), "ZwQueryInformationProcess");
        if (QueryInformationProcessProc == NULL) {
            set_win32_errno(GetLastError());
            return -1;
        }
    }
    if (QueryInformationProcessProc(prs, ProcessBasicInformation, &pbi, sizeof(pbi), &len) < 0) {
        set_win32_errno(GetLastError());
        return -1;
    }

    if (pbi.PebBaseAddress != NULL) {
        if (ReadProcessMemory(prs, (LPCVOID)pbi.PebBaseAddress, &peb, sizeof(peb), &len) == 0) {
            set_win32_errno(GetLastError());
            return -1;
        }

        if (peb.ProcessParameters != NULL) {
            if (ReadProcessMemory(prs, (LPCVOID)peb.ProcessParameters, &upa, sizeof(upa), &len) == 0) {
                set_win32_errno(GetLastError());
                return -1;
            }
        }
    }

    return 0;
}

static int write_unicode_string(OutputStream * out, HANDLE prs, UNICODE_STRING str, char * name) {
    if (str.Buffer != NULL) {
        wchar_t w_fnm[FILE_PATH_SIZE];
        SIZE_T buff_size = str.Length;
        SIZE_T read_size = 0;
        memset(w_fnm, 0, sizeof(w_fnm));
        if (buff_size > sizeof(w_fnm)) buff_size = sizeof(w_fnm);
        if (ReadProcessMemory(prs, (LPCVOID)str.Buffer, w_fnm, buff_size, &read_size)) {
            char a_fnm[FILE_PATH_SIZE * 4];
            DWORD k = wcslen(w_fnm);
            int n = WideCharToMultiByte(CP_UTF8, 0, w_fnm, k, a_fnm, sizeof(a_fnm), NULL, NULL);
            a_fnm[n] = 0;
            write_stream(out, ',');
            json_write_string(out, name);
            write_stream(out, ':');
            json_write_string(out, a_fnm);
            return 1;
        }
    }
    return 0;
}

static void write_time(OutputStream * out, FILETIME tm, int64_t base, char * name) {
    int64_t n = (((int64_t)tm.dwLowDateTime | ((int64_t)tm.dwHighDateTime << 32)) - base) / 10000;

    write_stream(out, ',');

    json_write_string(out, name);
    write_stream(out, ':');
    if (n < 0) n = 0;
    json_write_int64(out, n);
}

static void write_process_context(OutputStream * out, char * id, pid_t pid, HANDLE prs) {
    write_stream(out, '{');

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, ',');

    json_write_string(out, "PID");
    write_stream(out, ':');
    json_write_ulong(out, pid);

    write_unicode_string(out, prs, upa.ImagePathName, "File");
    write_unicode_string(out, prs, upa.CurrentDirectoryPath, "CWD");

    {
        FILETIME c_time, e_time, k_time, u_time;
        if (GetProcessTimes(prs, &c_time, &e_time, &k_time, &u_time)) {
            static int64_t system_start_time = 0; /* In FILETIME format: 100-nanosecond intervals since January 1, 1601 (UTC). */
            if (system_start_time == 0) {
                HKEY key;
                if (RegOpenKeyExW(HKEY_LOCAL_MACHINE,
                        L"SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Memory Management",
                        0, KEY_READ, &key) == ERROR_SUCCESS) {
                    wchar_t buf[FILE_PATH_SIZE];
                    DWORD size = sizeof(buf);
                    memset(buf, 0, sizeof(buf));
                    if (RegQueryValueExW(key,
                            L"PagingFiles",
                            NULL, NULL, (LPBYTE)buf, &size) == ERROR_SUCCESS) {
                        WIN32_FIND_DATAW data;
                        HANDLE h = INVALID_HANDLE_VALUE;
                        int n = 0;
                        while (n < FILE_PATH_SIZE && buf[n] != 0) n++;
                        while (n > 0 && buf[n - 1] != ' ') n--;
                        while (n > 0 && buf[n - 1] == ' ') n--;
                        while (n > 0 && buf[n - 1] != ' ') n--;
                        while (n > 0 && buf[n - 1] == ' ') n--;
                        buf[n] = 0;
                        h = FindFirstFileW(buf, &data);
                        if (h != INVALID_HANDLE_VALUE) {
                            system_start_time = (int64_t)data.ftLastWriteTime.dwLowDateTime | ((int64_t)data.ftLastWriteTime.dwHighDateTime << 32);
                            FindClose(h);
                        }
                    }
                    RegCloseKey(key);
                }
            }
            if (system_start_time == 0) {
                SYSTEMTIME st;
                FILETIME ft;
                GetSystemTime(&st);
                if (SystemTimeToFileTime(&st, &ft)) {
                    system_start_time = (int64_t)ft.dwLowDateTime | ((int64_t)ft.dwHighDateTime << 32);
                    system_start_time -= (int64_t)GetTickCount() * 10000; /* Note: GetTickCount() is valid only first 49 days */
                }
            }
            if (system_start_time != 0) {
                write_time(out, c_time, system_start_time, "StartTime");
            }
            write_time(out, k_time, 0, "STime");
            write_time(out, u_time, 0, "UTime");
        }
    }

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (parent != 0) {
        write_errno(&c->out, err);
        write_stringz(&c->out, "null");
    }
    else if (pid != 0) {
        HANDLE prs = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
        if (prs == NULL) err = set_win32_errno(GetLastError());
        if (err == 0 && get_process_info(prs) < 0) err = errno;
        write_errno(&c->out, err);
        if (err == 0) {
            write_process_context(&c->out, id, pid, prs);
            write_stream(&c->out, 0);
        }
        else {
            write_stringz(&c->out, "null");
        }
        if (prs != NULL) CloseHandle(prs);
    }
    else {
        write_errno(&c->out, err);
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);

    if (parent != 0) {
        /* Children of a thread: none */
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else if (pid != 0) {
        /* Children of a process: threads */
        /* TODO: enumerate threads */
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
        /* Children of the root: processes */
        static BOOL (WINAPI * EnumProcessesProc)(DWORD *, DWORD, DWORD *) = NULL;
        HANDLE heap = GetProcessHeap();
        DWORD * prs_ids = NULL;
        int prs_cnt = 0;
        if (EnumProcessesProc == NULL) {
            HINSTANCE psapi = LoadLibrary("PSAPI.DLL");
            if (psapi == NULL) {
                err = set_win32_errno(GetLastError());
            }
            else {
                *(FARPROC *)&EnumProcessesProc = GetProcAddress(psapi, "EnumProcesses");
                if (EnumProcessesProc == NULL) err = set_win32_errno(GetLastError());
            }
        }
        if (err == 0) {
            DWORD size_allocated = 128;
            DWORD size_returned = 0;
            do {
                size_allocated *= 2;
                if (prs_ids != NULL) HeapFree(heap, 0, prs_ids);
                prs_ids = (DWORD *)HeapAlloc(heap, 0, size_allocated);
                if (prs_ids == NULL) {
                    err = set_win32_errno(GetLastError());
                    break;
                }
                if (!EnumProcessesProc(prs_ids, size_allocated, &size_returned)) {
                    err = set_win32_errno(GetLastError());
                    break;
                }
            }
            while (size_returned == size_allocated);
            prs_cnt = size_returned / sizeof(DWORD);
        }
        write_errno(&c->out, err);
        if (err == 0) {
            int pos = 0;
            int cnt = 0;
            write_stream(&c->out, '[');
            for (pos = 0; pos < prs_cnt; pos++) {
                if (prs_ids[pos] == 0) continue;
                if (cnt > 0) write_stream(&c->out, ',');
                json_write_string(&c->out, pid2id(prs_ids[pos], 0));
                cnt++;
            }
            write_stream(&c->out, ']');
            write_stream(&c->out, 0);
        }
        else {
            write_stringz(&c->out, "null");
        }
        if (prs_ids != NULL) HeapFree(heap, 0, prs_ids);
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_command_line(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    HANDLE prs = NULL;
    wchar_t * cmd = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        prs = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
        if (prs == NULL) err = set_win32_errno(GetLastError());
    }
    else {
        err = ERR_INV_CONTEXT;
    }
    if (err == 0 && get_process_info(prs) < 0) err = errno;
    if (err == 0 && upa.CommandLine.Buffer != NULL) {
        SIZE_T cmd_size = upa.CommandLine.Length;
        SIZE_T read_size = 0;
        cmd = (wchar_t *)loc_alloc(cmd_size);
        if (ReadProcessMemory(prs, (LPCVOID)upa.CommandLine.Buffer, cmd, cmd_size, &read_size) == 0) {
            err = set_win32_errno(GetLastError());
        }
    }
    if (prs != NULL) CloseHandle(prs);

    write_errno(&c->out, err);

    if (err == 0 && cmd != NULL) {
        wchar_t * p = cmd;
        wchar_t * e = cmd + upa.CommandLine.Length / sizeof(wchar_t);
        int cnt = 0;
        write_stream(&c->out, '[');
        while (p < e && *p) {
            int quotation = 0;
            if (*p == ' ') { p++; continue; }
            if (*p == '\t') { p++; continue; }
            if (cnt > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '"');
            while (p < e && *p) {
                char buf[0x100];
                unsigned k = 0;
                while (p < e && *p && k < sizeof(buf) / 4) {
                    if (*p == '"' || *p == '\\' || *p == ' ' || *p == '\t') break;
                    p++;
                    k++;
                }
                if (k > 0) {
                    int i = 0;
                    int n = WideCharToMultiByte(CP_UTF8, 0, p - k, k, buf, sizeof(buf), NULL, NULL);
                    while (i < n) json_write_char(&c->out, buf[i++]);
                    if (p == e || *p == 0) break;
                }
                if (*p == '"') {
                    quotation = !quotation;
                    p++;
                }
                else if (*p == '\\') {
                    p++;
                    if (p == e) {
                        json_write_char(&c->out, '\\');
                    }
                    else if (*p == '"') {
                        json_write_char(&c->out, '"');
                        p++;
                    }
                    else if (*p == '\\') {
                        json_write_char(&c->out, '\\');
                        p++;
                    }
                    else {
                        json_write_char(&c->out, '\\');
                    }
                }
                else if (*p == ' ' || *p == '\t') {
                    p++;
                    if (!quotation) break;
                    json_write_char(&c->out, ' ');
                }
                else {
                    assert(k > 0);
                }
            }
            write_stream(&c->out, '"');
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(cmd);
}

static void command_get_environment(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    HANDLE prs = NULL;
    wchar_t * env = NULL;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        prs = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
        if (prs == NULL) err = set_win32_errno(GetLastError());
    }
    else {
        err = ERR_INV_CONTEXT;
    }
    if (err == 0 && get_process_info(prs) < 0) err = errno;
    if (err == 0 && upa.Environment != NULL) {
        wchar_t buf[0x100];
        SIZE_T buf_pos = 0;
        SIZE_T buf_len = 0;
        SIZE_T env_size = 0;
        int cnt = 0;

        for (;;) {
            if (buf_pos >= buf_len) {
                SIZE_T len = 0;
                if (ReadProcessMemory(prs, (LPCVOID)((SIZE_T)upa.Environment + env_size), buf, sizeof(buf), &len) == 0) {
                    err = set_win32_errno(GetLastError());
                    break;
                }
                buf_pos = 0;
                buf_len = len / sizeof(wchar_t);
            }
            env_size += sizeof(wchar_t);
            if (buf[buf_pos++] == 0) {
                cnt++;
                if (cnt == 2) break;
            }
            else {
                cnt = 0;
            }
        }

        if (err == 0) {
            env = (wchar_t *)loc_alloc(env_size);
            if (ReadProcessMemory(prs, (LPCVOID)upa.Environment, env, env_size, &buf_len) == 0) {
                err = set_win32_errno(GetLastError());
            }
        }
    }
    if (prs != NULL) CloseHandle(prs);

    write_errno(&c->out, err);

    if (err == 0 && env != NULL) {
        wchar_t * p = env;
        int cnt = 0;
        write_stream(&c->out, '[');
        while (*p) {
            if (cnt > 0) write_stream(&c->out, ',');
            write_stream(&c->out, '"');
            while (*p) {
                char buf[0x100];
                unsigned k = 0;
                int n = 0, i = 0;
                while (*p && k < sizeof(buf) / 4) { p++; k++; }
                n = WideCharToMultiByte(CP_UTF8, 0, p - k, k, buf, sizeof(buf), NULL, NULL);
                while (i < n) json_write_char(&c->out, buf[i++]);
            }
            p++;
            write_stream(&c->out, '"');
            cnt++;
        }
        write_stream(&c->out, ']');
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
    loc_free(env);
}

#else

#include <unistd.h>
#include <dirent.h>
#include <pwd.h>
#include <grp.h>
#include <linux/param.h>

#define BUF_EOF (-1)

static char buf[1024];
static int buf_fd = -1;
static int buf_pos = 0;
static int buf_len = 0;
static int buf_ch = 0;

static void next_ch(void) {
    while (buf_len >= 0 && buf_pos >= buf_len) {
        buf_pos = 0;
        buf_len = read(buf_fd, buf, sizeof(buf));
        if (buf_len == 0) buf_len = -1;
    }
    if (buf_len < 0) {
        buf_ch = BUF_EOF;
    }
    else {
        buf_ch = buf[buf_pos++];
    }
}

static void first_ch(int fd) {
    buf_fd = fd;
    buf_pos = 0;
    buf_len = 0;
    next_ch();
}

static void write_string_array(OutputStream * out, int f) {
    int cnt = 0;
    first_ch(f);
    write_stream(out, '[');
    while (buf_ch != BUF_EOF && buf_ch != 0) {
        if (cnt > 0) write_stream(out, ',');
        write_stream(out, '"');
        do {
            json_write_char(out, buf_ch);
            next_ch();
        }
        while (buf_ch != BUF_EOF && buf_ch != 0);
        next_ch();
        write_stream(out, '"');
        cnt++;
    }
    write_stream(out, ']');
}

static void write_context(OutputStream * out, char * id, char * parent_id, char * dir) {
    char fnm[FILE_PATH_SIZE + 1];
    int sz;
    int f;

    write_stream(out, '{');

    if (chdir(dir) >= 0) {
        if ((sz = readlink("cwd", fnm, FILE_PATH_SIZE)) > 0) {
            fnm[sz] = 0;
            json_write_string(out, "CWD");
            write_stream(out, ':');
            json_write_string(out, fnm);
            write_stream(out, ',');
        }

        if ((sz = readlink("root", fnm, FILE_PATH_SIZE)) > 0) {
            fnm[sz] = 0;
            json_write_string(out, "Root");
            write_stream(out, ':');
            json_write_string(out, fnm);
            write_stream(out, ',');
        }

        f = open("stat", O_RDONLY);
        if (f >= 0) {
            struct stat st;
            if (fstat(f, &st) == 0) {
                struct passwd * pwd;
                struct group * grp;

                json_write_string(out, "UID");
                write_stream(out, ':');
                json_write_long(out, st.st_uid);
                write_stream(out, ',');

                json_write_string(out, "UGID");
                write_stream(out, ':');
                json_write_long(out, st.st_gid);
                write_stream(out, ',');

                pwd = getpwuid(st.st_uid);
                if (pwd != NULL) {
                    json_write_string(out, "UserName");
                    write_stream(out, ':');
                    json_write_string(out, pwd->pw_name);
                    write_stream(out, ',');
                }

                grp = getgrgid(st.st_gid);
                if (grp != NULL) {
                    json_write_string(out, "GroupName");
                    write_stream(out, ':');
                    json_write_string(out, grp->gr_name);
                    write_stream(out, ',');
                }
            }

            memset(buf, 0, sizeof(buf));
            if ((sz = read(f, buf, sizeof(buf))) > 0) {
                char * str = buf;
                int pid = 0;                /* The process ID. */
                char * comm = fnm;          /* The  filename  of  the  executable,  in parentheses.  This is visible */
                                            /* whether or not the executable is swapped out. */
                char state = 0;             /* One character from the string "RSDZTW"  where  R  is  running,  S  is */
                                            /* sleeping  in  an  interruptible wait, D is waiting in uninterruptible */
                                            /* disk sleep, Z is zombie, T is traced or stopped (on a signal), and  W */
                                            /* is paging. */
                int ppid = 0;               /* The PID of the parent. */
                int pgrp = 0;               /* The process group ID of the process. */
                int session = 0;            /* The session ID of the process. */
                int tty_nr = 0;             /* The tty the process uses. */
                int tpgid = 0;              /* The process group ID of the process which currently owns the tty that */
                                            /* the process is connected to. */
                unsigned long flags = 0;    /* The kernel flags word of the process. For bit meanings, see the  PF_* */
                                            /* defines in <linux/sched.h>.  Details depend on the kernel version. */
                unsigned long minflt = 0;   /* The  number  of  minor  faults  the  process  has made which have not */
                                            /* required loading a memory page from disk. */
                unsigned long cminflt = 0;  /* The number of minor faults that  the  process's  waited-for  children */
                                            /* have made. */
                unsigned long majflt = 0;   /* The  number  of major faults the process has made which have required */
                                            /* loading a memory page from disk. */
                unsigned long cmajflt = 0;  /* The number of major faults that  the  process's  waited-for  children */
                                            /* have made. */
                unsigned long utime = 0;    /* The  number  of  jiffies that this process has been scheduled in user */
                                            /* mode. */
                unsigned long stime = 0;    /* The number of jiffies that this process has been scheduled in  kernel */
                                            /* mode. */
                long cutime = 0;            /* The  number  of  jiffies that this process's waited-for children have */
                                            /* been scheduled in user mode. (See also times(2).) */
                long cstime = 0;            /* The number of jiffies that this process's  waited-for  children  have */
                                            /* been scheduled in kernel mode. */
                long priority = 0;          /* The  standard  nice value, plus fifteen.  The value is never negative */
                                            /* in the kernel. */
                long nice = 0;              /* The nice value ranges from 19 (nicest) to -19 (not nice to others). */
                long dummy = 0;             /* This value is hard coded to 0 as a placeholder for a removed field. */
                long itrealvalue = 0;       /* The time in jiffies before the next SIGALRM is sent  to  the  process */
                                            /* due to an interval timer. */
                unsigned long starttime = 0;/* The time in jiffies the process started after system boot. */
                unsigned long vsize = 0;    /* Virtual memory size in bytes. */
                long rss = 0;               /* Resident  Set  Size:  number of pages the process has in real memory, */
                                            /* minus 3 for administrative purposes. This is  just  the  pages  which */
                                            /* count  towards  text,  data,  or  stack space.  This does not include */
                                            /* pages which have not been demand-loaded in, or which are swapped out. */
                unsigned long rlim = 0;     /* Current  limit in bytes on the rss of the process (usually 4294967295 */
                                            /* on i386). */
                unsigned long startcode = 0;/* The address above which program text can run. */
                unsigned long endcode = 0;  /* The address below which program text can run. */
                unsigned long startstack =0;/* The address of the start of the stack. */
                unsigned long kstkesp = 0;  /* The current value of esp (stack pointer),  as  found  in  the  kernel */
                                            /* stack page for the process. */
                unsigned long kstkeip = 0;  /* The current EIP (instruction pointer). */
                unsigned long signal = 0;   /* The bitmap of pending signals. */
                unsigned long blocked = 0;  /* The bitmap of blocked signals. */
                unsigned long sigignore = 0;/* The bitmap of ignored signals. */
                unsigned long sigcatch = 0; /* The bitmap of caught signals. */
                unsigned long wchan = 0;    /* This  is  the  "channel"  in which the process is waiting.  It is the */
                                            /* address of a system call, and can be looked up in a namelist  if  you */
                                            /* need  a  textual  name.   (If you have an up-to-date /etc/psdatabase, */
                                            /* then try ps -l to see the WCHAN field in action.) */
                unsigned long nswap = 0;    /* Number of pages swapped (not maintained). */
                unsigned long cnswap = 0;   /* Cumulative nswap for child processes (not maintained). */
                int exit_signal = 0;        /* Signal to be sent to parent when we die. */
                int processor = 0;          /* CPU number last executed on. */
                unsigned long rt_priority=0;/* Real-time scheduling priority (see sched_setscheduler(2)). */
                unsigned long policy = 0;   /* Scheduling policy (see sched_setscheduler(2)). */

                assert(sz < (int)sizeof(buf));
                buf[sz] = 0;

                pid = (int)strtol(str, &str, 10);
                while (*str == ' ') str++;
                if (*str == '(') str++;
                sz = 0;
                while (*str && *str != ')') comm[sz++] = *str++;
                comm[sz] = 0;
                if (*str == ')') str++;
                while (*str == ' ') str++;

                sscanf(str,
                    "%c %d %d %d %d %d %lu %lu %lu %lu %lu %lu %lu %ld %ld %ld %ld %ld %ld %lu %lu %ld %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu %d %d %lu %lu",
                    &state, &ppid, &pgrp, &session, &tty_nr, &tpgid, &flags,
                    &minflt, &cminflt, &majflt, &cmajflt, &utime, &stime, &cutime, &cstime,
                    &priority, &nice, &dummy, &itrealvalue, &starttime, &vsize, &rss, &rlim,
                    &startcode, &endcode, &startstack, &kstkesp, &kstkeip, &signal, &blocked, &sigignore, &sigcatch,
                    &wchan, &nswap, &cnswap, &exit_signal, &processor, &rt_priority, &policy);

                json_write_string(out, "PID");
                write_stream(out, ':');
                json_write_long(out, pid);
                write_stream(out, ',');

                json_write_string(out, "File");
                write_stream(out, ':');
                json_write_string(out, comm);
                write_stream(out, ',');

                json_write_string(out, "State");
                write_stream(out, ':');
                write_stream(out, '"');
                json_write_char(out, state);
                write_stream(out, '"');
                write_stream(out, ',');

                if (ppid > 0) {
                    json_write_string(out, "PPID");
                    write_stream(out, ':');
                    json_write_long(out, ppid);
                    write_stream(out, ',');
                }

                json_write_string(out, "PGRP");
                write_stream(out, ':');
                json_write_long(out, pgrp);
                write_stream(out, ',');

                json_write_string(out, "Session");
                write_stream(out, ':');
                json_write_long(out, session);
                write_stream(out, ',');

                if (tty_nr > 0) {
                    json_write_string(out, "TTY");
                    write_stream(out, ':');
                    json_write_long(out, tty_nr);
                    write_stream(out, ',');
                }

                if (tpgid > 0) {
                    json_write_string(out, "TGID");
                    write_stream(out, ':');
                    json_write_long(out, tpgid);
                    write_stream(out, ',');
                }

                json_write_string(out, "Flags");
                write_stream(out, ':');
                json_write_ulong(out, flags);
                write_stream(out, ',');

                json_write_string(out, "MinFlt");
                write_stream(out, ':');
                json_write_ulong(out, minflt);
                write_stream(out, ',');

                json_write_string(out, "CMinFlt");
                write_stream(out, ':');
                json_write_ulong(out, cminflt);
                write_stream(out, ',');

                json_write_string(out, "MajFlt");
                write_stream(out, ':');
                json_write_ulong(out, majflt);
                write_stream(out, ',');

                json_write_string(out, "CMajFlt");
                write_stream(out, ':');
                json_write_ulong(out, cmajflt);
                write_stream(out, ',');

                json_write_string(out, "UTime");
                write_stream(out, ':');
                json_write_uint64(out, (uint64_t)utime * 1000 / HZ);
                write_stream(out, ',');

                json_write_string(out, "STime");
                write_stream(out, ':');
                json_write_uint64(out, (uint64_t)stime * 1000 / HZ);
                write_stream(out, ',');

                json_write_string(out, "CUTime");
                write_stream(out, ':');
                json_write_uint64(out, (uint64_t)cutime * 1000 / HZ);
                write_stream(out, ',');

                json_write_string(out, "CSTime");
                write_stream(out, ':');
                json_write_uint64(out, (uint64_t)cstime * 1000 / HZ);
                write_stream(out, ',');

                json_write_string(out, "Priority");
                write_stream(out, ':');
                json_write_long(out, (long)priority - 15);
                write_stream(out, ',');

                if (nice != 0) {
                    json_write_string(out, "Nice");
                    write_stream(out, ':');
                    json_write_long(out, nice);
                    write_stream(out, ',');
                }

                if (itrealvalue != 0) {
                    json_write_string(out, "ITRealValue");
                    write_stream(out, ':');
                    json_write_int64(out, (int64_t)itrealvalue * 1000 / HZ);
                    write_stream(out, ',');
                }

                json_write_string(out, "StartTime");
                write_stream(out, ':');
                json_write_uint64(out, (uint64_t)starttime * 1000 / HZ);
                write_stream(out, ',');

                json_write_string(out, "VSize");
                write_stream(out, ':');
                json_write_ulong(out, vsize);
                write_stream(out, ',');

                json_write_string(out, "PSize");
                write_stream(out, ':');
                json_write_ulong(out, getpagesize());
                write_stream(out, ',');

                json_write_string(out, "RSS");
                write_stream(out, ':');
                json_write_long(out, rss);
                write_stream(out, ',');

                json_write_string(out, "RLimit");
                write_stream(out, ':');
                json_write_ulong(out, rlim);
                write_stream(out, ',');

                if (startcode != 0) {
                    json_write_string(out, "CodeStart");
                    write_stream(out, ':');
                    json_write_ulong(out, startcode);
                    write_stream(out, ',');
                }

                if (endcode != 0) {
                    json_write_string(out, "CodeEnd");
                    write_stream(out, ':');
                    json_write_ulong(out, endcode);
                    write_stream(out, ',');
                }

                if (startstack != 0) {
                    json_write_string(out, "StackStart");
                    write_stream(out, ':');
                    json_write_ulong(out, startstack);
                    write_stream(out, ',');
                }

                json_write_string(out, "Signals");
                write_stream(out, ':');
                json_write_ulong(out, signal);
                write_stream(out, ',');

                json_write_string(out, "SigBlock");
                write_stream(out, ':');
                json_write_ulong(out, blocked);
                write_stream(out, ',');

                json_write_string(out, "SigIgnore");
                write_stream(out, ':');
                json_write_ulong(out, sigignore);
                write_stream(out, ',');

                json_write_string(out, "SigCatch");
                write_stream(out, ':');
                json_write_ulong(out, sigcatch);
                write_stream(out, ',');

                if (wchan != 0) {
                    json_write_string(out, "WChan");
                    write_stream(out, ':');
                    json_write_ulong(out, wchan);
                    write_stream(out, ',');
                }

                json_write_string(out, "NSwap");
                write_stream(out, ':');
                json_write_ulong(out, nswap);
                write_stream(out, ',');

                json_write_string(out, "CNSwap");
                write_stream(out, ':');
                json_write_ulong(out, cnswap);
                write_stream(out, ',');

                json_write_string(out, "ExitSignal");
                write_stream(out, ':');
                json_write_long(out, exit_signal);
                write_stream(out, ',');

                json_write_string(out, "Processor");
                write_stream(out, ':');
                json_write_long(out, processor);
                write_stream(out, ',');

                json_write_string(out, "RTPriority");
                write_stream(out, ':');
                json_write_ulong(out, rt_priority);
                write_stream(out, ',');

                json_write_string(out, "Policy");
                write_stream(out, ':');
                json_write_ulong(out, policy);
                write_stream(out, ',');
            }
            close(f);
        }
    }

    if (parent_id != NULL && parent_id[0] != 0) {
        json_write_string(out, "ParentID");
        write_stream(out, ':');
        json_write_string(out, parent_id);
        write_stream(out, ',');
    }

    json_write_string(out, "ID");
    write_stream(out, ':');
    json_write_string(out, id);

    write_stream(out, '}');
}

static void command_get_context(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[FILE_PATH_SIZE];

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0) {
        struct stat st;
        if (parent != 0) {
            snprintf(dir, sizeof(dir), "/proc/%d/task/%d", parent, pid);
        }
        else {
            snprintf(dir, sizeof(dir), "/proc/%d", pid);
        }
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }

    write_errno(&c->out, err);

    if (err == 0 && pid != 0) {
        char bf[256];
        write_context(&c->out, id, parent == 0 ? NULL : strcpy(bf, pid2id(parent, 0)), dir);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_children(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);

    if (parent != 0) {
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
        DIR * proc = NULL;
        char dir[FILE_PATH_SIZE];
        if (pid == 0) strcpy(dir, "/proc");
        else snprintf(dir, sizeof(dir), "/proc/%d/task", pid);
        proc = opendir(dir);
        if (proc == NULL) {
            int err = errno;
            if (pid != 0 && err == ENOENT) {
                struct stat buf;
                snprintf(dir, sizeof(dir), "/proc/%d", pid);
                if (stat(dir, &buf) == 0) {
                    /* Zombie */
                    err = 0;
                }
            }
            write_errno(&c->out, err);
            write_stringz(&c->out, "null");
        }
        else {
            int cnt = 0;
            write_errno(&c->out, 0);
            write_stream(&c->out, '[');
            for (;;) {
                struct dirent * ent = readdir(proc);
                if (ent == NULL) break;
                if (ent->d_name[0] >= '1' && ent->d_name[0] <= '9') {
                    if (cnt > 0) write_stream(&c->out, ',');
                    json_write_string(&c->out, pid2id(atol(ent->d_name), pid));
                    cnt++;
                }
            }
            write_stream(&c->out, ']');
            write_stream(&c->out, 0);
            closedir(proc);
        }
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_command_line(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[FILE_PATH_SIZE];
    int f = -1;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        struct stat st;
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    if (err == 0 && chdir(dir) < 0) err = errno;
    if (err == 0 && (f = open("cmdline", O_RDONLY)) < 0) err = errno;

    write_errno(&c->out, err);

    if (err == 0) {
        write_string_array(&c->out, f);
        close(f);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}

static void command_get_environment(char * token, Channel * c) {
    char id[256];
    pid_t pid = 0;
    pid_t parent = 0;
    int err = 0;
    char dir[FILE_PATH_SIZE];
    int f = -1;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    pid = id2pid(id, &parent);
    if (pid != 0 && parent == 0) {
        struct stat st;
        snprintf(dir, sizeof(dir), "/proc/%d", pid);
        if (lstat(dir, &st) < 0) err = errno;
        else if (!S_ISDIR(st.st_mode)) err = ERR_INV_CONTEXT;
    }
    else {
        err = ERR_INV_CONTEXT;
    }

    if (err == 0 && chdir(dir) < 0) err = errno;
    if (err == 0 && (f = open("environ", O_RDONLY)) < 0) err = errno;

    write_errno(&c->out, err);

    if (err == 0) {
        write_string_array(&c->out, f);
        close(f);
        write_stream(&c->out, 0);
    }
    else {
        write_stringz(&c->out, "null");
    }

    write_stream(&c->out, MARKER_EOM);
}
#endif

extern void ini_sys_mon_service(Protocol * proto) {
    add_command_handler(proto, SYS_MON, "getContext", command_get_context);
    add_command_handler(proto, SYS_MON, "getChildren", command_get_children);
    add_command_handler(proto, SYS_MON, "getCommandLine", command_get_command_line);
    add_command_handler(proto, SYS_MON, "getEnvironment", command_get_environment);
}

#endif /* SERVICE_SysMonitor */


