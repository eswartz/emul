/**
 * 
 */
package v9t9.common.client;

import java.net.URI;

/**
 * This interface represents a mechanism for discovering or
 * analyzing content usable by the emulator,
 * either through a file, dragged-in content, etc., intended for
 * use in the client in interactive usage.
 * @author ejs
 *
 */
public interface IEmulatorContentSourceProvider {
	/**
	 * Analyze the given URI (non-recursively) and discover
	 * any content sources.  This may be a long-running operation.
	 * @param uri the location to examine.  If a directory, descend
	 * only one level, but no more.
	 * @return array of source (never <code>null</code>)
	 */
	IEmulatorContentSource[] analyze(URI uri);
}
