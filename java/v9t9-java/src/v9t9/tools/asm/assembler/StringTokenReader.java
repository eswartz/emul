/**
 * 
 */
package v9t9.tools.asm.assembler;

/**
 * @author ejs
 *
 */
public class StringTokenReader implements TokenReader {

	private String string;
	private int pos;

	public StringTokenReader(String string) {
		this.string = string;
		this.pos = 0;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.TokenReader#getPos()
	 */
	public int getPos() {
		return pos;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.TokenReader#read()
	 */
	public int read() {
		if (pos < string.length())
			return string.charAt(pos++);
		else
			return -1;
	}
	
	public void unread() {
		pos--;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.TokenReader#setPos(int)
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

}
