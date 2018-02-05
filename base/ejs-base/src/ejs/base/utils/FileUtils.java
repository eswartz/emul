/*
  FileUtils.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author ejs
 */
public class FileUtils  {
	/**
	 * Overcome egregious buggy surprising behavior in {@link InputStream#skip(long)}
	 * @param in
	 * @param nBytes
	 * @throws IOException
	 */
	public static void skipFully(InputStream in, long nBytes) throws IOException {
    	long remaining = nBytes;
    	while (remaining > 0) {
    		long skipped = in.skip(remaining);
    		if (skipped == 0)
    			throw new EOFException();
    		remaining -= skipped;
    	}
    }

	public static String readInputStreamTextAndClose(InputStream is) throws IOException {
		return new String(readInputStreamContentsAndClose(is));
	}

	public static byte[] readInputStreamContentsAndClose(InputStream is) throws IOException {
		return readInputStreamContentsAndClose(is, Integer.MAX_VALUE);
	}

	public static byte[] readInputStreamContentsAndClose(InputStream is, int maxsize) throws IOException {
		try {
			return readInputStreamContents(is, maxsize);
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { } 
			}
		}
	}
	public static byte[] readInputStreamContents(InputStream is, int maxsize) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] result = new byte[1024];
		int len;
		int left = maxsize;
		while (left > 0  && (len = is.read(result, 0, Math.min(result.length, left))) >= 0) {
			int use = Math.min(len, left);
			bos.write(result, 0, use);
			left -= use;
		}
		return bos.toByteArray();
	}

	public static String readFileText(File file) throws IOException {
		FileInputStream stream = null;
		byte[] result;
		try {
			long size = file.length();
			stream = new FileInputStream(file);
			result = new byte[(int) size];
			stream.read(result);
		} finally {
			if (stream != null)
				stream.close();
		}
		return new String(result);
	}
	public static void writeFileText(File file, String text) throws IOException {
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(file);
			stream.write(text.getBytes());
		} finally {
			if (stream != null)
				stream.close();
		}
		
	}

	/**
	
	 */
	public static void writeOutputStreamContentsAndClose(OutputStream os, byte[] memory, int size) throws IOException 
	{
		try {
			os.write(memory, 0, size);
		} finally {
			os.close();
		}
	}

	/**
	 * Get the MD5 hash of the given content as a hex-encoded string.
	 * @return String
	 * @throws NoSuchAlgorithmException 
	 */
	public static String getMD5Hash(byte[] content) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
		byte[] md5 = digest.digest(content);
		StringBuilder sb = new StringBuilder();
		for (byte b : md5) {
			sb.append(HexUtils.toHex2(b));
		}
		return sb.toString();
	}


	/**
	 * @param uri
	 * @return
	 */
	public static String abbreviateURI(URI uri, int maxlength) {
		String text;
		try {
			text = new File(uri).toString();
		} catch (IllegalArgumentException e) {
			text = uri.toString();
		}
		if (text.length() > maxlength) {
			int prev = text.length();
			do {
				prev = text.lastIndexOf('/', prev - 1);
			} while (prev > 0 && text.length() - prev < 20);
			text = prev > 0 ? "..." + text.substring(prev) : text;
		}
		return text;
	}
}
