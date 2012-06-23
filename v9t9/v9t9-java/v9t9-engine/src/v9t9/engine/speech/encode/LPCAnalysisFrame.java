/**
 * 
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
	float power;
	float powerScale;
	public float[] coefs;
	public float[] residue;

	public void reduce(int levels) {
		for (int x = 0; x < coefs.length; x++) {
			coefs[x] = (int) (coefs[x] * levels) / (float) levels;  
		}
	}
}