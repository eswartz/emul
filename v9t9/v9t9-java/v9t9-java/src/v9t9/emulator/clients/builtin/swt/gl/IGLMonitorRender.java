/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.gl;

/**
 * @author Ed
 *
 */
public interface IGLMonitorRender {
	/** initialize when GL changes */
	void init();
	
	/** render the geometry */
	void render();

}
