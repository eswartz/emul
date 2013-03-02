/*
  StaticStoredSettings.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
