/**
 * 
 */
package v9t9.common.demo;

import v9t9.common.machine.IMachine;

/**
 * This interface underlies events that may be replayed to 
 * re-enact a demo.
 * @author ejs
 *
 */
public interface IDemoEvent {

	void execute(IMachine machine);
	
}
