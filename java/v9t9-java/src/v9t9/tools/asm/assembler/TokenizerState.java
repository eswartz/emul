/**
 * 
 */
package v9t9.tools.asm.assembler;

/**
 * @author Ed
 *
 */
public class TokenizerState {

	public final int curtoken;
	public final String image;
	public final int number;
	public final boolean pushedBack;

	/**
	 * @param curtoken
	 * @param image
	 * @param number
	 * @param pushedBack
	 */
	public TokenizerState(int curtoken, String image, int number,
			boolean pushedBack) {
		this.curtoken = curtoken;
		this.image = image;
		this.number = number;
		this.pushedBack = pushedBack;
	}

}
