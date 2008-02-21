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
 * Local memory heap manager.
 */

#ifndef D_myalloc
#define D_myalloc

#include <stdlib.h>

extern void * loc_alloc(size_t size);
extern void * loc_alloc_zero(size_t size);
extern void * loc_realloc(void *ptr, size_t size);
extern void loc_free(void *p);
extern char * loc_strdup(const char *s);
extern char * loc_strdup2(const char *s1, const char *s2);
extern char * loc_strndup(const char *s, size_t len);

#endif
