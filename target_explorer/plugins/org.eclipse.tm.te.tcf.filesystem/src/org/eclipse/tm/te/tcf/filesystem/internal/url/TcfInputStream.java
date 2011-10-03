/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)- [345387]Open the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.url;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DoneClose;
import org.eclipse.tm.tcf.services.IFileSystem.DoneOpen;
import org.eclipse.tm.tcf.services.IFileSystem.DoneRead;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;

/**
 * The TCF input stream returned by {@link TcfURLConnection#getInputStream()}.
 *
 */
public class TcfInputStream extends InputStream {
	// Default chunk size while pumping the data.
	private static final int DEFAULT_CHUNK_SIZE = 5 * 1024;

	// If the stream has already connected.
	private boolean connected;

	// The TCF agent peer.
	IPeer peer;
	// The remote path to the resource.
	String path;

	// The TCF channel used to open and read the resource.
	IChannel channel;
	// The file system service used to open and read the resource.
	IFileSystem service;
	// The file's handle
	IFileHandle handle;

	// Current reading position
	long position;
	// The byte array used to buffer data.
	byte[] buffer;
	// The offset being read in the buffer.
	int offset;

	// If the reading has reached the end of the file.
	boolean EOF;
	// If the stream has been closed.
	boolean closed;
	// The current error during reading.
	Exception ERROR;

	// The chunk size of the reading buffer.
	int chunk_size = 0;

	// Channel opening timeout.
	long connectTimeout;
	// File opening timeout.
	long openTimeout;
	// File reading timeout.
	long readTimeout;
	// File closing timeout.
	long closeTimeout;

	/**
	 * Create a TCF input stream connected the specified peer with specified
	 * path to the remote resource.
	 *
	 * @param peer
	 *            The TCF agent peer.
	 * @param path
	 *            The path to the remote resource.
	 */
	public TcfInputStream(IPeer peer, String path) {
		this(peer, path, DEFAULT_CHUNK_SIZE);
	}

	/**
	 * Create a TCF input stream connected the specified peer with specified
	 * path to the remote resource using the specified buffer size.
	 *
	 * @param peer
	 *            The TCF agent peer.
	 * @param path
	 *            The path to the remote resource.
	 * @param chunk_size
	 *            The buffer size.
	 */
	public TcfInputStream(IPeer peer, final String path, int chunk_size) {
		this.peer = peer;
		this.path = path;
		this.chunk_size = chunk_size;
	}

	/**
	 * Set the timeout for opening a TCF channel.
	 *
	 * @param connectTimeout the timeout in milliseconds.
	 */
	void setConnectTimeout(long channelTimeout) {
		this.connectTimeout = channelTimeout;
	}

	/**
	 * Set the timeout for opening a file.
	 *
	 * @param openTimeout the timeout in milliseconds.
	 */
	void setOpenTimeout(long openTimeout) {
		this.openTimeout = openTimeout;
	}

	/**
	 * Set the timeout for reading a file.
	 *
	 * @param readTimeout the timeout in milliseconds.
	 */
	void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	/**
	 * Set the timeout for closing a file.
	 *
	 * @param closeTimeout the timeout in milliseconds.
	 */
	void setCloseTimeout(long closeTimeout) {
		this.closeTimeout = closeTimeout;
	}


	/**
	 * Open and connect the input stream to the agent peer.
	 *
	 * @throws IOException
	 *             Thrown when the connecting is failed.
	 */
	private void connect() throws IOException {
		final Rendezvous rendezvous = new Rendezvous();
		// Open the channel
		channel = peer.openChannel();
		channel.addChannelListener(new IChannelListener() {
			@Override
			public void onChannelOpened() {
				Assert.isTrue(Protocol.isDispatchThread());
				service = channel.getRemoteService(IFileSystem.class);
				rendezvous.arrive();
			}

			@Override
			public void onChannelClosed(Throwable error) {
			}

			@Override
			public void congestionLevel(int level) {
			}
		});
		// Wait for the end of the opening.
		try {
			rendezvous.waiting(connectTimeout);
		} catch (InterruptedException e) {
			throw new IOException(Messages.TcfInputStream_OpenTCFTimeout);
		}
		if (service != null) {
			rendezvous.reset();
			final FileSystemException[] errors = new FileSystemException[1];
			// Open the file.
			service.open(path, IFileSystem.TCF_O_READ, null, new DoneOpen() {
				@Override
				public void doneOpen(IToken token, FileSystemException error,
						IFileHandle hdl) {
					errors[0] = error;
					handle = hdl;
					// Rendezvous
					rendezvous.arrive();
				}
			});
			try {
				rendezvous.waiting(openTimeout);
			} catch (InterruptedException e) {
				throw new IOException(Messages.TcfInputStream_OpenFileTimeout);
			}
			if (errors[0] != null) {
				IOException exception = new IOException(errors[0].toString());
				exception.initCause(errors[0]);
				throw exception;
			}
			if (handle == null) {
				throw new IOException(Messages.TcfInputStream_NoFileReturned);
			}
		} else {
			throw new IOException(Messages.TcfInputStream_NoFSServiceAvailable);
		}
		connected = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		if (!connected) {
			connect();
		}
		if (closed)
			throw new IOException(Messages.TcfInputStream_StreamClosed);
		if (ERROR != null) {
			IOException exception = new IOException(ERROR.toString());
			exception.initCause(ERROR);
			throw exception;
		}
		if (buffer == null) {
			if (EOF) {
				return -1;
			}
			readBlock();
			return read();
		}
		if (EOF) {
			if (offset == buffer.length) {
				return -1;
			}
			// Note that convert the byte to an integer correctly
			return 0xff & buffer[offset++];
		}
		if (offset == buffer.length) {
			readBlock();
			return read();
		}
		// Note that convert the byte to an integer correctly
		return 0xff & buffer[offset++];
	}

	/**
	 * Read a block of data into the buffer. Reset the offset, increase the
	 * current position and remember the EOF status. If there's an error,
	 * remember it for read() to check.
	 */
	private void readBlock() {
		final Rendezvous rendezvous = new Rendezvous();
		service.read(handle, position, chunk_size, new DoneRead() {
			@Override
			public void doneRead(IToken token, FileSystemException error,
					byte[] data, boolean eof) {
				if (error != null) {
					ERROR = error;
				}
				if (data == null) {
					ERROR = new IOException(Messages.TcfInputStream_NoDataAvailable);
				}
				EOF = eof;
				buffer = data;
				if (buffer != null)
					position += buffer.length;
				offset = 0;
				// Rendezvous
				rendezvous.arrive();
			}
		});
		// Waiting for reading.
		try {
			rendezvous.waiting(readTimeout);
		} catch (InterruptedException e) {
			ERROR = new IOException(Messages.TcfInputStream_ReadTimeout);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		if (connected && !closed) {
			final Rendezvous rendezvous = new Rendezvous();
			service.close(handle, new DoneClose() {
				@Override
				public void doneClose(IToken token, FileSystemException error) {
					rendezvous.arrive();
				}
			});
			try {
				rendezvous.waiting(closeTimeout);
			} catch (InterruptedException e) {
				throw new IOException(Messages.TcfInputStream_CloseTimeout);
			}
			channel.close();
			super.close();
			closed = true;
		}
	}
}
