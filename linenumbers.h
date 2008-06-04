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
 * TCF service Line Numbers
 * The service associates locations in the source files with the corresponding
 * machine instruction addresses in the executable object.
 */

#ifndef D_linenumbers
#define D_linenumbers

#include "protocol.h"
#include "context.h"

typedef void (*line_to_address_callback)(void *, unsigned long);

extern int line_to_address(Context * ctx, char * file, int line, int column, line_to_address_callback, void * args);

/*
 * Initialize Line Numbers service.
 */
extern void ini_line_numbers_service(Protocol *);


#endif
