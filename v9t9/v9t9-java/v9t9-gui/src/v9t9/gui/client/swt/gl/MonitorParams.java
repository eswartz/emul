/*
  MonitorParams.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
