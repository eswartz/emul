/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Command line interpreter.
 */

#ifndef D_cmdline
#define D_cmdline

#include "channel.h"

extern Channel *chan;

extern void cmdline_suspend(void);
extern void cmdline_resume(void);
extern void ini_cmdline_handler(void);

#endif
