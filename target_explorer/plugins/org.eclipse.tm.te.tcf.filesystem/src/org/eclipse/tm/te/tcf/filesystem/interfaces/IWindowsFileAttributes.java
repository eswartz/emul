/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.interfaces;

/**
 * Windows specific file system attribute definitions.
 *
 * @see <nop>Windows File Attribute Constants for more details.
 */
public interface IWindowsFileAttributes {

	/**
	 * If set, the file is read-only. Read-only files cannot be modified or deleted.
	 * The attributes does not apply to directories.
	 */
	public int FILE_ATTRIBUTE_READONLY				=	1;

	/**
	 * If set, the file or directory is hidden. Hidden files or directories should not
	 * be included in default directory content lists.
	 */
	public int FILE_ATTRIBUTE_HIDDEN				=	2;

	/**
	 * If set, the file or directory is reserved to be used by the OS.
	 */
	public int FILE_ATTRIBUTE_SYSTEM				=	4;

	/**
	 * The file system object is a directory.
	 */
	public int FILE_ATTRIBUTE_DIRECTORY				=	16;

	/**
	 * If set, the file or directory is an archive file or directory.
	 */
	public int FILE_ATTRIBUTE_ARCHIVE				=	32;

	/**
	 * Reserved for system use.
	 */
	public int FILE_ATTRIBUTE_DEVICE				=	64;

	/**
	 * The file system object is a file with no other attributes set. Valid
	 * only if used exclusively.
	 */
	public int FILE_ATTRIBUTE_NORMAL				=	128;

	/**
	 * If set, the file is used for temporary storage.
	 */
	public int FILE_ATTRIBUTE_TEMPORARY				=	256;

	/**
	 * The file is a sparse file.
	 */
	public int FILE_ATTRIBUTE_SPARSE_FILE			=	512;

	/**
	 * If set, the file or directory has an associated reparse point or is a symbolic link.
	 */
	public int FILE_ATTRIBUTE_REPARSE_POINT			=	1024;

	/**
	 * If set, the file or directory is compressed.
	 */
	public int FILE_ATTRIBUTE_COMPRESSED			=	2048;

	/**
	 * If set, the content of the file is currently not available.
	 * This attribute should not be changed by applications.
	 */
	public int FILE_ATTRIBUTE_OFFLINE				=	4096;

	/**
	 * If set, the file or directory is not indexed.
	 */
	public int FILE_ATTRIBUTE_NOT_CONTENT_INDEXED	=	8192;

	/**
	 * If set, the file or directory is encrypted.
	 */
	public int FILE_ATTRIBUTE_ENCRYPTED				=	16384;

	/**
	 * Reserved for system use.
	 */
	public int FILE_ATTRIBUTE_VIRTUAL				=	65536;
}
