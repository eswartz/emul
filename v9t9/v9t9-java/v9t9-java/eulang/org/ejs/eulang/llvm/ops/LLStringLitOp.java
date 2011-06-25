/**
 * 
 */
package org.ejs.eulang.llvm.ops;


import org.ejs.eulang.types.LLArrayType;

/**
 * @author ejs
 *
 */
public class LLStringLitOp extends BaseLLConstOperand {

	private final LLArrayType arrayType;
	private final String text;

	/**
	 * 
	 */
	public LLStringLitOp(LLArrayType type, String text) {
		super(type);
		this.arrayType = type;
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.BaseLLConstOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((arrayType == null) ? 0 : arrayType.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLStringLitOp other = (LLStringLitOp) obj;
		if (arrayType == null) {
			if (other.arrayType != null)
				return false;
		} else if (!arrayType.equals(other.arrayType))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('c');
		sb.append('"');
		for (int idx = 0; idx < text.length(); idx++) {
			char ch = text.charAt(idx);
			if (ch < 0x20 || ch >= 0x7f) {
				sb.append('\\');
				if (ch < 0x10) sb.append('0');
				sb.append(Integer.toHexString(ch & 0xff));
			} else {
				sb.append(ch);
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
