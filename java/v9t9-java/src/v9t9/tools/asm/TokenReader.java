/**
 * 
 */
package v9t9.tools.asm;

/**
 * Like Reader, but allows copying
 * @author ejs
 *
 */
public interface TokenReader {
	int read();
	void unread();
	
	int getPos();
	void setPos(int pos);
}
