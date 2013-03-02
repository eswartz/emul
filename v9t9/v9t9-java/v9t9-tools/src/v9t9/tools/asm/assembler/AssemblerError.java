/*
  AssemblerError.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
