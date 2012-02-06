/**
 * 
 */
package v9t9.common.asm;


/**
 * @author ejs
 *
 */
public interface ILabelDetector {

	/**
	 * @param llp
	 */
	void defineStandardLabels(BasePhase llp);

	/**
	 * @param inst
	 */
	//void populateLabelsReachableFrom(BasePhase llp, IHighLevelInstruction inst);

	Object getLabels(BasePhase llp, IHighLevelInstruction inst);
}
