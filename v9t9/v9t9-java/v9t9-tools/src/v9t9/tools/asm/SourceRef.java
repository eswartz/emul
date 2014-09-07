/**
 * 
 */
package v9t9.tools.asm;

/**
 * @author ejs
 *
 */
public class SourceRef {

	public String filename;
	public int lineno;
	public String line;
	
	public SourceRef from;

	public SourceRef(String filename, int lineno, String line) {
		this(filename, lineno, line, null);
	}
	public SourceRef(String filename, int lineno, String line, SourceRef from) {
		this.filename = filename;
		this.lineno = lineno;
		this.line = line;
		this.from = from;
	}


	@Override
	public String toString() {
		return filename + ":" + lineno + ": " + line;
	}
	public String format(String message) {
		StringBuilder sb = new StringBuilder();
		SourceRef ref = this;
		sb.append(message);
		while (ref != null) {
			sb.append("\n\tfrom "+ref.filename + ":" + ref.lineno);
			if (ref.line != null)
				sb.append(": " + ref.line);
			ref = ref.from;
		}
		return sb.toString();
	}
	
}
