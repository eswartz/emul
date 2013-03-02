/*
  RtLPCFilter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

/**
 * @author ejs
 *
 */
public class RtLPCFilter implements ILPCFilter {

	/**
	 * 
	 */
	public RtLPCFilter() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.speech.encode.ILPCFilter#filter(float[], float[], int, int)
	 */
	@Override
	public void filter(float[] in, int offs, int len, float[] out, float[] y) {

	}

}
