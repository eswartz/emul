/**
 * 
 */
package v9t9.common.demos;

/**
 * This interface underlies events that represent the behavior
 * of a demo.
 * @author ejs
 *
 */
public interface IDemoEvent {

	/**
	 * Get a unique identifier for the event, which can identify it
	 * for purposes of serialization and playback
	 * @return
	 */
	String getIdentifier();
	
}
