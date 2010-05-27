/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    public static String SysMonitor_AllProcesses;
    public static String SysMonitor_Process;

    // PROCESS PROPERTIES
    public static String PROCESS_ID_LABEL;
    public static String PROCESS_PID_LABEL;
    public static String PROCESS_NAME_LABEL;
    public static String PROCESS_CWD_LABEL;
    public static String PROCESS_ROOT_LABEL;
    public static String PROCESS_UID_LABEL;
    public static String PROCESS_USERNAME_LABEL;
    public static String PROCESS_GID_LABEL;
    public static String PROCESS_GROUPNAME_LABEL;
    public static String PROCESS_PPID_LABEL;
    public static String PROCESS_PGRP_LABEL;
    public static String PROCESS_STATE_LABEL;
    public static String PROCESS_TRACERPID_LABEL;
    public static String PROCESS_VMSIZE_LABEL;
    public static String PROCESS_VMRSS_LABEL;
    public static String PROCESS_SESSION_LABEL;
    public static String PROCESS_TTY_LABEL;
    public static String PROCESS_TGID_LABEL;
    public static String PROCESS_FLAGS_LABEL;
    public static String PROCESS_MINFLT_LABEL;
    public static String PROCESS_CMINFLT_LABEL;
    public static String PROCESS_MAJFLT_LABEL;
    public static String PROCESS_CMAJFLT_LABEL;
    public static String PROCESS_UTIME_LABEL;
    public static String PROCESS_STIME_LABEL;
    public static String PROCESS_CUTIME_LABEL;
    public static String PROCESS_CSTIME_LABEL;
    public static String PROCESS_PC_UTIME_LABEL;
    public static String PROCESS_PC_STIME_LABEL;
    public static String PROCESS_PRIORITY_LABEL;
    public static String PROCESS_NICE_LABEL;
    public static String PROCESS_ITREALVALUE_LABEL;
    public static String PROCESS_STARTTIME_LABEL;
    public static String PROCESS_RLIMIT_LABEL;
    public static String PROCESS_CODESTART_LABEL;
    public static String PROCESS_CODEEND_LABEL;
    public static String PROCESS_STACKSTART_LABEL;
    public static String PROCESS_SIGNALS_LABEL;
    public static String PROCESS_SIGBLOCK_LABEL;
    public static String PROCESS_SIGIGNORE_LABEL;
    public static String PROCESS_SIGCATCH_LABEL;
    public static String PROCESS_WCHAN_LABEL;
    public static String PROCESS_NSWAP_LABEL;
    public static String PROCESS_CNSWAP_LABEL;
    public static String PROCESS_EXITSIGNAL_LABEL;
    public static String PROCESS_PROCESSOR_LABEL;
    public static String PROCESS_RTPRIORITY_LABEL;
    public static String PROCESS_POLICY_LABEL;

    public static String PROCESS_ID_TOOLTIP;
    public static String PROCESS_PID_TOOLTIP;
    public static String PROCESS_NAME_TOOLTIP;
    public static String PROCESS_CWD_TOOLTIP;
    public static String PROCESS_ROOT_TOOLTIP;
    public static String PROCESS_UID_TOOLTIP;
    public static String PROCESS_USERNAME_TOOLTIP;
    public static String PROCESS_GID_TOOLTIP;
    public static String PROCESS_GROUPNAME_TOOLTIP;
    public static String PROCESS_PPID_TOOLTIP;
    public static String PROCESS_PGRP_TOOLTIP;
    public static String PROCESS_STATE_TOOLTIP;
    public static String PROCESS_TRACERPID_TOOLTIP;
    public static String PROCESS_VMSIZE_TOOLTIP;
    public static String PROCESS_VMRSS_TOOLTIP;
    public static String PROCESS_SESSION_TOOLTIP;
    public static String PROCESS_TTY_TOOLTIP;
    public static String PROCESS_TGID_TOOLTIP;
    public static String PROCESS_FLAGS_TOOLTIP;
    public static String PROCESS_MINFLT_TOOLTIP;
    public static String PROCESS_CMINFLT_TOOLTIP;
    public static String PROCESS_MAJFLT_TOOLTIP;
    public static String PROCESS_CMAJFLT_TOOLTIP;
    public static String PROCESS_UTIME_TOOLTIP;
    public static String PROCESS_STIME_TOOLTIP;
    public static String PROCESS_PC_UTIME_TOOLTIP;
    public static String PROCESS_PC_STIME_TOOLTIP;
    public static String PROCESS_CUTIME_TOOLTIP;
    public static String PROCESS_CSTIME_TOOLTIP;
    public static String PROCESS_PRIORITY_TOOLTIP;
    public static String PROCESS_NICE_TOOLTIP;
    public static String PROCESS_ITREALVALUE_TOOLTIP;
    public static String PROCESS_STARTTIME_TOOLTIP;
    public static String PROCESS_RLIMIT_TOOLTIP;
    public static String PROCESS_CODESTART_TOOLTIP;
    public static String PROCESS_CODEEND_TOOLTIP;
    public static String PROCESS_STACKSTART_TOOLTIP;
    public static String PROCESS_SIGNALS_TOOLTIP;
    public static String PROCESS_SIGBLOCK_TOOLTIP;
    public static String PROCESS_SIGIGNORE_TOOLTIP;
    public static String PROCESS_SIGCATCH_TOOLTIP;
    public static String PROCESS_WCHAN_TOOLTIP;
    public static String PROCESS_NSWAP_TOOLTIP;
    public static String PROCESS_CNSWAP_TOOLTIP;
    public static String PROCESS_EXITSIGNAL_TOOLTIP;
    public static String PROCESS_PROCESSOR_TOOLTIP;
    public static String PROCESS_RTPRIORITY_TOOLTIP;
    public static String PROCESS_POLICY_TOOLTIP;

    public static String PROCESS_VMSIZE_VALUE;
    public static String PROCESS_VMRSS_VALUE;

    static {
        // initialize resource bundle
        NLS.initializeMessages(Messages.class.getName(), Messages.class);
    }

    private Messages() {
    }
}
