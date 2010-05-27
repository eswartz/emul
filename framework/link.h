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
 * Double linked list support.
 */

#ifndef D_link
#define D_link

#include <stddef.h>

typedef struct LINK LINK;

struct LINK {
    LINK * next;
    LINK * prev;
};

#define list_init(list) { \
            (list)->next = (list)->prev = (list); \
        }

#define list_is_empty(list) ((list)->next == (list) || (list)->next == NULL)

#define list_remove(item) { \
            (item)->prev->next = (item)->next; \
            (item)->next->prev = (item)->prev; \
            (item)->next = (item)->prev = (item); \
        }

#define list_add_first(item,list) { \
            (item)->next = (list)->next; (item)->prev = (list); \
            (list)->next->prev = (item); (list)->next = (item); \
        }

#define list_add_last(item,list) { \
            (item)->next = (list); (item)->prev = (list)->prev; \
            (list)->prev->next = (item); (list)->prev = (item); \
        }

#define list_concat(item,list) { \
            if (!list_is_empty(list)) { \
                (item)->prev->next = (list)->next; \
                (list)->next->prev = (item)->prev; \
                (item)->prev = (list)->prev; \
                (list)->prev->next = (item); \
            } \
        }

#endif /* D_link */
