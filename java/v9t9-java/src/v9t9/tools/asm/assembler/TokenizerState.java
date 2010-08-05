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
	public final int streamPos;

	/**
	 * @param curtoken
	 * @param image
	 * @param number
	 * @param pushedBack
	 */
	public TokenizerState(int curtoken, String image, int number,
			boolean pushedBack, int streamPos) {
		this.curtoken = curtoken;
		this.image = image;
		this.number = number;
		this.pushedBack = pushedBack;
		this.streamPos = streamPos;
	}

}
