/**
 * 
 */
package v9t9.tools.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * A single file to parse.
 * @author ejs
 *
 */
public class FileEntry {
	private String name;
	private LineNumberReader reader;

	public FileEntry(File file) throws IOException {
		this.name = file.getPath();
		this.reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
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
}
