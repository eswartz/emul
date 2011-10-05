/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.model;

/**
 * The enumeration that defines the states of a file's local cache, including "consistent", "modified",
 * "outdated" and "conflict".
 */
public enum CacheState {
	consistent,	// Neither of the local file and the remote file has been changed since checking out.
	modified,	// The local file has changed while the remote file has not since checking out.
	outdated,	// The remote file has changed while the local file has not since checking out.
	conflict	// Both the local file and the remote file have changed since checking out.
}
