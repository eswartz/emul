/**
 * 
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public interface IArrayAccess {

	/**
	 * Get the sample (interleaving channels)
	 * @param absOffs
	 * @return sample, or 0 if out of range
	 */
	float at(int absOffs);

	/**
	 * @return
	 */
	int size();


}