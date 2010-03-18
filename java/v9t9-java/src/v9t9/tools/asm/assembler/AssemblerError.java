/**
 * 
 */
package v9t9.tools.asm.assembler;

/**
 * @author Ed
 *
 */
public class AssemblerError {
	private final Exception exception;
	private final String filename;
	private final int lineno;
	private final String line;

	public AssemblerError(Exception e, String filename, int lineno, String line) {
		this.exception = e;
		this.filename = filename;
		this.lineno = lineno;
		this.line = line;
	}
	
	public String getDescr() {
		return filename + ":" + lineno;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getLine() {
		return line;
	}
	
	public int getLineno() {
		return lineno;
	}
	
	public Exception getException() {
		return exception;
	}
}
