/*
  DescrDirective.java

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
package v9t9.tools.asm.assembler.directive;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;

/**
 * @author Ed
 *
 */
public class DescrDirective extends Directive {

	private final String content;
	private final int line;
	private final String filename;

	public DescrDirective(String filename, int line, String content) {
		this.filename = filename;
		this.line = line;
		this.content = content;
	}

	@Override
	public String toString() {
		return filename + ":" + line;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.IInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass)
			throws ResolveException {
		return new IInstruction[] { this };
	}

	public String getFilename() {
		return filename;
	}
	public int getLine() {
		return line;
	}
	public String getContent() {
		return content;
	}


}
