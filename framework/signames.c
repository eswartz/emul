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

/*
 * This module provides POSIX signal names and descriptions.
 */

#include <config.h>

#include <signal.h>
#include <framework/signames.h>

#if defined(WIN32)

typedef struct ExceptionName {
    DWORD code;
    char * name;
    char * desc;
} ExceptionName;

static ExceptionName exception_names[] = {
    { 0x40010005, NULL, "Control-C" },
    { 0x40010008, NULL, "Control-Break" },
    { EXCEPTION_DATATYPE_MISALIGNMENT, "EXCEPTION_DATATYPE_MISALIGNMENT", "Datatype Misalignment" },
    { EXCEPTION_ACCESS_VIOLATION, "EXCEPTION_ACCESS_VIOLATION", "Access Violation" },
    { EXCEPTION_IN_PAGE_ERROR, "EXCEPTION_IN_PAGE_ERROR", "In Page Error" },
    { EXCEPTION_ILLEGAL_INSTRUCTION, "EXCEPTION_ILLEGAL_INSTRUCTION", "Illegal Instruction" },
    { EXCEPTION_ARRAY_BOUNDS_EXCEEDED, "EXCEPTION_ARRAY_BOUNDS_EXCEEDED", "Array Bounds Exceeded" },
    { EXCEPTION_FLT_DENORMAL_OPERAND, "EXCEPTION_FLT_DENORMAL_OPERAND", "Float Denormal Operand" },
    { EXCEPTION_FLT_DIVIDE_BY_ZERO, "EXCEPTION_FLT_DIVIDE_BY_ZERO", "Float Divide by Zero" },
    { EXCEPTION_FLT_INEXACT_RESULT, "EXCEPTION_FLT_INEXACT_RESULT", "Float Inexact Result" },
    { EXCEPTION_FLT_INVALID_OPERATION, "EXCEPTION_FLT_INVALID_OPERATION", "Float Invalid Operation" },
    { EXCEPTION_FLT_OVERFLOW, "EXCEPTION_FLT_OVERFLOW", "Float Overflow" },
    { EXCEPTION_FLT_STACK_CHECK, "EXCEPTION_FLT_STACK_CHECK", "Float Stack Check" },
    { EXCEPTION_FLT_UNDERFLOW, "EXCEPTION_FLT_UNDERFLOW", "Float Underflow" },
    { EXCEPTION_NONCONTINUABLE_EXCEPTION, "EXCEPTION_NONCONTINUABLE_EXCEPTION", "Noncontinuable Exception" },
    { EXCEPTION_INVALID_DISPOSITION, "EXCEPTION_INVALID_DISPOSITION", "Invalid Disposition" },
    { EXCEPTION_INT_DIVIDE_BY_ZERO, "EXCEPTION_INT_DIVIDE_BY_ZERO", "Integer Divide by Zero" },
    { EXCEPTION_INT_OVERFLOW, "EXCEPTION_INT_OVERFLOW", "Integer Overflow" },
    { EXCEPTION_PRIV_INSTRUCTION, "EXCEPTION_PRIV_INSTRUCTION", "Privileged Instruction" },
    { EXCEPTION_STACK_OVERFLOW, "EXCEPTION_STACK_OVERFLOW", "Stack Overflow" },
    { EXCEPTION_GUARD_PAGE, "EXCEPTION_GUARD_PAGE", "Guard Page" },
    { 0xC0000194, "EXCEPTION_POSSIBLE_DEADLOCK", "Possible Deadlock" },
    { EXCEPTION_INVALID_HANDLE, "EXCEPTION_INVALID_HANDLE", "Invalid Handle" },
    { 0xc0000017, NULL, "No Memory" },
    { 0xc0000135, NULL, "DLL Not Found" },
    { 0xc0000142, NULL, "DLL Initialization Failed" },
    { 0xc06d007e, NULL, "Module Not Found" },
    { 0xc06d007f, NULL, "Procedure Not Found" },
    { 0xe06d7363, NULL, "Microsoft C++ Exception" },
};

#define EXCEPTION_NAMES_CNT ((int)(sizeof(exception_names) / sizeof(ExceptionName)))

const char * signal_name(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].name;
    return NULL;
}

const char * signal_description(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].desc;
    return NULL;
}

unsigned signal_code(int signal) {
    int n = signal - 1;
    if (n >= 0 && n < EXCEPTION_NAMES_CNT) return exception_names[n].code;
    return 0;
}

int get_signal_from_code(unsigned code) {
    int n = 0;
    while (n < EXCEPTION_NAMES_CNT) {
        if (exception_names[n].code == code) return n + 1;
        n++;
    }
    return 0;
}

#else

/*
 * POSIX signal names
 */

#define CASE(var) case var: return ""#var;
const char * signal_name(int signal) {
    switch (signal) {
    CASE(SIGHUP)
    CASE(SIGINT)
    CASE(SIGQUIT)
    CASE(SIGILL)
    CASE(SIGTRAP)
    CASE(SIGABRT)
    CASE(SIGBUS)
    CASE(SIGFPE)
    CASE(SIGKILL)
    CASE(SIGUSR1)
    CASE(SIGSEGV)
    CASE(SIGUSR2)
    CASE(SIGPIPE)
    CASE(SIGALRM)
    CASE(SIGTERM)
#ifdef SIGSTKFLT
    CASE(SIGSTKFLT)
#endif
    CASE(SIGCHLD)
    CASE(SIGCONT)
    CASE(SIGSTOP)
    CASE(SIGTSTP)
    CASE(SIGTTIN)
    CASE(SIGTTOU)
    CASE(SIGURG)
    CASE(SIGXCPU)
    CASE(SIGXFSZ)
    CASE(SIGVTALRM)
    CASE(SIGPROF)
#ifdef SIGWINCH
    CASE(SIGWINCH)
#endif
#ifdef SIGIO
    CASE(SIGIO)
#endif
#ifdef SIGPWR
    CASE(SIGPWR)
#endif
    CASE(SIGSYS)
    }
    return NULL;
}
#undef CASE

const char * signal_description(int signal) {
    /* TODO: signal description */
    return NULL;
}

unsigned signal_code(int signal) {
    return signal;
}

int get_signal_from_code(unsigned code) {
    return code;
}

#endif
