/**
 * 
 */
package v9t9.common.files;

/**
 * This is the interface to files handled by the emulator (either by emulating a
 * DSR or through direct manipulation for utilities).
 * @author ejs
 *
 */
public interface IEmulatedFile {
	/** Tell whether the file exists */
	boolean exists();
	
	/** Check validity, returning a string describing any problems or <code>null</code> for success */
	String isValid();
	
	/** Get the FDR */
	FDR getFDR();

	
}
