/**
 * 
 */
package v9t9.gui.client.swt.gl;

import v9t9.common.client.IMonitorEffect;

/**
 * @author Ed
 *
 */
public class MonitorEffect implements IGLMonitorEffect {
	private final MonitorParams params;
	private final IGLMonitorRender render;
	private String label;

	public MonitorEffect(String label, MonitorParams params, IGLMonitorRender render) {
		this.label = label;
		this.params = params;
		this.render = render;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IMonitorEffect#getLabel()
	 */
	@Override
	public String getLabel() {
		return label;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((render == null) ? 0 : render.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitorEffect other = (MonitorEffect) obj;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (render == null) {
			if (other.render != null)
				return false;
		} else if (!render.equals(other.render))
			return false;
		return true;
	}


	public MonitorParams getParams() {
		return params;
	}

	public IGLMonitorRender getRender() {
		return render;
	}
}
