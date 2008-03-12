/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Implements simple UDP based auto discovery.
 */

#if defined(_WRS_KERNEL)
#  include <vxWorks.h>
#  include <inetLib.h>
#endif
#include <stddef.h>
#include <errno.h>
#include <assert.h>
#include "mdep.h"
#include "tcf.h"
#include "discovery.h"
#include "discovery_udp.h"
#include "myalloc.h"
#include "events.h"
#include "errors.h"
#include "trace.h"
#include "peer.h"
#include "ip_ifc.h"

#define MAX_PENDING_INFO_REQ    10
#define MAX_PENDING_INFO_ACK    10
#define MAX_IFC                 10

#define REFRESH_TIME            10
#define REFRESH_MIN_TIME        2
#define STALE_TIME_DELTA        (REFRESH_TIME*3)

typedef struct receive_message receive_message;

struct receive_message {
    int addr_len;
    int buf_len;
    struct sockaddr_in addr;
    char buf[PKT_SIZE];
};

static int ifcind;
static ip_ifc_info ifclist[MAX_IFC];
static int refresh_timer_active;
static time_t last_refresh_time;
static int pending_info_req;
static int pending_info_ack;
static int discovery_port;
static int udp_server_socket = -1;
static pthread_t udp_server_thread = 0;
static pthread_mutex_t udp_discovery_mutex;

#define MAX(x,y) ((x) > (y) ? (x) : (y))

static void app_char(char * buf, int * pos, char ch) {
    if (*pos < PKT_SIZE) buf[*pos] = ch;
    (*pos)++;
}

static void app_str(char * buf, int * pos, char * str) {
    while (*str) {
        if (*pos < PKT_SIZE) buf[*pos] = *str;
        (*pos)++;
        str++;
    }
}

static void app_strz(char * buf, int * pos, char * str) {
    app_str(buf, pos, str);
    app_char(buf, pos, 0);
}

static int udp_send_peer_sever(PeerServer * ps, void * arg) {
    struct sockaddr_in * addr = arg;
    char * transport = NULL;
    char * host = NULL;
    char * port = NULL;
    struct in_addr src_addr;
    ip_ifc_info * ifc;

    if ((ps->flags & (PS_FLAG_LOCAL | PS_FLAG_PRIVATE | PS_FLAG_DISCOVERABLE)) != 
        (PS_FLAG_LOCAL | PS_FLAG_DISCOVERABLE)) {
        return 0;
    }
    transport = peer_server_getprop(ps, "TransportName", "");
    if (strcmp(transport, "TCP") != 0 && strcmp(transport, "UDP") != 0) return 0;
    host = peer_server_getprop(ps, "Host", NULL);
#ifdef _WRS_KERNEL
    // VxWorks inet_aton() return codes are opposite to standard
    if (host == NULL || inet_aton(host, &src_addr) != OK) return 0;
#else
    if (host == NULL || inet_aton(host, &src_addr) == 0) return 0;
#endif    
    port = peer_server_getprop(ps, "Port", NULL);
    if (port == NULL) return 0;

    for (ifc = ifclist; ifc < &ifclist[ifcind]; ifc++) {
        int i;
        int pos = 0;
        int seenName = 0;
        int seenOSName = 0;
        struct sockaddr_in * dst_addr;
        struct sockaddr_in dst_addr_buf;
        char buf[PKT_SIZE];
        if (src_addr.s_addr != INADDR_ANY &&
            (ifc->addr & ifc->mask) != (src_addr.s_addr & ifc->mask)) {
            /* Server address not matching this interface */
            continue;
        }
        if (addr != NULL &&
            (ifc->addr & ifc->mask) != (addr->sin_addr.s_addr & ifc->mask)) {
            /* Requesting address not matching this interface */
            continue;
        }
        if (addr == NULL) {
            dst_addr = &dst_addr_buf;
            memset(&dst_addr_buf, 0, sizeof dst_addr_buf);
            dst_addr->sin_family = AF_INET;
            dst_addr->sin_port = htons((short)discovery_port);
            dst_addr->sin_addr.s_addr = ifc->addr | ~ifc->mask;
        }
        else {
            dst_addr = addr;
        }
        trace(LOG_DISCOVERY, "udp_send_peer_sever: sending UDP_ACK_INFO, ID=%s:%s:%s, dst=%s", transport, host, port, inet_ntoa(dst_addr->sin_addr));

        buf[pos++] = 'T';
        buf[pos++] = 'C';
        buf[pos++] = 'F';
        buf[pos++] = '1';
        buf[pos++] = UDP_ACK_INFO;
        buf[pos++] = 0;
        buf[pos++] = 0;
        buf[pos++] = 0;
        app_str(buf, &pos, "ID=");
        app_str(buf, &pos, transport);
        app_str(buf, &pos, ":");
        app_str(buf, &pos, host);
        app_str(buf, &pos, ":");
        app_strz(buf, &pos, port);
        for (i = 0; i < ps->ind; i++) {
            char *name = ps->list[i].name;
            if (strcmp(name, "ID") == 0) continue;
            app_str(buf, &pos, name);
            app_char(buf, &pos, '=');
            if (strcmp(name, "Name") == 0) {
                seenName = 1;
            }
            if (strcmp(name, "OSName") == 0) {
                seenOSName = 1;
            }
            app_strz(buf, &pos, ps->list[i].value);
        }
        if (!seenName) {
            app_strz(buf, &pos, "Name=TCF Agent");
        }
        if (!seenOSName) {
            app_str(buf, &pos, "OSName=");
            app_strz(buf, &pos, get_os_name());
        }
        if (sendto(udp_server_socket, buf, pos, 0, (struct sockaddr *)dst_addr, sizeof *dst_addr) < 0) {
            trace(LOG_ALWAYS, "Can't send UDP packet to %s: %s",
                  inet_ntoa(dst_addr->sin_addr), errno_to_str(errno));
        }
    }
    return 0;
}

static void udp_send_ack(struct sockaddr_in * addr) {
    assert(is_dispatch_thread());
    ifcind = build_ifclist(udp_server_socket, MAX_IFC, ifclist);
    peer_server_iter(udp_send_peer_sever, addr);
}

static void udp_send_req(void) {
    int i = 0;
    char buf[PKT_SIZE];
    struct sockaddr_in dst_addr;

    trace(LOG_DISCOVERY, "udp_send_req: sending UDP_REQ_INFO");
    memset(&dst_addr, 0, sizeof dst_addr);
    dst_addr.sin_family = AF_INET;
    dst_addr.sin_port = htons((short)discovery_port);
    dst_addr.sin_addr.s_addr = INADDR_BROADCAST;

    buf[i++] = 'T';
    buf[i++] = 'C';
    buf[i++] = 'F';
    buf[i++] = '1';
    buf[i++] = UDP_REQ_INFO;
    buf[i++] = 0;
    buf[i++] = 0;
    buf[i++] = 0;
    if (sendto(udp_server_socket, buf, i, 0, (struct sockaddr *)&dst_addr, sizeof dst_addr) < 0) {
        trace(LOG_ALWAYS, "Can't send UDP packet to %s: %s",
              inet_ntoa(dst_addr.sin_addr), errno_to_str(errno));
    }
}

static void udp_refresh_info(void * arg) {
    int implcit_refresh = (int)arg;
    time_t timenow = time(0);
    int delta;

    assert(is_dispatch_thread());
    trace(LOG_DISCOVERY, "udp_refresh_info, implcit %d, active %d, timenow %ld, last_refresh_time %ld", implcit_refresh, refresh_timer_active, timenow, last_refresh_time);
    if (implcit_refresh) {
        assert(refresh_timer_active);
        if ((delta = timenow - last_refresh_time) < REFRESH_TIME) {
            /* Recent explicit refresh - wait a little longer */
            assert(delta > 0);
            post_event_with_delay(udp_refresh_info, (void *)1, (REFRESH_TIME - delta)*1000*1000);
            return;
        }
        refresh_timer_active = 0;
    }
    else if (refresh_timer_active && last_refresh_time + REFRESH_MIN_TIME < timenow) {
        /* Less than 2 seconds since last refresh - ignore */
        return;
    }
    if (udp_server_socket < 0) {
        /* Server closed */
        return;
    }
    udp_send_ack(NULL);
    udp_send_req();
    last_refresh_time = timenow;
    if (!refresh_timer_active) {
        refresh_timer_active = 1;
        post_event_with_delay(udp_refresh_info, (void *)1, REFRESH_TIME*1000*1000);
    }
}

static void udp_receive_req(void * arg) {
    receive_message * m = arg;

    udp_send_ack(&m->addr);
    loc_free(m);

    pthread_mutex_lock(&udp_discovery_mutex);
    pending_info_req--;
    pthread_mutex_unlock(&udp_discovery_mutex);
}

static int is_remote_host(struct in_addr inaddr) {
    int i;

    for (i = 0; i < ifcind; i++) {
        if (inaddr.s_addr == ifclist[i].addr) {
            return 0;
        }
    }
    return 1;
}

static void udp_receive_ack(void * arg) {
    receive_message * m = arg;
    PeerServer * ps = peer_server_alloc();
    char * p = m->buf + 8;
    char * e = m->buf + m->buf_len;

    assert(is_dispatch_thread());
    while (p < e) {
        char * name = p;
        char * value = NULL;
        while (p < e && *p != '\0' && *p != '=') p++;
        if (p >= e || *p != '=') {
            p = NULL;
            break;
        }
        *p++ = '\0';
        value = p;
        while (p < e && *p != '\0') p++;
        if (p >= e) {
            p = NULL;
            break;
        }
        peer_server_addprop(ps, loc_strdup(name), loc_strdup(value));
        p++;
    }
    if (p != NULL && ps->id != NULL) {
        trace(LOG_DISCOVERY, "udp_receive_ack: received UDP_ACK_INFO, ID=%s", ps->id);
        peer_server_add(ps, STALE_TIME_DELTA);
    }
    else {
        trace(LOG_ALWAYS, "Received malformed UDP ACK packet");
        peer_server_free(ps);
    }
    loc_free(m);

    pthread_mutex_lock(&udp_discovery_mutex);
    pending_info_ack--;
    pthread_mutex_unlock(&udp_discovery_mutex);
}

static void * udp_server_socket_handler(void * x) {
    post_event(udp_refresh_info, NULL);
    for (;;) {
        receive_message * m = loc_alloc(sizeof *m);
        memset(&m->addr, 0, sizeof m->addr);
        m->addr_len = sizeof m->addr;
        m->buf_len = recvfrom(udp_server_socket, m->buf, sizeof m->buf, 0,
                              (struct sockaddr *)&m->addr, &m->addr_len);
        if (m->buf_len < 0) {
            trace(LOG_ALWAYS, "UDP socket receive failed: %s", errno_to_str(errno));
            continue;
        }
        if (m->buf_len < 8 || strncmp(m->buf, "TCF1", 4) != 0) {
            trace(LOG_ALWAYS, "Received malformed UDP packet");
            continue;
        }
        pthread_mutex_lock(&udp_discovery_mutex);
        if (m->buf[4] == UDP_REQ_INFO &&
            pending_info_req < MAX_PENDING_INFO_REQ &&
            is_remote_host(m->addr.sin_addr)) {
            pending_info_req++;
            post_event(udp_receive_req, m);
        }
        else if (m->buf[4] == UDP_ACK_INFO && pending_info_ack < MAX_PENDING_INFO_ACK) {
            pending_info_ack++;
            post_event(udp_receive_ack, m);
        }
        else {
            loc_free(m);
        }
        pthread_mutex_unlock(&udp_discovery_mutex);
    }
    return NULL;
}

static void local_server_change(PeerServer * ps, int changeType, void * arg) {
    trace(LOG_ALWAYS, "local_server_change: ps=0x%x, type=%d, arg=0x%x", ps, changeType, arg);
    if (changeType > 0) {
        /* Boardcast information about new peers */
        udp_send_peer_sever(ps, NULL);
    }
}

int discovery_udp_server(const char * port) {
    int sock;
    int error;
    char *reason;
    const int i = 1;
    struct addrinfo hints;
    struct addrinfo * reslist = NULL;
    struct addrinfo * res = NULL;

    pthread_mutex_init(&udp_discovery_mutex, NULL);
    if (port == NULL) port = DISCOVERY_TCF_PORT;
    memset(&hints, 0, sizeof hints);
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_protocol = IPPROTO_UDP;
    hints.ai_flags = AI_PASSIVE;
    error = loc_getaddrinfo(NULL, port, &hints, &reslist);
    if (error) {
        trace(LOG_ALWAYS, "getaddrinfo error: %s", loc_gai_strerror(error));
        return error;
    }
    sock = -1;
    reason = NULL;
    for (res = reslist; res != NULL; res = res->ai_next) {
        sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
        if (sock < 0) {
            error = errno;
            reason = "create";
            continue;
        }
        if (setsockopt(sock, SOL_SOCKET, SO_BROADCAST, (char *)&i, sizeof(i)) < 0) {
            error = errno;
            reason = "setsockopt(SO_BROADCAST)";
            closesocket(sock);
            sock = -1;
            continue;
        }
        if (bind(sock, res->ai_addr, res->ai_addrlen)) {
            error = errno;
            reason = "bind";
            closesocket(sock);
            sock = -1;
            continue;
        }
        /* Only bind once - don't see how getaddrinfo with the given
         * arguments could return more then one anyway */
        break;
    }
    if (sock < 0) {
        trace(LOG_ALWAYS, "socket %s error: %s", reason, errno_to_str(error));
        loc_freeaddrinfo(reslist);
        return error;
    }
    discovery_port = ntohs(((struct sockaddr_in *)res->ai_addr)->sin_port);
    loc_freeaddrinfo(reslist);

    udp_server_socket = sock;
    ifcind = build_ifclist(udp_server_socket, MAX_IFC, ifclist);
    if ((error = pthread_create(&udp_server_thread, &pthread_create_attr, udp_server_socket_handler, 0)) != 0) {
        trace(LOG_ALWAYS, "can't create a thread: %s", errno_to_str(error));
        return error;
    }
    peer_server_add_listener(local_server_change, NULL);
    return 0;
}
