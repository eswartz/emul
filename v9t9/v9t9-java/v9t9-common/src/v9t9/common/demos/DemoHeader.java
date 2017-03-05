/*
  DemoHeader.java

  (c) 2012-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.demos;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoHeader {
	
	/** original Java format */
	public static final byte[] DEMO_MAGIC_HEADER_V9t9 = { 'V','9','7','0' };
	
	private byte[] magic;
	
	// ASCIIZ string
	private String machineModel;
	// ASCIIZ string
	private String description;
	private long timestamp = System.currentTimeMillis();
	private int timerRate = 100;
	
	// as ID byte followed by ASCIIZ string;
	// ID of 0 terminates list
	private Map<Integer, String> bufferIdentifiers = new HashMap<Integer, String>();
	
	// as ID byte followed by ASCIIZ string;
	// ID of 0 terminates list
	private Map<String, List<String>> streamIdentifiers = new HashMap<String, List<String>>();
	
	/**
	 * 
	 */
	public DemoHeader(byte[] magic) {
		this.magic = magic;
	}
	
	public String getMachineModel() {
		return machineModel;
	}
	public void setMachineModel(String machineModel) {
		this.machineModel = machineModel;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getTimerRate() {
		return timerRate;
	}
	public void setTimerRate(int timerRate) {
		this.timerRate = timerRate;
	}
	
	
	public Map<Integer, String> getBufferIdentifierMap() {
		return bufferIdentifiers;
	}
	
	public Map<String, List<String>> getStreamIdentifierMap() {
		return streamIdentifiers;
	}
	
	public void read(InputStream is) throws IOException {
		if (is.read() != 0x7f) {
			throw new IOException("unexpected format: wanted 0x80 or 0x7f");
		}
		
		setMachineModel(readString(is));

		// description
		setDescription(readString(is));
		
		// timestamp
		long time = 0;
		for (int i = 0; i < 8; i++) {
			int byt = is.read();
			if (byt < 0)
				throw new EOFException();
			time |= byt << (64 - i * 8 - 8);
		}
		setTimestamp(time);
		
		// timer rate (ticks per sec)
		int rate = is.read();
		if (rate < 0)
			throw new EOFException();
		
		setTimerRate(rate);
		
		// read TOC
		readTOC(is);
	}

	private void readTOC(InputStream is) throws IOException {
		int id;
		while ((id = is.read()) != 0) {
			if (id < 0)
				throw new EOFException();
			String idString = readString(is);
			if (bufferIdentifiers.put(id, idString) != null) {
				throw new IOException("ID " + id + " is registered more than once");
			}
		}
	}
	
	private String readString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		int ch;
		while ((ch = is.read()) > 0) {
			sb.append((char) ch);
		}
		if (ch != 0)
			throw new EOFException();
		return sb.toString();
	}
	
	
	public void write(OutputStream os) throws IOException {
		// machine ID token
		if (bufferIdentifiers.isEmpty())
			os.write(0x7f);
		os.write(0x7f);

		// machine identifier
		writeString(os, machineModel);
		
		// description
		writeString(os, description);
		
		// timestamp
		long time = getTimestamp();
		for (int i = 0; i < 8; i++) {
			os.write((int) (time >>> (64 - i * 8 - 8)));
		}
		
		// timer rate (ticks per sec)
		os.write(getTimerRate());
		
		// write TOC
		for (Map.Entry<Integer, String> entry : bufferIdentifiers.entrySet()) {
			if ((entry.getKey() & 0xff) != entry.getKey())
				throw new IOException("invalid buffer identifier: " + entry.getKey());
			os.write(entry.getKey());
			writeString(os, entry.getValue());
		}
		os.write(0);
	}
	
	private void writeString(OutputStream os, String str) throws IOException {
		if (str != null)
			os.write(str.getBytes());
		os.write(0);
	}
	
	/**
	 * Find code for identifier, or allocate one.
	 * @param identifier string
	 * @return code
	 * @throws IOException 
	 */
	public int findOrAllocateIdentifier(String id) throws IOException {
		int max = 1;
		for (Map.Entry<Integer, String> ent : bufferIdentifiers.entrySet()) {
			if (ent.getValue().equals(id)) {
				return ent.getKey();
			}
			max = Math.max(max, ent.getKey() + 1);
		}
		if (max >= 256)
			throw new IOException("no identifier space left for " + id);
		bufferIdentifiers.put(max, id);
		return max;
	}

	/**
	 * @return
	 */
	public byte[] getMagic() {
		return magic;
	}

	/**
	 * Tell if the magic is one of the V9t9j formats 
	 * @param magic
	 * @return
	 */
	public static boolean isV9t9jFormat(byte[] magic) {
		return Arrays.equals(magic, DEMO_MAGIC_HEADER_V9t9);
	}
	
}