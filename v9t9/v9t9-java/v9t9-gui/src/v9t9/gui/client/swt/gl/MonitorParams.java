/**
 * 
 */
package v9t9.gui.client.swt.gl;

/**
 * @author Ed
 *
 */
public class MonitorParams {

	private final String shaderBase;
	private final String texture;
	private final int minFilter;
	private final int magFilter;
	private boolean refreshRealtime;

	public MonitorParams(String shaderBase, String texture, int minFilter, int maxFilter, boolean refreshRealtime) {
		this.shaderBase = shaderBase;
		this.texture = texture;
		this.minFilter = minFilter;
		this.magFilter = maxFilter;
		this.refreshRealtime = refreshRealtime;
	}
	
	public MonitorParams(String shaderBase, String texture, int minFilter, int maxFilter) {
		this(shaderBase, texture, minFilter, maxFilter, false);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + magFilter;
		result = prime * result + minFilter;
		result = prime * result
				+ ((shaderBase == null) ? 0 : shaderBase.hashCode());
		result = prime * result + ((texture == null) ? 0 : texture.hashCode());
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
		MonitorParams other = (MonitorParams) obj;
		if (magFilter != other.magFilter)
			return false;
		if (minFilter != other.minFilter)
			return false;
		if (shaderBase == null) {
			if (other.shaderBase != null)
				return false;
		} else if (!shaderBase.equals(other.shaderBase))
			return false;
		if (texture == null) {
			if (other.texture != null)
				return false;
		} else if (!texture.equals(other.texture))
			return false;
		return true;
	}


	public String getShaderBase() {
		return shaderBase;
	}
	public String getTexture() {
		return texture;
	}
	public int getMinFilter() {
		return minFilter;
	}
	public int getMagFilter() {
		return magFilter;
	}
	
	/**
	 * @return the refreshRealtime
	 */
	public boolean isRefreshRealtime() {
		return refreshRealtime;
	}
}
