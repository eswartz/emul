/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLTargetDataTypeDirective extends LLTargetDirective {

	private final TypeEngine typeEngine;

	public LLTargetDataTypeDirective(TypeEngine typEngine) {
		this.typeEngine = typEngine;
	}

	public LLTargetDataTypeDirective(TypeEngine typEngine, String desc) {
		this.typeEngine = typEngine;
		String[] pieces = desc.split("-");
		int idx = 0;
		String end = pieces[idx++];
		typeEngine.setLittleEndian(end.equals("e"));
		
		String tok = pieces[idx++];
		if (tok.startsWith("p:")) {
			String[] ptr = tok.split(":");
			
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("target datalayout = \"");
		sb.append(typeEngine.isLittleEndian() ? 'e' : 'E');
		sb.append('-');
		sb.append("p:").append(typeEngine.getPtrBits()).append(':').append(typeEngine.getPtrAlign());
		sb.append('-');
		sb.append("s0:").append(typeEngine.getStackMinAlign()).append(':').append(typeEngine.getStackAlign());
		sb.append('-');
		sb.append("a0:").append(typeEngine.getStructMinAlign()).append(':').append(typeEngine.getStructAlign());
		sb.append("\"\n");
		return sb.toString();
	}
	
}
