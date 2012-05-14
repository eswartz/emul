/**
 * 
 */
package v9t9.common.demo;

import java.io.IOException;
import java.net.URI;

import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;

/**
 * @author ejs
 *
 */
public interface IDemoManager {

	/** locate the demos */
	IPathFileLocator getDemoLocator();
	
	/** get an array of all known demos along paths */
	IDemo[] getDemos();
	
	/** refresh demo list */
	void reload();
	
	/** register this demo (e.g., just recorded) */
	void addDemo(IDemo demo);

	/**
	 * remove a demo
	 */
	void removeDemo(IDemo demo);
	
	IDemoInputStream createDemoReader(URI uri) throws IOException, NotifyException;
	IDemoOutputStream createDemoWriter(URI uri) throws IOException, NotifyException;

}
