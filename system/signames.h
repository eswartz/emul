/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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
 * POSIX signal names
 */

#define CASE(var) case var: return ""#var;
char * signal_name(int signal) {
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

char * signal_description(int signal) {
    /* TODO: signal description */
    return NULL;
}

unsigned signal_code(int signal) {
    return signal;
}
