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
 * Implements IP interface list.
 */

#include <config.h>
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include <framework/ip_ifc.h>
#include <framework/myalloc.h>
#include <framework/events.h>
#include <framework/errors.h>
#include <framework/trace.h>

#define MAX(x,y) ((x) > (y) ? (x) : (y))

#if !defined(WIN32) && !defined(ifr_netmask)
#  define ifr_netmask ifr_addr
#endif

int build_ifclist(int sock, int max, ip_ifc_info * list) {
#ifdef WIN32
    int i;
    int ind;
    MIB_IPADDRTABLE * info;
    ULONG out_buf_len;
    DWORD ret_val;

    out_buf_len = sizeof *info;
    info = (MIB_IPADDRTABLE *)loc_alloc(out_buf_len);
    ret_val = GetIpAddrTable(info, &out_buf_len, 0);
    if (ret_val == ERROR_INSUFFICIENT_BUFFER) {
        loc_free(info);
        info = (MIB_IPADDRTABLE *)loc_alloc(out_buf_len);
        ret_val = GetIpAddrTable(info, &out_buf_len, 0);
    }
    if (ret_val != NO_ERROR) {
        trace(LOG_ALWAYS, "GetIpAddrTable() error: %d", ret_val);
        loc_free(info);
        return 0;
    }
    ind = 0;
    for (i = 0; i < (int)info->dwNumEntries && ind < max; i++) {
        list[ind].addr = info->table[i].dwAddr;
        if (list[ind].addr == 0) continue;
        list[ind].mask = info->table[i].dwMask;
        ind++;
    }
    loc_free(info);
#elif defined(__SYMBIAN32__)
    int ind = 0;
    ip_ifc_info* info = get_ip_ifc();
    if (info) {
        trace(LOG_ALWAYS,"The IP address is %d.%d.%d.%d",
                (info->addr >> 24) & 0xff,
                (info->addr >> 16) & 0xff,
                (info->addr >> 8) & 0xff,
                info->addr & 0xff
                );
        list[ind++] = *info;
    }
#else
    int ind;
    char * cp;
    struct ifconf ifc;
    char if_bbf[0x2000];

    memset(&ifc, 0, sizeof ifc);
    ifc.ifc_len = sizeof if_bbf;
    ifc.ifc_buf = if_bbf;
    if (ioctl(sock, SIOCGIFCONF, &ifc) < 0) {
        trace(LOG_ALWAYS, "error: ioctl(SIOCGIFCONF) returned %d: %s", errno, errno_to_str(errno));
        return 0;
    }
    ind = 0;
    cp = (char *)ifc.ifc_req;
    while (cp < (char *)ifc.ifc_req + ifc.ifc_len && ind < max) {
        struct ifreq * ifreq_addr = (struct ifreq *)cp;
        struct ifreq ifreq_mask = *ifreq_addr;
        size_t size = sizeof(struct ifreq);
        /* BSD systems allow sockaddrs to be longer than their sizeof */
        if (SA_LEN(&ifreq_addr->ifr_addr) > sizeof(ifreq_addr->ifr_addr))
            size += SA_LEN(&ifreq_addr->ifr_addr) - sizeof(ifreq_addr->ifr_addr);
        cp += size;
        if (ifreq_addr->ifr_addr.sa_family != AF_INET) continue;
        if (ioctl(sock, SIOCGIFNETMASK, &ifreq_mask) < 0) {
            trace(LOG_ALWAYS, "error: ioctl(SIOCGIFNETMASK) returned %d: %s", errno, errno_to_str(errno));
            continue;
        }
        list[ind].addr = ((struct sockaddr_in *)&ifreq_addr->ifr_addr)->sin_addr.s_addr;
        list[ind].mask = ((struct sockaddr_in *)&ifreq_mask.ifr_netmask)->sin_addr.s_addr;
        ind++;
    }
#endif
    return ind;
}
