/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Stack;

/**
 * @author ejs
 *
 */
public class TokenStream {
	private Stack<LineNumberReader> streams;
	private int line;
	
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

	/**
	 * @param fis
	 * @throws FileNotFoundException 
	 */
	public void push(File file) throws FileNotFoundException {
		streams.push(new FileLineNumberReader(file));		
	}
	
	/**
	 * @param text
	 */
	public void push(String text) {
		streams.push(new LineNumberReader(new StringReader(text)));
	}
	public void pop() {
		try {
			streams.pop().close();
		} catch (IOException e) {
		}
	}

	/**
	 * @return
	 */
	public String read() throws IOException {
		if (streams.isEmpty())
			return null;
		LineNumberReader fr = streams.peek();
		int ch;
		while (Character.isWhitespace(ch = fr.read())) /**/;
		if (ch == -1)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append((char) ch);
		line = fr.getLineNumber();
		while (!Character.isWhitespace(ch = fr.read()) && ch != -1) {
			sb.append((char) ch);
		}
		return sb.toString();
	}
	
	public boolean isAtEol() {
		if (streams.isEmpty())
			return true;
		LineNumberReader fr = streams.peek();
		try {
			fr.mark(64);
			try {
				int ch;
				while (Character.isWhitespace(ch = fr.read()))  {
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
		return streams.peek() instanceof FileLineNumberReader 
			? ((FileLineNumberReader) streams.peek()).getFile().toString() : "<string>";
	}

	/**
	 * @return
	 */
	public int getLine() {
		return line;
	}

	/**
	 * @param string
	 * @return
	 */
	public AbortException abort(String string) {
		return new AbortException(getFile(), line, string);
	}

	
}
