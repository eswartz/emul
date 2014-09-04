/*
  LPCAnalysisFrame.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

class LPCAnalysisFrame {
	/**
	 * @param i 
	 * 
	 */
	public LPCAnalysisFrame(int i) {
		this.coefs = new float[i];
	}
	float invPitch;
	float pitch;
	float power;
	float powerScale;
	int coefsOffs;
	public float[] coefs;
	public float[] residue;

	public void reduce(int levels) {
		for (int x = 0; x < coefs.length; x++) {
			coefs[x] = (int) (coefs[x] * levels) / (float) levels;  
		}
	}
}