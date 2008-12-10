package v9t9.tools.asm;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;

public class ContentEntry {

	protected String name;
	protected LineNumberReader reader;

	public ContentEntry(String name, LineNumberReader reader) {
		this.name = name;
		this.reader = reader;
	}
	
	public ContentEntry(String name, String text) {
		this.name = name;
		this.reader = new LineNumberReader(new StringReader(text));
	}
	
	/**
	 * Describe the last line read
	 * @return
	 */
	public String describe() {
		// starts at zero, and describes last line, so at right value
		return name + ":" + reader.getLineNumber();
	}

	/**
	 * Get the next line, or <code>null</code>
	 * @return
	 * @throws IOException
	 */
	public String next() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public int getLine() {
		return reader.getLineNumber();
	}

}