/*
  TokenizerState.java

  (c) 2010-2013 Edward Swartz

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
