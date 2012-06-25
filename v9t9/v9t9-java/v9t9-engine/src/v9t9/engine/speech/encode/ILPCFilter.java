/**
 * 
 */
package v9t9.engine.speech.encode;

/**
 * @author ejs
 *
 */
public interface ILPCFilter {

	void filter(float[] in, int offs, int len, float[] out, float[] y);
}
