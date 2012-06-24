/**
 * 
 */
package v9t9.engine.speech.encode;


/**
 * @author ejs
 *
 */
public interface ILPCEngine {

	LPCAnalysisFrame analyze(float[] x, int offs, int len);
	
	void synthesize( float[] y, int offs, int len, int playbackHz, LPCAnalysisFrame frame);
}