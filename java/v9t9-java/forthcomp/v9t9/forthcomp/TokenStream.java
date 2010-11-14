/**
 * 
 */
package v9t9.forthcomp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Stack;

import v9t9.forthcomp.AbortException;

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
		int ch;
		LineNumberReader fr = null;
		while (true) {
			if (streams.isEmpty())
				return null;
			fr = streams.peek();
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
		fr.reset();
		return sb.toString();
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
		return streams.peek() instanceof FileLineNumberReader 
			? ((FileLineNumberReader) streams.peek()).getFile().toString() : "<string>";
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
	 * 
	 */
	public void readToEOL() {
		if (streams.isEmpty())
			return;
		try {
			streams.peek().readLine();
		} catch (IOException e) {
		}
	}

	/**
	 * @return
	 */
	public char readChar() throws AbortException {
		if (streams.isEmpty())
			return 0;
		try {
			return (char) streams.peek().read();
		} catch (IOException e) {
			throw abort(e.toString());
		}
	}

	
}
