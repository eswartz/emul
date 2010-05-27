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
 * Local memory heap manager.
 */

#ifndef D_myalloc
#define D_myalloc

#include <stdlib.h>

extern void * loc_alloc(size_t size);
extern void * loc_alloc_zero(size_t size);
extern void * loc_realloc(void * ptr, size_t size);
extern char * loc_strdup(const char * s);
extern char * loc_strdup2(const char * s1, const char * s2);
extern char * loc_strndup(const char * s, size_t len);

extern void loc_free(const void * p);

#endif /* D_myalloc */
