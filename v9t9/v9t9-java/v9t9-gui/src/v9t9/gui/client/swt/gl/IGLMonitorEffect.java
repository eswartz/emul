/**
 * 
 */
package v9t9.gui.client.swt.gl;

import v9t9.common.client.IMonitorEffect;

/**
 * @author ejs
 *
 */
public interface IGLMonitorEffect extends IMonitorEffect {
	MonitorParams getParams();
	IGLMonitorRender getRender();
}
