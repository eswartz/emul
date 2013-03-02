/*
  ILPCEngine.java

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
public interface ILPCEngine {

	
	LPCAnalysisFrame analyze(float[] x, int offs, int len);
	
	void synthesize( float[] y, int offs, int len, int playbackHz, LPCAnalysisFrame frame);

	/**
	 * @return
	 */
	float[] getY();
}