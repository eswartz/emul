/*
  StaticStoredSettings.java

  (c) 2011 Edward Swartz

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
package v9t9.common.settings;

/**
 * @author ejs
 *
 */
public class StaticStoredSettings extends BaseStoredSettings {

	private String configFileName;

	/**
	 * 
	 */
	public StaticStoredSettings(String context, String configFileName) {
		super(context);
		this.configFileName = configFileName;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.settings.IStoredSettings#getConfigFileName()
	 */
	@Override
	public String getConfigFileName() {
		return configFileName;
	}

	/**
	 * @param configFileName the configFileName to set
	 */
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}
}
