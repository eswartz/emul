/**
 * 
 */
package v9t9.tools.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * A single file to parse.
 * @author ejs
 *
 */
public class FileContentEntry {
	private String name;
	private LineNumberReader reader;

	public FileContentEntry(File file) throws IOException {
		this.name = file.getPath();
		this.reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
	}

	public FileContentEntry(String name, String text) throws IOException {
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
	public File getFile() {
		return new File(name);
	}

	public int getLine() {
		return reader.getLineNumber();
	}
}
