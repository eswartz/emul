/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River) - [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.url;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DoneWrite;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;

/**
 * The TCF output stream returned by {@link TcfURLConnection#getOutputStream()}.
 *
 */
public class TcfOutputStream extends OutputStream {
	// Default chunk size while pumping the data.
	private static final int DEFAULT_CHUNK_SIZE = 5 * 1024;

	// Current writing position
	long position;
	// The byte array used to buffer data.
	byte[] buffer;
	// The offset being written in the buffer.
	int offset;

	// If the stream has been closed.
	boolean closed;
	// The current error during writing.
	Exception ERROR;

	// The chunk size of the writing buffer.
	int chunk_size = 0;

	// The URL Connection
	TcfURLConnection connection;

	// The timeout for writing data.
	int timeout;
	/**
	 * Create a TCF output stream connected the specified peer with specified
	 * path to the remote resource.
	 *
	 * @param peer
	 *            The TCF agent peer.
	 * @param path
	 *            The path to the remote resource.
	 */
	public TcfOutputStream(TcfURLConnection connection) {
		this(connection, DEFAULT_CHUNK_SIZE);
	}

	/**
	 * Create a TCF output stream connected the specified peer with specified
	 * path to the remote resource using the specified buffer size.
	 *
	 * @param peer
	 *            The TCF agent peer.
	 * @param path
	 *            The path to the remote resource.
	 * @param chunk_size
	 *            The buffer size.
	 */
	public TcfOutputStream(TcfURLConnection connection, int chunk_size) {
		this.connection = connection;
		this.chunk_size = chunk_size;
		buffer = new byte[chunk_size];
		offset = 0;
	}

	/**
	 * Set the timeout for writing a file.
	 *
	 * @param timeout The timeout for writing a file.
	 */
	void setTimeout(int timeout){
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		if (closed)
			throw new IOException(Messages.TcfOutputStream_StreamClosed);
		if (ERROR != null) {
			IOException exception = new IOException(ERROR.toString());
			exception.initCause(ERROR);
			throw exception;
		}
		if (offset < buffer.length) {
			buffer[offset++] = (byte) b;
		}
		if (offset == buffer.length)
			flush();
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (offset > 0) {
			final Rendezvous rendezvous = new Rendezvous();
			IFileSystem service = connection.handle.getService();
			service.write(connection.handle, position, buffer, 0, offset, new DoneWrite() {
				@Override
				public void doneWrite(IToken token, FileSystemException error) {
					if (error != null) {
						ERROR = error;
					}
					position += offset;
					offset = 0;
					rendezvous.arrive();
				}
			});
			// Waiting for writing.
			try {
				rendezvous.waiting(timeout);
			} catch (InterruptedException e) {
				ERROR = new IOException(Messages.TcfOutputStream_WriteTimeout);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (!closed) {
			connection.closeStream(this);
			closed = true;
		}
	}
}
