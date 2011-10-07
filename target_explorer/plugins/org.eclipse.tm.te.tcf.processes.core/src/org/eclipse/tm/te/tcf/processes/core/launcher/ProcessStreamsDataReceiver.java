/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.core.launcher;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.services.IProcesses;

/**
 * Remote process streams data receiver implementation.
 */
public class ProcessStreamsDataReceiver extends PlatformObject {
	// The associated writer instance
	private final Writer writer;
	// The list of applicable stream type id's
	private final List<String> streamTypeIds;

	/**
	 * Constructor.
	 *
	 * @param writer The writer instance. Must not be <code>null</code>.
	 * @param streamTypeIds The list of applicable stream type id's or <code>null</code>.
	 *
	 * @see IProcesses
	 */
	public ProcessStreamsDataReceiver(Writer writer, String[] streamTypeIds) {
		Assert.isNotNull(writer);
		this.writer = writer;
		this.streamTypeIds = streamTypeIds != null ? Arrays.asList(streamTypeIds) : null;
	}

	/**
	 * Dispose the data receiver instance.
	 */
	public void dispose() {
		try {
			writer.close();
		}
		catch (IOException e) {
			/* ignored on purpose */
		}
	}

	/**
	 * Returns the associated writer instance.
	 *
	 * @return The associated writer instance.
	 */
	public final Writer getWriter() {
		return writer;
	}

	/**
	 * Returns if or if not the given stream type id is applicable for this data receiver.
	 *
	 * @param streamTypeId The stream type id. Must not be <code>null</code>.
	 * @return <code>True</code> if the given stream type id is applicable for this data receiver, <code>false</code>
	 *         otherwise.
	 */
	public final boolean isApplicable(String streamTypeId) {
		Assert.isNotNull(streamTypeId);
		return streamTypeIds == null || streamTypeIds.contains(streamTypeId);
	}
}
