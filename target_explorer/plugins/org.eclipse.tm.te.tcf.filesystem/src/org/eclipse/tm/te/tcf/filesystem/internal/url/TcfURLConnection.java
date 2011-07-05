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
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
/**
 * The URL connection returned by TCF url stream service used to handle
 * "tcf" stream protocol.
 */
public class TcfURLConnection extends URLConnection {
	// Default connecting timeout.
	private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
	// Default file opening timeout.
	private static final int DEFAULT_OPEN_TIMEOUT = 5000;
	// Default file reading timeout.
	private static final int DEFAULT_READ_TIMEOUT = 5000;
	//Default file closing timeout.
	private static final int DEFAULT_CLOSE_TIMEOUT = 5000;
	// The schema name of the stream protocol.
	public static final String PROTOCOL_SCHEMA = "tcf"; //$NON-NLS-1$
	// The input stream of this connection.
	private TcfInputStream inputStream;
	// The TCF agent peer of the connection.
	private IPeer peer;
	// The path to the resource on the remote file system.
	private String path;
	// The timeout for opening a file.
	private long openTimeout;
	// The timeout for closing a file.
	private long closeTimeout;
	/**
	 * Create a TCF URL Connection using the specified url. The
	 * format of this URL should be:
	 * tcf:///<TCF_AGENT_ID>/remote/path/to/the/resource...
	 * The stream protocol schema is designed in this way in order
	 * to retrieve the agent peer ID without knowing the structure
	 * of a TCF peer id.
	 *
	 * @param url The URL of the resource.
	 */
	public TcfURLConnection(URL url) {
		super(url);
		// The path should have already contained the peer's id like:
		// /<TCF_AGENT_ID>/remote/path/to/the/resource...
		path = url.getPath();
		int slash = path.indexOf("/", 1); //$NON-NLS-1$
		String peerId;
		if (slash != -1){
			peerId = path.substring(1, slash);
			path = path.substring(slash);
			if (path.matches("/[A-Za-z]:.*")) path = path.substring(1); //$NON-NLS-1$
		}else{
			peerId = path.substring(1);
		}
		//Get the peer using the peer id.
		peer = Protocol.getLocator().getPeers().get(peerId);
		// Set default timeout.
		setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
		setOpenTimeout(DEFAULT_OPEN_TIMEOUT);
		setReadTimeout(DEFAULT_READ_TIMEOUT);
		setCloseTimeout(DEFAULT_CLOSE_TIMEOUT);
	}

	/**
	 * Get the timeout for closing a file.
	 *
	 * @return the timeout in milliseconds.
	 */
	public long getCloseTimeout() {
		return closeTimeout;
	}

	/**
	 * Set the timeout for closing a file.
	 *
	 * @param closeTimeout the timeout in milliseconds.
	 */
	public void setCloseTimeout(long closeTimeout) {
		this.closeTimeout = closeTimeout;
	}
	/**
	 * Get the timeout for opening a file.
	 *
	 * @return the timeout in milliseconds.
	 */
	public long getOpenTimeout() {
		return openTimeout;
	}

	/**
	 * Set the timeout for opening a file.
	 *
	 * @param openTimeout the timeout in milliseconds.
	 */
	public void setOpenTimeout(long openTimeout) {
		this.openTimeout = openTimeout;
	}
	/*
	 * (non-Javadoc)
	 * @see java.net.URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException {
		if (!connected) {
			inputStream = new TcfInputStream(peer, path);
			inputStream.setConnectTimeout(getConnectTimeout());
			inputStream.setOpenTimeout(getOpenTimeout());
			inputStream.setReadTimeout(getReadTimeout());
			inputStream.setCloseTimeout(getCloseTimeout());
			connected = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.net.URLConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (!connected)
			connect();
		return inputStream;
	}
}
