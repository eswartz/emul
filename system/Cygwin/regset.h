/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
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
 * This header file provides definition of REG_SET - a structure that can
 * hold values of target CPU registers.
 */
#ifdef __cplusplus
extern "C" {
#endif

#if defined(__CYGWIN__)
   typedef CONTEXT REG_SET;
#endif

#ifdef __cplusplus
}
#endif
