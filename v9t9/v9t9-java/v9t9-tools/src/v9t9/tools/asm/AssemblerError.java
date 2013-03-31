/*
  AssemblerError.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

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
