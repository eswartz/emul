/*
  TokenStream.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author ejs
 *
 */
public class TokenStream {
	private Stack<LineNumberReader> streams;
	private int line;
	private Map<LineNumberReader, String> stringStreams = new HashMap<LineNumberReader, String>();
	
	class FileLineNumberReader extends LineNumberReader {

		private final File file;

		/**
		 * @param in
		 * @throws FileNotFoundException 
		 */
		public FileLineNumberReader(File file) throws FileNotFoundException {
			super(new FileReader(file));
			this.file = file;
		}
	
		/**
		 * @return the file
		 */
		public File getFile() {
			return file;
		}
	}
	/**
	 * 
	 */
	public TokenStream() {
		streams = new Stack<LineNumberReader>();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getLocation();
	}
	/**
	 * @param fis
	 * @throws FileNotFoundException 
	 */
	public void push(File file) throws FileNotFoundException {
		FileLineNumberReader reader = new FileLineNumberReader(file);
		reader.setLineNumber(1);
		streams.push(reader);		
	}
	
	public LineNumberReader push(String name, String text) {
		LineNumberReader reader = new LineNumberReader(new StringReader(text));
		reader.setLineNumber(1);
		streams.push(reader);
		stringStreams.put(reader, name);
		return reader;
	}
	public void pop() {
		try {
			LineNumberReader reader = streams.pop();
			stringStreams.remove(reader);
			reader.close();
		} catch (IOException e) {
		}
	}

	/** read a forth token */
	public String read() throws IOException {
		int ch;
		LineNumberReader fr = null;
		while (true) {
			if (streams.isEmpty())
				return null;
			fr = streams.peek();
			// skip initial whitespace
			while (Character.isWhitespace(ch = fr.read())) /**/;
			if (ch == -1) {
				streams.pop().close();
				continue;
			}
			else
				break;
		}
		StringBuilder sb = new StringBuilder();
		sb.append((char) ch);
		line = fr.getLineNumber();
		fr.mark(1);
		while (!Character.isWhitespace(ch = fr.read()) && ch != -1) {
			sb.append((char) ch);
			fr.mark(1);
		}
		if (ch == '\n' || ch == '\r')
			fr.reset();
		return sb.toString();
	}

	
	/** skip past the given character */
	public void readThrough(char stop) throws IOException {
		int ch;
		LineNumberReader fr = null;
		while (true) {
			if (streams.isEmpty())
				throw new EOFException();
			fr = streams.peek();
			while (true) {
				ch = fr.read();
				if (ch == stop)
					return;
				if (ch == -1) {
					throw new EOFException();
				}
			}
		}
	}

	public boolean isAtEol(int forLine) {
		if (streams.isEmpty())
			return true;
		LineNumberReader fr = streams.peek();
		try {
			fr.mark(64);
			try {
				int ch;
				while (Character.isWhitespace(ch = fr.read()))  {
					if (fr.getLineNumber() > forLine)
						return true;
					if (ch == '\n')
						return true;
				}
			} finally {
				fr.reset();
			}
		} catch (IOException e) {
			return true;
		}
		return false;
	}
	

	/**
	 * @return
	 */
	public String getFile() {
		if (streams.isEmpty())
			return "<empty>";
		
		LineNumberReader curReader = streams.peek();
		if (curReader instanceof FileLineNumberReader) {
			return ((FileLineNumberReader) curReader).getFile().toString();
		} else {
			String name = stringStreams.get(curReader);
			return name != null ? name : "<string>";
		}
	}

	/**
	 * @return
	 */
	public int getLine() {
		//return line;
		if (streams.isEmpty())
			return line;
		return streams.peek().getLineNumber();
	}

	/**
	 * @param string
	 * @return
	 */
	public AbortException abort(String string) {
		return new AbortException(getFile(), line+1, string);
	}

	/**
	 * @return
	 */
	public String getLocation() {
		return getFile() + ":" + getLine();
	}

	/**
	 * @return 
	 * 
	 */
	public String readToEOL() {
		if (streams.isEmpty())
			return null;
		try {
			return streams.peek().readLine();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @return
	 */
	public int readChar() throws AbortException {
		if (streams.isEmpty())
			return 0;
		try {
			return streams.peek().read();
		} catch (IOException e) {
			throw abort(e.toString());
		}
	}

	/**
	 * @return
	 */
	public LineNumberReader getCurrentReader() {
		return streams.peek();
	}

	
}
