/**
 * 
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public interface IWriteArrayAccess extends IArrayAccess {
	/**
	 * Set the sample value 
	 * @param sampleOffs
	 * @param value
	 */
	void set(int sampleOffs, float value);
}
